package com.example.pulseup.presentation.ejercicios

import com.example.pulseup.domain.model.RecomendacionEjercicio
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    @Test
    fun `carga el detalle`() = runTest(main.dispatcher) {
        val vm = EjercicioDetalleViewModel(EjerciciosFalso().apply { detalleRespuesta = detalle(recomendacion) }, 1)
        advanceUntilIdle()

        val s = vm.estado.value
        assertFalse(s.cargando)
        assertNull(s.error)
        assertEquals("Press de banca", s.ejercicio?.nombre)
        assertEquals(recomendacion, s.ejercicio?.recomendacion)
    }

    @Test
    fun `sin perfil fisico trae el motivo en lugar de la recomendacion`() = runTest(main.dispatcher) {
        val vm = EjercicioDetalleViewModel(EjerciciosFalso().apply { detalleRespuesta = detalle(null) }, 1)
        advanceUntilIdle()

        assertNull(vm.estado.value.ejercicio?.recomendacion)
        assertEquals("Completa tu perfil físico para ver la recomendación", vm.estado.value.ejercicio?.recomendacionMotivo)
    }

    @Test
    fun `error y reintentar`() = runTest(main.dispatcher) {
        val repo = EjerciciosFalso().apply { fallarDetalle = "Ejercicio no encontrado" }
        val vm = EjercicioDetalleViewModel(repo, 1)
        advanceUntilIdle()
        assertEquals("Ejercicio no encontrado", vm.estado.value.error)
        assertNull(vm.estado.value.ejercicio)

        repo.fallarDetalle = null
        vm.cargar()
        advanceUntilIdle()
        assertNull(vm.estado.value.error)
        assertNotNull(vm.estado.value.ejercicio)
    }

    @Test
    fun `al volver del perfil fisico se actualiza la recomendacion`() = runTest(main.dispatcher) {
        val repo = EjerciciosFalso().apply { detalleRespuesta = detalle(null) }
        val vm = EjercicioDetalleViewModel(repo, 1)
        advanceUntilIdle()

        repo.detalleRespuesta = detalle(recomendacion) // el usuario llenó su perfil
        vm.refrescarRecomendacion()
        advanceUntilIdle()

        assertEquals(recomendacion, vm.estado.value.ejercicio?.recomendacion)
    }

    @Test
    fun `refrescar no borra lo que se ve si falla`() = runTest(main.dispatcher) {
        val repo = EjerciciosFalso().apply { detalleRespuesta = detalle(null) }
        val vm = EjercicioDetalleViewModel(repo, 1)
        advanceUntilIdle()

        repo.fallarDetalle = "Sin conexión"
        vm.refrescarRecomendacion()
        advanceUntilIdle()

        assertNull(vm.estado.value.error)
        assertEquals("Press de banca", vm.estado.value.ejercicio?.nombre)
    }
}
