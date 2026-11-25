package com.example.pulseup.presentation.ejercicios

import com.example.pulseup.domain.model.RecomendacionEjercicio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FormatoRecomendacionTest {

    private val aviso = "Es una guía general y no reemplaza a un entrenador ni a un médico."

    private fun rec(
        unidad: String = "repeticiones",
        repsMin: Int = 6, repsMax: Int = 12,
        seriesMin: Int = 3, seriesMax: Int = 4,
        descansoSeg: Int? = 150,
        rir: String? = null, diasSemana: String? = null, rpe: String? = null,
        pesoKg: Double? = null, nota: String? = null
    ) = RecomendacionEjercicio(
        "intermedio", unidad, repsMin, repsMax, seriesMin, seriesMax,
        descansoSeg, rir, diasSemana, rpe, pesoKg, nota, aviso
    )

    @Test
    fun `repeticiones`() {
        assertEquals("3–4 series × 6–12 repeticiones", textoPrincipal(rec()))
    }

    @Test
    fun `segundos`() {
        assertEquals("3 series × 30–45 s", textoPrincipal(rec("segundos", 30, 45, 3, 3)))
    }

    @Test
    fun `minutos de cardio sin series con dias y esfuerzo`() {
        val r = rec("minutos", 30, 40, 1, 1, descansoSeg = null, diasSemana = "3-4", rpe = "5-6")
        assertEquals("30–40 min · 3–4 días por semana · esfuerzo 5–6 de 10", textoPrincipal(r))
    }

    @Test
    fun `minutos sin dias ni esfuerzo muestra solo la duracion`() {
        assertEquals("20–30 min", textoPrincipal(rec("minutos", 20, 30, 1, 1)))
    }

    @Test
    fun `un dia por semana va en singular`() {
        assertEquals("20 min · 1 día por semana", textoPrincipal(rec("minutos", 20, 20, 1, 1, diasSemana = "1")))
    }

    @Test
    fun `una sola serie va en singular`() {
        assertEquals("1 serie × 10 repeticiones", textoPrincipal(rec(repsMin = 10, repsMax = 10, seriesMin = 1, seriesMax = 1)))
    }

    @Test
    fun `unidad desconocida se muestra como repeticiones`() {
        assertEquals("3–4 series × 6–12 repeticiones", textoPrincipal(rec(unidad = "otra")))
    }

    @Test
    fun `descanso solo si viene`() {
        assertEquals("Descanso entre series: 2 min 30 s", textosRecomendacion(rec(descansoSeg = 150)).descanso)
        assertEquals("Descanso entre series: 45 s", textosRecomendacion(rec(descansoSeg = 45)).descanso)
        assertEquals("Descanso entre series: 1 min", textosRecomendacion(rec(descansoSeg = 60)).descanso)
        assertNull(textosRecomendacion(rec(descansoSeg = null)).descanso)
    }

    @Test
    fun `rir como repeticiones en reserva`() {
        assertEquals("Deja 2–3 repeticiones en reserva", textosRecomendacion(rec(rir = "2-3")).reserva)
        assertNull(textosRecomendacion(rec(rir = null)).reserva)
    }

    @Test
    fun `peso aproximado con coma decimal solo si viene`() {
        assertEquals("≈ 72,5 kg", textosRecomendacion(rec(pesoKg = 72.5)).peso)
        assertEquals("≈ 70 kg", textosRecomendacion(rec(pesoKg = 70.0)).peso)
        assertNull(textosRecomendacion(rec(pesoKg = null)).peso)
    }

    // ---------- Ejercicios del plan ----------

    @Test
    fun `plan con repeticiones y peso`() {
        assertEquals("3 × 10 · 40 kg", textoSeriesPlan(3, "10", "repeticiones", 40.0))
        assertEquals("4 × 9 · 72,5 kg", textoSeriesPlan(4, "9", "repeticiones", 72.5))
    }

    @Test
    fun `plan con rango de repeticiones y sin peso`() {
        assertEquals("3 × 8–10", textoSeriesPlan(3, "8-10", "repeticiones", null))
    }

    @Test
    fun `plan en segundos`() {
        assertEquals("3 × 40 s", textoSeriesPlan(3, "40", "segundos", null))
    }

    @Test
    fun `plan en minutos sin series ni peso`() {
        assertEquals("35 min", textoSeriesPlan(1, "35", "minutos", null))
    }

    @Test
    fun `plan con unidad desconocida como repeticiones`() {
        assertEquals("3 × 12", textoSeriesPlan(3, "12", "otra", null))
    }

    @Test
    fun `descanso del plan oculto en cardio`() {
        assertNull(textoDescansoPlan(0))
        assertEquals("Descanso 1 min 15 s", textoDescansoPlan(75))
        assertEquals("Descanso 45 s", textoDescansoPlan(45))
    }

    @Test
    fun `titulo nota y aviso`() {
        val t = textosRecomendacion(rec(nota = "Sube poco a poco."))
        assertEquals("Recomendado para ti · nivel intermedio", t.titulo)
        assertEquals("Sube poco a poco.", t.nota)
        assertEquals(aviso, t.aviso)
        assertNull(textosRecomendacion(rec(nota = null)).nota)
    }
}
