package com.example.pulseup.presentation.plan

import com.example.pulseup.domain.model.DiaPlan
import com.example.pulseup.domain.model.DiaPrevisualizado
import com.example.pulseup.domain.model.RutinaPlan
import com.example.pulseup.presentation.ejercicios.EjerciciosFalso
import com.example.pulseup.presentation.ejercicios.ReglaMainDispatcher
import com.example.pulseup.presentation.ejercicios.ejercicio
import com.example.pulseup.presentation.rutinas.CrearRutinaViewModel
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

/** Asignar una rutina a un día con contenido pide "Reemplazar" antes. */
@OptIn(ExperimentalCoroutinesApi::class)
class ReemplazoRutinaTest {

    @get:Rule
    val main = ReglaMainDispatcher()

    private fun planConMartes() = PlanFalso().apply {
        dias = dias.map { if (it.diaSemana == 2) diaConRutina(2, ejercicioPlan(36)) else it }
    }

    @Test
    fun `texto del reemplazo`() {
        assertEquals("Esto reemplazará los ejercicios de martes", textoReemplazo(listOf("martes")))
        assertEquals("Esto reemplazará los ejercicios de lunes y jueves", textoReemplazo(listOf("lunes", "jueves")))
    }

    @Test
    fun `dia con contenido`() {
        assertFalse(DiaPlan(1, "lunes", null).tieneContenido)
        assertTrue(DiaPlan(1, "lunes", RutinaPlan(1, "Pecho", emptyList())).tieneContenido)
        assertTrue(diaConRutina(1, ejercicioPlan(1)).tieneContenido)
    }

    // ---------- Plan semanal: "Cambiar rutina" ----------

    @Test
    fun `cambiar la rutina de un dia con ejercicios pide confirmar`() = runTest(main.dispatcher) {
        val plan = planConMartes()
        val vm = PlanSemanalViewModel(plan, RutinasFalso())
        advanceUntilIdle()

        vm.abrirSelector(2)
        vm.elegirRutina(10)
        advanceUntilIdle()

        val r = vm.estado.value.porReemplazar!!
        assertEquals("Esto reemplazará los ejercicios de martes", r.texto)
        assertTrue(plan.previsualizadas.isEmpty())
        assertTrue(plan.asignados.isEmpty())
        assertNull(vm.estado.value.diaEditando)
    }

    @Test
    fun `Reemplazar sigue con previsualizar y asigna`() = runTest(main.dispatcher) {
        val plan = planConMartes()
        val vm = PlanSemanalViewModel(plan, RutinasFalso())
        advanceUntilIdle()

        vm.abrirSelector(2)
        vm.elegirRutina(10)
        vm.confirmarReemplazo()
        advanceUntilIdle()

        assertEquals(listOf("rutina 10"), plan.previsualizadas)
        assertEquals(listOf(2 to 10), plan.asignados)
        assertNull(vm.estado.value.porReemplazar)
    }

    @Test
    fun `Reemplazar y luego aviso de 48 h pide las dos confirmaciones`() = runTest(main.dispatcher) {
        val plan = planConMartes().apply {
            previsualizacion = previsualizacion.map {
                if (it.dia == 2) DiaPrevisualizado(2, "martes", true, listOf(aviso(diaConflicto = 1))) else it
            }
        }
        val vm = PlanSemanalViewModel(plan, RutinasFalso())
        advanceUntilIdle()

        vm.abrirSelector(2)
        vm.elegirRutina(10)
        vm.confirmarReemplazo()
        advanceUntilIdle()
        assertNotNull(vm.estado.value.porConfirmar)
        assertTrue(plan.asignados.isEmpty())

        vm.confirmarAsignacion()
        advanceUntilIdle()
        assertEquals(listOf(2 to 10), plan.asignados)
    }

