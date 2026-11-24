package com.example.pulseup.presentation.rutinas

import com.example.pulseup.presentation.ejercicios.EjerciciosFalso
import com.example.pulseup.presentation.ejercicios.ReglaMainDispatcher
import com.example.pulseup.presentation.ejercicios.ejercicio
import com.example.pulseup.presentation.plan.PlanFalso
import com.example.pulseup.presentation.plan.RutinasFalso
import com.example.pulseup.presentation.plan.TipoNota
import com.example.pulseup.presentation.plan.aviso
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
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
class CrearRutinaViewModelTest {

    @get:Rule
    val main = ReglaMainDispatcher()

    /** Llena el formulario mínimo y guarda; la rutina nueva tiene id 50. */
    private fun TestScope.guardada(plan: PlanFalso, rutinas: RutinasFalso = RutinasFalso()): CrearRutinaViewModel {
        val vm = CrearRutinaViewModel(rutinas, EjerciciosFalso(), plan)
        advanceUntilIdle()
        vm.cambiarNombre("Empuje")
        vm.agregar(ejercicio(1))
        vm.guardar()
        advanceUntilIdle()
        return vm
    }

    @Test
    fun `al guardar ofrece ponerla en el plan con los avisos de cada dia`() = runTest(main.dispatcher) {
        val plan = PlanFalso()
        val vm = guardada(plan)

        val oferta = vm.estado.value.ofrecerPlan!!
        assertEquals(50, oferta.idRutina)
        assertFalse(oferta.cargando)
        assertEquals(7, oferta.dias.size)
        assertEquals(listOf("rutina 50"), plan.previsualizadas)
        assertFalse(vm.estado.value.creada) // no se cierra hasta responder la hoja
    }

    @Test
    fun `ahora no cierra sin tocar el plan`() = runTest(main.dispatcher) {
        val plan = PlanFalso()
        val vm = guardada(plan)

        vm.terminar()
        advanceUntilIdle()

        assertTrue(vm.estado.value.creada)
        assertTrue(plan.asignados.isEmpty())
    }

    @Test
    fun `elegir varios dias asigna cada uno y resume los avisos`() = runTest(main.dispatcher) {
        val plan = PlanFalso().apply { avisosAlAsignar = mapOf(2 to listOf(aviso(diaConflicto = 1))) }
        val vm = guardada(plan)
        val dias = vm.estado.value.ofrecerPlan!!.dias

        vm.alternarDia(dias[3]) // jueves
        vm.alternarDia(dias[0]) // lunes
        vm.alternarDia(dias[1]) // martes
        vm.alternarDia(dias[3]) // jueves otra vez: se desmarca
        assertEquals(setOf(1, 2), vm.estado.value.ofrecerPlan!!.seleccionados)

        vm.agregarAlPlan()
        advanceUntilIdle()

        assertEquals(listOf(1 to 50, 2 to 50), plan.asignados)
        val resumen = vm.estado.value.ofrecerPlan!!.resumen!!
        assertEquals("Rutina agregada a tu plan: lunes y martes.", resumen[0].texto)
        assertEquals(TipoNota.ADVERTENCIA, resumen[1].tipo)
        assertEquals(2, resumen.size)
        assertFalse(vm.estado.value.creada)

        vm.terminar()
        assertTrue(vm.estado.value.creada)
    }

    @Test
    fun `un dia que falla se informa y los demas se guardan`() = runTest(main.dispatcher) {
        val plan = PlanFalso().apply { diasQueFallanAlAsignar = setOf(3) }
        val vm = guardada(plan)
        val dias = vm.estado.value.ofrecerPlan!!.dias

        vm.alternarDia(dias[2]) // miércoles
        vm.alternarDia(dias[4]) // viernes
        vm.agregarAlPlan()
        advanceUntilIdle()

        val resumen = vm.estado.value.ofrecerPlan!!.resumen!!
        assertEquals("Rutina agregada a tu plan: viernes.", resumen[0].texto)
        assertEquals(TipoNota.ERROR, resumen[1].tipo)
        assertEquals("No se pudo agregar al miércoles: Error del servidor (500)", resumen[1].texto)
    }

    @Test
    fun `sin dias elegidos no hace nada`() = runTest(main.dispatcher) {
        val plan = PlanFalso()
        val vm = guardada(plan)

        vm.agregarAlPlan()
        advanceUntilIdle()

        assertTrue(plan.asignados.isEmpty())
        assertNull(vm.estado.value.ofrecerPlan!!.resumen)
    }

    @Test
    fun `si previsualizar falla se puede reintentar o seguir con ahora no`() = runTest(main.dispatcher) {
        val plan = PlanFalso().apply { fallarPrevisualizar = "No se pudo conectar con el servidor. Revisa tu conexión." }
        val vm = guardada(plan)
        assertNotNull(vm.estado.value.ofrecerPlan!!.error)

        plan.fallarPrevisualizar = null
        vm.reintentarDiasPlan()
        advanceUntilIdle()
        assertNull(vm.estado.value.ofrecerPlan!!.error)
        assertEquals(7, vm.estado.value.ofrecerPlan!!.dias.size)
    }

    @Test
    fun `si crear falla no se ofrece el plan`() = runTest(main.dispatcher) {
        val vm = guardada(PlanFalso(), RutinasFalso().apply { fallarCrear = "El nombre ya existe" })
        assertNull(vm.estado.value.ofrecerPlan)
        assertEquals("El nombre ya existe", vm.estado.value.error)
    }
}
