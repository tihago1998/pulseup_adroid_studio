package com.example.pulseup.presentation.plan

import com.example.pulseup.domain.model.ImagenEjercicio
import com.example.pulseup.presentation.ejercicios.EjerciciosFalso
import com.example.pulseup.presentation.ejercicios.ReglaMainDispatcher
import com.example.pulseup.presentation.ejercicios.detalle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Tocar la foto de un ejercicio del plan abre sus fotos (GET api/ejercicios/{id}). */
@OptIn(ExperimentalCoroutinesApi::class)
class FotosPlanDiaTest {

    @get:Rule
    val main = ReglaMainDispatcher()

    private val fotos = listOf(
        ImagenEjercicio(1, "http://servidor/static/ej/3/1.jpg"),
        ImagenEjercicio(0, "http://servidor/static/ej/3/0.jpg")
    )

    private val press = ejercicioPlan(36, 3)
    private val burpees = ejercicioPlan(37, 41)

    private fun TestScope.vm(ejercicios: EjerciciosFalso): PlanDiaViewModel {
        val plan = PlanFalso().apply {
            dias = dias.map { if (it.diaSemana == 1) diaConRutina(1, press, burpees) else it }
        }
        return PlanDiaViewModel(plan, ejercicios, 1).also { advanceUntilIdle() }
    }

    @Test
    fun `tocar la foto carga las fotos del ejercicio en orden`() = runTest(main.dispatcher) {
        val ejercicios = EjerciciosFalso().apply { detalleRespuesta = detalle().copy(imagenes = fotos) }
        val vm = vm(ejercicios)

        vm.abrirFotos(press)
        val cargando = vm.estado.value.fotos!!
        assertTrue(cargando.cargando)
        assertEquals("Ejercicio 3", cargando.nombre)

        advanceUntilIdle()
        val f = vm.estado.value.fotos!!
        assertFalse(f.cargando)
        assertEquals(listOf(0, 1), f.imagenes.map { it.orden })
        assertEquals("http://servidor/static/ej/3/0.jpg", f.imagenes[0].url)
        assertFalse(f.sinFotos)
        assertEquals(listOf(3), ejercicios.detallesPedidos)
    }

    @Test
    fun `ejercicio sin fotos abre la ventana con el aviso`() = runTest(main.dispatcher) {
        val ejercicios = EjerciciosFalso() // detalle() sin imágenes
        val vm = vm(ejercicios)

        vm.abrirFotos(burpees)
        advanceUntilIdle()

        val f = vm.estado.value.fotos!!
        assertTrue(f.sinFotos)
        assertNull(f.error)
        assertEquals(41, f.ejercicioId)
    }

    @Test
    fun `si falla muestra el error y reintentar vuelve a pedirlas`() = runTest(main.dispatcher) {
        val ejercicios = EjerciciosFalso().apply {
            detalleRespuesta = detalle().copy(imagenes = fotos)
            fallarDetalle = "No se pudo conectar con el servidor. Revisa tu conexión."
        }
        val vm = vm(ejercicios)

        vm.abrirFotos(press)
        advanceUntilIdle()
        assertEquals("No se pudo conectar con el servidor. Revisa tu conexión.", vm.estado.value.fotos!!.error)
        assertFalse(vm.estado.value.fotos!!.sinFotos)

        ejercicios.fallarDetalle = null
        vm.reintentarFotos()
        assertTrue(vm.estado.value.fotos!!.cargando)
        advanceUntilIdle()

        val f = vm.estado.value.fotos!!
        assertNull(f.error)
        assertEquals(2, f.imagenes.size)
        assertEquals(listOf(3, 3), ejercicios.detallesPedidos)
    }

    @Test
    fun `varios toques seguidos hacen una sola llamada`() = runTest(main.dispatcher) {
        val ejercicios = EjerciciosFalso().apply { detalleRespuesta = detalle().copy(imagenes = fotos) }
        val vm = vm(ejercicios)

        vm.abrirFotos(press)
        vm.abrirFotos(press)
        vm.cerrarFotos()
        vm.abrirFotos(press) // cerró y volvió a tocar mientras cargaba
        advanceUntilIdle()

        assertEquals(listOf(3), ejercicios.detallesPedidos)
        assertEquals(2, vm.estado.value.fotos!!.imagenes.size)
    }

    @Test
    fun `volver a abrir las fotos usa las ya cargadas`() = runTest(main.dispatcher) {
        val ejercicios = EjerciciosFalso().apply { detalleRespuesta = detalle().copy(imagenes = fotos) }
        val vm = vm(ejercicios)

        vm.abrirFotos(press)
        advanceUntilIdle()
        vm.cerrarFotos()
        assertNull(vm.estado.value.fotos)

        vm.abrirFotos(press)
        val f = vm.estado.value.fotos!!
        assertFalse(f.cargando)
        assertEquals(2, f.imagenes.size)
        assertEquals(listOf(3), ejercicios.detallesPedidos)
    }

    @Test
    fun `cerrar la ventana no recarga el plan ni toca el dia`() = runTest(main.dispatcher) {
        val ejercicios = EjerciciosFalso()
        val vm = vm(ejercicios)
        val antes = vm.estado.value.dia

        vm.abrirFotos(press)
        advanceUntilIdle()
        vm.cerrarFotos()
        advanceUntilIdle()

        assertNull(vm.estado.value.fotos)
        assertEquals(antes, vm.estado.value.dia)
        assertNull(vm.estado.value.edicion)
    }
}
