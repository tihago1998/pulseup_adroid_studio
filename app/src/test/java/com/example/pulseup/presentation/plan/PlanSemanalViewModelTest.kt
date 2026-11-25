package com.example.pulseup.presentation.plan

import com.example.pulseup.domain.model.DiaPrevisualizado
import com.example.pulseup.presentation.ejercicios.ReglaMainDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlanSemanalViewModelTest {

    @get:Rule
    val main = ReglaMainDispatcher()

    private fun planConAvisoElMiercoles() = PlanFalso().apply {
        previsualizacion = previsualizacion.map {
            if (it.dia == 3) DiaPrevisualizado(3, "miércoles", false, listOf(aviso(diaConflicto = 2))) else it
        }
    }

    @Test
    fun `sin avisos asigna directo despues de previsualizar la rutina`() = runTest(main.dispatcher) {
        val plan = PlanFalso()
        val vm = PlanSemanalViewModel(plan, RutinasFalso())
        advanceUntilIdle()

        vm.abrirSelector(4)
        vm.elegirRutina(10)
        advanceUntilIdle()

        assertEquals(listOf("rutina 10"), plan.previsualizadas)
        assertEquals(listOf(4 to 10), plan.asignados)
        assertNull(vm.estado.value.porConfirmar)
        assertEquals(TipoNota.EXITO, vm.estado.value.notas.single().tipo)
    }

    @Test
    fun `con avisos pide confirmar y Asignar igual asigna`() = runTest(main.dispatcher) {
        val plan = planConAvisoElMiercoles().apply { avisosAlAsignar = mapOf(3 to listOf(aviso(diaConflicto = 2))) }
        val vm = PlanSemanalViewModel(plan, RutinasFalso())
        advanceUntilIdle()

        vm.abrirSelector(3)
        vm.elegirRutina(10)
        advanceUntilIdle()

        assertTrue(plan.asignados.isEmpty())
        val p = vm.estado.value.porConfirmar!!
        assertEquals(3, p.dia)
        assertEquals("Torso", p.nombreRutina)
        assertEquals(1, p.avisos.size)

        vm.confirmarAsignacion()
        advanceUntilIdle()

        assertEquals(listOf(3 to 10), plan.asignados)
        // El PUT también trae los avisos: se muestran después de guardar
        assertEquals(listOf(TipoNota.EXITO, TipoNota.ADVERTENCIA), vm.estado.value.notas.map { it.tipo })
    }

    @Test
    fun `cancelar no asigna`() = runTest(main.dispatcher) {
        val plan = planConAvisoElMiercoles()
        val vm = PlanSemanalViewModel(plan, RutinasFalso())
        advanceUntilIdle()

        vm.abrirSelector(3)
        vm.elegirRutina(10)
        advanceUntilIdle()
        vm.cancelarAsignacion()
        advanceUntilIdle()

        assertTrue(plan.asignados.isEmpty())
        assertNull(vm.estado.value.porConfirmar)
    }

    @Test
    fun `si previsualizar falla no asigna y muestra el error`() = runTest(main.dispatcher) {
        val plan = PlanFalso().apply { fallarPrevisualizar = "No se pudo conectar con el servidor. Revisa tu conexión." }
        val vm = PlanSemanalViewModel(plan, RutinasFalso())
        advanceUntilIdle()

        vm.abrirSelector(1)
        vm.elegirRutina(10)
        advanceUntilIdle()

        assertTrue(plan.asignados.isEmpty())
        assertEquals(TipoNota.ERROR, vm.estado.value.notas.single().tipo)
    }

    @Test
    fun `refrescar recarga el plan sin pantalla de carga`() = runTest(main.dispatcher) {
        val plan = PlanFalso()
        val vm = PlanSemanalViewModel(plan, RutinasFalso())
        advanceUntilIdle()
        val antes = plan.vecesPlan

        plan.dias = plan.dias.map { if (it.diaSemana == 1) diaConRutina(1, ejercicioPlan(36)) else it }
        vm.refrescar()
        advanceUntilIdle()

        assertEquals(antes + 1, plan.vecesPlan)
        assertEquals(1, vm.estado.value.dias[0].ejercicios.size)
    }
}
