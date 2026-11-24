package com.example.pulseup.presentation.ejercicios

import com.example.pulseup.domain.model.RecomendacionEjercicio
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EjercicioDetalleViewModelTest {

    @get:Rule
    val main = ReglaMainDispatcher()

    private val recomendacion = RecomendacionEjercicio(
        "intermedio", "repeticiones", 6, 12, 3, 4, 150, "2-3", null, null, 72.5, null,
        "Es una guía general y no reemplaza a un entrenador ni a un médico."
    )

    private fun vm(repo: EjerciciosFalso, perfil: PerfilFisicoFalso = PerfilFisicoFalso(nivel = "intermedio")) =
        EjercicioDetalleViewModel(repo, perfil, 1)

    @Test
    fun `carga el detalle`() = runTest(main.dispatcher) {
        val vm = vm(EjerciciosFalso().apply { detalleRespuesta = detalle(recomendacion) })
        advanceUntilIdle()

        val s = vm.estado.value
        assertFalse(s.cargando)
        assertNull(s.error)
        assertEquals("Press de banca", s.ejercicio?.nombre)
        assertEquals(recomendacion, s.ejercicio?.recomendacion)
    }

    @Test
    fun `error y reintentar`() = runTest(main.dispatcher) {
        val repo = EjerciciosFalso().apply { fallarDetalle = "Ejercicio no encontrado" }
        val vm = vm(repo)
        advanceUntilIdle()
        assertEquals("Ejercicio no encontrado", vm.estado.value.error)
        assertNull(vm.estado.value.ejercicio)

        repo.fallarDetalle = null
        vm.cargar()
        advanceUntilIdle()
        assertNull(vm.estado.value.error)
        assertNotNull(vm.estado.value.ejercicio)
    }

    // ---------- Botón "Ir a mi perfil físico" ----------

    @Test
    fun `sin perfil fisico se ofrece ir a llenarlo`() = runTest(main.dispatcher) {
        val repo = EjerciciosFalso().apply { detalleRespuesta = detalle(null, "Completa tu perfil físico para ver la recomendación") }
        val vm = vm(repo, PerfilFisicoFalso(nivel = null))
        advanceUntilIdle()

        assertEquals(true, vm.estado.value.sinPerfilFisico)
        assertTrue(vm.estado.value.mostrarIrAPerfil)
    }

    @Test
    fun `con perfil pero ejercicio sin recomendacion se muestra solo el motivo`() = runTest(main.dispatcher) {
        // El motivo da igual: la decisión sale de que el perfil existe
        val repo = EjerciciosFalso().apply { detalleRespuesta = detalle(null, "Este ejercicio todavía no tiene recomendación") }
        val vm = vm(repo, PerfilFisicoFalso(nivel = "intermedio"))
        advanceUntilIdle()

        assertNull(vm.estado.value.ejercicio?.recomendacion)
        assertEquals("Este ejercicio todavía no tiene recomendación", vm.estado.value.ejercicio?.recomendacionMotivo)
        assertEquals(false, vm.estado.value.sinPerfilFisico)
        assertFalse(vm.estado.value.mostrarIrAPerfil)
    }

    @Test
    fun `con recomendacion no hay boton`() = runTest(main.dispatcher) {
        val vm = vm(EjerciciosFalso().apply { detalleRespuesta = detalle(recomendacion) }, PerfilFisicoFalso(nivel = null))
        advanceUntilIdle()

        assertFalse(vm.estado.value.mostrarIrAPerfil)
    }

    @Test
    fun `si no se puede consultar el perfil no se ofrece el boton`() = runTest(main.dispatcher) {
        val repo = EjerciciosFalso().apply { detalleRespuesta = detalle(null) }
        val vm = vm(repo, PerfilFisicoFalso(nivel = null, falla = true))
        advanceUntilIdle()

        assertNull(vm.estado.value.sinPerfilFisico)
        assertFalse(vm.estado.value.mostrarIrAPerfil)
        assertNotNull(vm.estado.value.ejercicio) // el detalle se muestra igual
    }

    // ---------- Al volver del perfil físico ----------

    @Test
    fun `al volver del perfil fisico se actualiza la recomendacion y se quita el boton`() = runTest(main.dispatcher) {
        val repo = EjerciciosFalso().apply { detalleRespuesta = detalle(null) }
        val perfil = PerfilFisicoFalso(nivel = null)
        val vm = vm(repo, perfil)
        advanceUntilIdle()
        assertTrue(vm.estado.value.mostrarIrAPerfil)

        // El usuario llenó su perfil
        perfil.nivel = "intermedio"
        repo.detalleRespuesta = detalle(recomendacion)
        vm.refrescarRecomendacion()
        advanceUntilIdle()

        assertEquals(recomendacion, vm.estado.value.ejercicio?.recomendacion)
        assertEquals(false, vm.estado.value.sinPerfilFisico)
        assertFalse(vm.estado.value.mostrarIrAPerfil)
    }

    @Test
    fun `refrescar no borra lo que se ve si falla`() = runTest(main.dispatcher) {
        val repo = EjerciciosFalso().apply { detalleRespuesta = detalle(null) }
        val vm = vm(repo, PerfilFisicoFalso(nivel = null))
        advanceUntilIdle()

        repo.fallarDetalle = "Sin conexión"
        vm.refrescarRecomendacion()
        advanceUntilIdle()

        assertNull(vm.estado.value.error)
        assertEquals("Press de banca", vm.estado.value.ejercicio?.nombre)
        assertTrue(vm.estado.value.mostrarIrAPerfil)
    }
}
