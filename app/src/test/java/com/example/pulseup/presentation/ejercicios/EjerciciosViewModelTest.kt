package com.example.pulseup.presentation.ejercicios

import com.example.pulseup.domain.model.FiltroCatalogo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EjerciciosViewModelTest {

    @get:Rule
    val main = ReglaMainDispatcher()

    private fun vm(repo: EjerciciosFalso, nivelPerfil: String? = null, perfilFalla: Boolean = false) =
        EjerciciosViewModel(repo, PerfilFisicoFalso(nivelPerfil, perfilFalla))

    // ---------- Primera carga y nivel por defecto ----------

    @Test
    fun `al abrir pide la primera pagina de 20`() = runTest(main.dispatcher) {
        val repo = EjerciciosFalso(total = 45)
        val vm = vm(repo)
        advanceUntilIdle()

        val s = vm.estado.value
        assertFalse(s.cargando)
        assertEquals(20, s.ejercicios.size)
        assertEquals(45, s.total)
        assertTrue(s.hayMas)
        assertEquals(Triple(FiltroCatalogo(), 20, 0), repo.busquedas.single())
        assertEquals(listOf("gimnasio", "casa"), s.opciones?.ambientes)
    }

    @Test
    fun `el nivel por defecto es el del perfil fisico`() = runTest(main.dispatcher) {
        val repo = EjerciciosFalso()
        val vm = vm(repo, nivelPerfil = "avanzado")
        advanceUntilIdle()

        assertEquals("avanzado", vm.estado.value.filtro.nivel)
        assertEquals("avanzado", repo.busquedas.single().first.nivel)
    }

    @Test
    fun `sin perfil fisico o si falla no se filtra por nivel`() = runTest(main.dispatcher) {
        val sinPerfil = vm(EjerciciosFalso(), nivelPerfil = null)
        val conFallo = vm(EjerciciosFalso(), nivelPerfil = "avanzado", perfilFalla = true)
        advanceUntilIdle()

        assertNull(sinPerfil.estado.value.filtro.nivel)
        assertNull(conFallo.estado.value.filtro.nivel)
        assertFalse(conFallo.estado.value.cargando)
    }

    // ---------- Filtros ----------

    @Test
    fun `cambiar un filtro vuelve a la primera pagina con ese filtro`() = runTest(main.dispatcher) {
        val repo = EjerciciosFalso()
        val vm = vm(repo)
        advanceUntilIdle()
        vm.cargarMas()
        advanceUntilIdle()

        vm.elegirAmbiente("casa")
        vm.elegirMusculo("pecho")
        vm.elegirEquipo("barra")
        advanceUntilIdle()

        val ultima = repo.busquedas.last()
        assertEquals(FiltroCatalogo(ambiente = "casa", musculo = "pecho", equipo = "barra"), ultima.first)
        assertEquals(0, ultima.third)
        assertEquals(20, vm.estado.value.ejercicios.size)
    }

    @Test
    fun `el texto espera a que se deje de escribir`() = runTest(main.dispatcher) {
        val repo = EjerciciosFalso()
        val vm = vm(repo)
        advanceUntilIdle()
        val antes = repo.busquedas.size

        vm.cambiarTexto("p")
        vm.cambiarTexto("pr")
        vm.cambiarTexto("pre")
        advanceTimeBy(ESPERA_BUSQUEDA_MS - 1)
        assertEquals(antes, repo.busquedas.size)

        advanceUntilIdle()
        assertEquals(antes + 1, repo.busquedas.size)
        assertEquals("pre", repo.busquedas.last().first.texto)
    }

    @Test
    fun `quitar filtros deja la busqueda sin filtros`() = runTest(main.dispatcher) {
        val repo = EjerciciosFalso()
        val vm = vm(repo, nivelPerfil = "intermedio")
        advanceUntilIdle()
        assertTrue(vm.estado.value.conFiltros)

        vm.quitarFiltros()
        advanceUntilIdle()

        assertFalse(vm.estado.value.conFiltros)
        assertEquals(FiltroCatalogo(), repo.busquedas.last().first)
    }

    // ---------- Paginación ----------

    @Test
    fun `cargar mas pide la pagina siguiente y la agrega`() = runTest(main.dispatcher) {
        val repo = EjerciciosFalso(total = 45)
        val vm = vm(repo)
        advanceUntilIdle()

        vm.cargarMas()
        advanceUntilIdle()
        assertEquals(40, vm.estado.value.ejercicios.size)
        assertEquals(20, repo.busquedas.last().third)

        vm.cargarMas()
        advanceUntilIdle()
        assertEquals(45, vm.estado.value.ejercicios.size)
        assertFalse(vm.estado.value.hayMas)

        val pedidas = repo.busquedas.size
        vm.cargarMas() // ya no hay más: no pide nada
        advanceUntilIdle()
        assertEquals(pedidas, repo.busquedas.size)
        assertEquals((1..45).toList(), vm.estado.value.ejercicios.map { it.id })
    }

    @Test
    fun `no pide otra pagina mientras una esta en curso`() = runTest(main.dispatcher) {
        val repo = EjerciciosFalso(total = 100)
        val vm = vm(repo)
        advanceUntilIdle()

        vm.cargarMas()
        vm.cargarMas()
        vm.cargarMas()
        advanceUntilIdle()

        assertEquals(listOf(0, 20), repo.busquedas.map { it.third })
    }

    // ---------- Errores ----------

    @Test
    fun `error en la primera pagina y reintentar`() = runTest(main.dispatcher) {
        val repo = EjerciciosFalso().apply { fallarBusqueda = "No se pudo conectar con el servidor. Revisa tu conexión." }
        val vm = vm(repo)
        advanceUntilIdle()

        assertEquals("No se pudo conectar con el servidor. Revisa tu conexión.", vm.estado.value.error)
        assertFalse(vm.estado.value.cargando)
        assertTrue(vm.estado.value.ejercicios.isEmpty())
        assertFalse(vm.estado.value.vacio) // error, no "sin resultados"

        repo.fallarBusqueda = null
        vm.reintentar()
        advanceUntilIdle()

        assertNull(vm.estado.value.error)
        assertEquals(20, vm.estado.value.ejercicios.size)
    }

    @Test
    fun `error al paginar conserva la lista y permite reintentar`() = runTest(main.dispatcher) {
        val repo = EjerciciosFalso(total = 45)
        val vm = vm(repo)
        advanceUntilIdle()

        repo.fallarBusqueda = "Error del servidor (500)"
        vm.cargarMas()
        advanceUntilIdle()
        assertEquals(20, vm.estado.value.ejercicios.size)
        assertEquals("Error del servidor (500)", vm.estado.value.errorMas)
        assertNull(vm.estado.value.error)

        // Con errorMas, llegar al final no reintenta solo (evita un bucle de errores)
        val pedidas = repo.busquedas.size
        vm.cargarMas()
        advanceUntilIdle()
        assertEquals(pedidas, repo.busquedas.size)

        repo.fallarBusqueda = null
        vm.reintentarMas()
        advanceUntilIdle()
        assertNull(vm.estado.value.errorMas)
        assertEquals(40, vm.estado.value.ejercicios.size)
    }

    @Test
    fun `sin resultados queda vacio`() = runTest(main.dispatcher) {
        val vm = vm(EjerciciosFalso(total = 0))
        advanceUntilIdle()

        assertTrue(vm.estado.value.vacio)
        assertFalse(vm.estado.value.hayMas)
    }
}
