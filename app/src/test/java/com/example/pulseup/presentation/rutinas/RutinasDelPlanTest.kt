package com.example.pulseup.presentation.rutinas

import com.example.pulseup.data.mapper.aDominio
import com.example.pulseup.data.remote.dto.RutinasListaResponse
import com.example.pulseup.domain.model.sinLasDelPlan
import com.example.pulseup.presentation.ejercicios.ReglaMainDispatcher
import com.example.pulseup.presentation.plan.PlanFalso
import com.example.pulseup.presentation.plan.PlanSemanalViewModel
import com.example.pulseup.presentation.plan.RutinasFalso
import com.example.pulseup.presentation.plan.rutina
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Las rutinas con origen = "plan" solo se ven dentro del plan de su día. */
@OptIn(ExperimentalCoroutinesApi::class)
class RutinasDelPlanTest {

    @get:Rule
    val main = ReglaMainDispatcher()

    private val lista = listOf(
        rutina(10, "Torso"),
        rutina(8, "Lunes · pecho", origen = "plan"),
        rutina(12, "Full body", origen = "otro"),
        rutina(11, "Miércoles · Torso", origen = "plan")
    )

    @Test
    fun `origen se lee del backend y es null si no viene`() {
        val json = """{"rutinas":[
            {"id":8,"nombre":"Lunes · pecho","es_plantilla":false,"origen":"plan"},
            {"id":10,"nombre":"Torso","es_plantilla":false,"origen":null},
            {"id":1,"nombre":"Plantilla","es_plantilla":true}
        ]}"""
        val rutinas = Gson().fromJson(json, RutinasListaResponse::class.java).rutinas!!.map { it.aDominio() }

        assertEquals("plan", rutinas[0].origen)
        assertTrue(rutinas[0].esDelPlan)
        assertNull(rutinas[1].origen)
        assertNull(rutinas[2].origen)
        assertFalse(rutinas[2].esDelPlan)
    }

    @Test
    fun `el filtro quita solo las rutinas del plan`() {
        assertEquals(listOf(10, 12), lista.sinLasDelPlan().map { it.id })
    }

    @Test
    fun `la pantalla de Rutinas no muestra las del plan`() = runTest(main.dispatcher) {
        val vm = RutinasViewModel(RutinasFalso(lista))
        advanceUntilIdle()
        assertEquals(listOf("Torso", "Full body"), vm.estado.value.rutinas.map { it.nombre })
    }

    @Test
    fun `el selector del plan semanal no ofrece las del plan`() = runTest(main.dispatcher) {
        val vm = PlanSemanalViewModel(PlanFalso(), RutinasFalso(lista))
        advanceUntilIdle()
        assertEquals(listOf(10, 12), vm.estado.value.rutinas.map { it.id })
    }
}
