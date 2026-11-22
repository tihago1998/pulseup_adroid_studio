package com.example.pulseup.data.mapper

import com.example.pulseup.data.remote.dto.CalendarioResponse
import com.example.pulseup.data.remote.dto.CompletarResponse
import com.example.pulseup.data.remote.dto.DiaCalendarioDto
import com.example.pulseup.data.remote.dto.DiaPlanDto
import com.example.pulseup.data.remote.dto.RachaDto
import com.example.pulseup.data.remote.dto.RutinaPlanDto
import com.example.pulseup.data.remote.dto.SesionDto
import com.example.pulseup.domain.model.EstadoDia
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlanMapperTest {

    @Test
    fun `dia sin rutina es descanso`() {
        val dia = DiaPlanDto(1, "lunes", null).aDominio()
        assertEquals(1, dia.diaSemana)
        assertNull(dia.rutina)
    }

    @Test
    fun `dia con rutina conserva los grupos musculares`() {
        val dia = DiaPlanDto(7, "domingo", RutinaPlanDto(9, "Pecho", listOf("pecho"))).aDominio()
        assertEquals("Pecho", dia.rutina?.nombre)
        assertEquals(listOf("pecho"), dia.rutina?.gruposMusculares)
    }

    @Test
    fun `los cuatro estados del backend se reconocen y uno desconocido es descanso`() {
        assertEquals(EstadoDia.CUMPLIDO, estadoDeTexto("cumplido"))
        assertEquals(EstadoDia.PENDIENTE, estadoDeTexto("pendiente"))
        assertEquals(EstadoDia.INCUMPLIDO, estadoDeTexto("incumplido"))
        assertEquals(EstadoDia.DESCANSO, estadoDeTexto("descanso"))
        assertEquals(EstadoDia.DESCANSO, estadoDeTexto("otro"))
        assertEquals(EstadoDia.DESCANSO, estadoDeTexto(null))
    }

    @Test
    fun `calendario con campos nulos usa valores por defecto`() {
        val c = CalendarioResponse(dias = null, mes = null, racha = null).aDominio()
        assertEquals(0, c.dias.size)
        assertEquals(0, c.racha.actual)
    }

    @Test
    fun `calendario real del backend se lee completo`() {
        val json = """{"dias":[{"dia_semana":7,"estado":"cumplido","fecha":"2026-11-22","rutina":{"id":9,"nombre":"Pecho"}},{"dia_semana":1,"estado":"descanso","fecha":"2026-11-23","rutina":null}],"mes":"2026-11","racha":{"actual":1,"mejor":1}}"""
        val c = Gson().fromJson(json, CalendarioResponse::class.java).aDominio()
        assertEquals("2026-11", c.mes)
        assertEquals(EstadoDia.CUMPLIDO, c.dias[0].estado)
        assertEquals("Pecho", c.dias[0].rutina?.nombre)
        assertNull(c.dias[1].rutina)
        assertEquals(1, c.racha.mejor)
    }

    @Test
    fun `respuesta de completar real se lee`() {
        val json = """{"message":"¡Rutina de hoy cumplida!","sesion":{"completada_en":"2026-11-22T18:13:56-05:00","dia_semana":7,"fecha":"2026-11-22","nombre_dia":"domingo","rutina":{"id":9,"nombre":"Pecho"}}}"""
        val s = Gson().fromJson(json, CompletarResponse::class.java).aDominio()
        assertEquals("Pecho", s.nombreRutina)
        assertEquals("2026-11-22", s.fecha)
    }
}