    @Test
    fun `Cancelar no asigna nada`() = runTest(main.dispatcher) {
        val plan = planConMartes()
        val vm = PlanSemanalViewModel(plan, RutinasFalso())
        advanceUntilIdle()

        vm.abrirSelector(2)
        vm.elegirRutina(10)
        vm.cancelarReemplazo()
        advanceUntilIdle()

        assertNull(vm.estado.value.porReemplazar)
        assertTrue(plan.previsualizadas.isEmpty())
        assertTrue(plan.asignados.isEmpty())
    }

    @Test
    fun `un dia de descanso no pide confirmar el reemplazo`() = runTest(main.dispatcher) {
        val plan = planConMartes()
        val vm = PlanSemanalViewModel(plan, RutinasFalso())
        advanceUntilIdle()

        vm.abrirSelector(4)
        vm.elegirRutina(10)
        advanceUntilIdle()

        assertNull(vm.estado.value.porReemplazar)
        assertEquals(listOf(4 to 10), plan.asignados)
    }

    // ---------- Crear rutina: "¿Quieres ponerla en tu plan?" ----------

    private fun TestScope.crearConDiasOcupados(plan: PlanFalso): CrearRutinaViewModel {
        plan.previsualizacion = plan.previsualizacion.map {
            if (it.dia == 1 || it.dia == 4) it.copy(tieneRutina = true) else it
        }
        val vm = CrearRutinaViewModel(RutinasFalso(), EjerciciosFalso(), plan)
        advanceUntilIdle()
        vm.cambiarNombre("Empuje")
        vm.agregar(ejercicio(1))
        vm.guardar()
        advanceUntilIdle()
        return vm
    }

    @Test
    fun `crear rutina pide reemplazar los dias elegidos que ya tienen rutina`() = runTest(main.dispatcher) {
        val plan = PlanFalso()
        val vm = crearConDiasOcupados(plan)
        val dias = vm.estado.value.ofrecerPlan!!.dias
        vm.alternarDia(dias[3]) // jueves, con rutina
        vm.alternarDia(dias[1]) // martes, libre
        vm.alternarDia(dias[0]) // lunes, con rutina

        vm.agregarAlPlan()
        advanceUntilIdle()

        assertEquals(listOf("lunes", "jueves"), vm.estado.value.ofrecerPlan!!.porReemplazar)
        assertTrue(plan.asignados.isEmpty())

        vm.confirmarReemplazo()
        advanceUntilIdle()
        assertEquals(listOf(1 to 50, 2 to 50, 4 to 50), plan.asignados)
        assertNull(vm.estado.value.ofrecerPlan!!.porReemplazar)
        assertNotNull(vm.estado.value.ofrecerPlan!!.resumen)
    }

    @Test
    fun `crear rutina con Cancelar vuelve a la eleccion sin asignar`() = runTest(main.dispatcher) {
        val plan = PlanFalso()
        val vm = crearConDiasOcupados(plan)
        vm.alternarDia(vm.estado.value.ofrecerPlan!!.dias[0])

        vm.agregarAlPlan()
        vm.cancelarReemplazo()
        advanceUntilIdle()

        assertTrue(plan.asignados.isEmpty())
        assertNull(vm.estado.value.ofrecerPlan!!.porReemplazar)
        assertNull(vm.estado.value.ofrecerPlan!!.resumen)
        assertEquals(setOf(1), vm.estado.value.ofrecerPlan!!.seleccionados)
    }

    @Test
    fun `crear rutina en dias libres no pregunta`() = runTest(main.dispatcher) {
        val plan = PlanFalso()
        val vm = crearConDiasOcupados(plan)
        vm.alternarDia(vm.estado.value.ofrecerPlan!!.dias[2]) // miércoles, libre

        vm.agregarAlPlan()
        advanceUntilIdle()

        assertNull(vm.estado.value.ofrecerPlan!!.porReemplazar)
        assertEquals(listOf(3 to 50), plan.asignados)
    }
}
