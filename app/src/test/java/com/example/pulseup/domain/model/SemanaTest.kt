package com.example.pulseup.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class SemanaTest {

    private val rutina = RutinaPlan(1, "Pierna", listOf("piernas"))

    private fun dia(fecha: String, estado: EstadoDia) = DiaCalendario(
        fecha = fecha,
        diaSemana = LocalDate.parse(fecha).dayOfWeek.value,
        estado = estado,
        rutina = if (estado == EstadoDia.DESCANSO) null else rutina
    )

    // La semana del 23 al 29 de noviembre de 2026 va de lunes a domingo
    private val miercoles = LocalDate.of(2026, 11, 25)

    // ---------- progresoSemana ----------

    @Test
    fun `semana sin plan da 0 de 0`() {
        val dias = (23..29).map { dia("2026-11-$it", EstadoDia.DESCANSO) }

        val progreso = progresoSemana(dias, miercoles)

        assertEquals(ProgresoSemana(0, 0), progreso)
        assertTrue(progreso.sinPlan)
        assertEquals(0f, progreso.fraccion)
    }

    @Test
    fun `sin datos del calendario tambien es semana sin plan`() {
        assertTrue(progresoSemana(emptyList(), miercoles).sinPlan)
    }

    @Test
    fun `tres rutinas con dos cumplidas da 2 de 3`() {
        val dias = listOf(
            dia("2026-11-23", EstadoDia.CUMPLIDO),
            dia("2026-11-24", EstadoDia.DESCANSO),
            dia("2026-11-25", EstadoDia.CUMPLIDO),
            dia("2026-11-26", EstadoDia.DESCANSO),
            dia("2026-11-27", EstadoDia.PENDIENTE),
            dia("2026-11-28", EstadoDia.DESCANSO),
            dia("2026-11-29", EstadoDia.DESCANSO)
        )

        val progreso = progresoSemana(dias, miercoles)

        assertEquals(ProgresoSemana(hechas = 2, planificadas = 3), progreso)
        assertFalse(progreso.sinPlan)
    }

    @Test
    fun `los incumplidos cuentan como planificados pero no como hechos`() {
        val dias = listOf(
            dia("2026-11-23", EstadoDia.INCUMPLIDO),
            dia("2026-11-25", EstadoDia.CUMPLIDO)
        )
        assertEquals(ProgresoSemana(hechas = 1, planificadas = 2), progresoSemana(dias, miercoles))
    }

    @Test
    fun `los dias de descanso no cuentan`() {
        val dias = listOf(
            dia("2026-11-23", EstadoDia.CUMPLIDO),
            dia("2026-11-24", EstadoDia.DESCANSO),
            dia("2026-11-25", EstadoDia.DESCANSO),
            dia("2026-11-26", EstadoDia.DESCANSO)
        )
        assertEquals(ProgresoSemana(hechas = 1, planificadas = 1), progresoSemana(dias, miercoles))
    }

    @Test
    fun `semana que cruza dos meses junta los dias de ambos`() {
        // Semana del lunes 30 de noviembre al domingo 6 de diciembre de 2026
        val hoy = LocalDate.of(2026, 12, 2)
        val noviembre = listOf(
            dia("2026-11-27", EstadoDia.CUMPLIDO), // semana anterior: no cuenta
            dia("2026-11-30", EstadoDia.CUMPLIDO)
        )
        val diciembre = listOf(
            dia("2026-12-02", EstadoDia.PENDIENTE),
            dia("2026-12-04", EstadoDia.PENDIENTE),
            dia("2026-12-07", EstadoDia.PENDIENTE) // semana siguiente: no cuenta
        )

        assertEquals(
            listOf(YearMonth.of(2026, 11), YearMonth.of(2026, 12)),
            mesesDeLaSemana(hoy)
        )
        assertEquals(ProgresoSemana(hechas = 1, planificadas = 3), progresoSemana(noviembre + diciembre, hoy))
    }

    @Test
    fun `semana dentro de un solo mes pide un solo mes`() {
        assertEquals(listOf(YearMonth.of(2026, 11)), mesesDeLaSemana(miercoles))
    }

    @Test
    fun `el domingo cierra la semana que empezo el lunes anterior`() {
        val domingo = LocalDate.of(2026, 11, 29)
        assertEquals(DayOfWeek.SUNDAY, domingo.dayOfWeek)
        val dias = listOf(
            dia("2026-11-22", EstadoDia.CUMPLIDO), // domingo anterior: otra semana
            dia("2026-11-23", EstadoDia.CUMPLIDO), // lunes de esta semana
            dia("2026-11-29", EstadoDia.PENDIENTE), // hoy, domingo
            dia("2026-11-30", EstadoDia.PENDIENTE)  // lunes siguiente: otra semana
        )

        assertEquals(ProgresoSemana(hechas = 1, planificadas = 2), progresoSemana(dias, domingo))
    }

    @Test
    fun `el lunes abre una semana nueva`() {
        val lunes = LocalDate.of(2026, 11, 23)
        assertEquals(DayOfWeek.MONDAY, lunes.dayOfWeek)
        val dias = listOf(
            dia("2026-11-22", EstadoDia.CUMPLIDO), // domingo anterior: otra semana
            dia("2026-11-23", EstadoDia.PENDIENTE),
            dia("2026-11-29", EstadoDia.PENDIENTE)
        )

        assertEquals(ProgresoSemana(hechas = 0, planificadas = 2), progresoSemana(dias, lunes))
    }

    // ---------- tiraSemana ----------

    @Test
    fun `la tira tiene 7 dias de lunes a domingo`() {
        val tira = tiraSemana(emptyList(), miercoles)

        assertEquals(7, tira.size)
        assertEquals(LocalDate.of(2026, 11, 23), tira.first().fecha)
        assertEquals(LocalDate.of(2026, 11, 29), tira.last().fecha)
        assertEquals(DayOfWeek.MONDAY, tira.first().fecha.dayOfWeek)
        assertEquals(DayOfWeek.SUNDAY, tira.last().fecha.dayOfWeek)
    }

    @Test
    fun `la tira marca solo el dia de hoy`() {
        val tira = tiraSemana(emptyList(), miercoles)

        assertEquals(listOf(false, false, true, false, false, false, false), tira.map { it.esHoy })
    }

    @Test
    fun `la tira copia el estado de cada dia y deja en descanso los que faltan`() {
        val dias = listOf(
            dia("2026-11-23", EstadoDia.CUMPLIDO),
            dia("2026-11-24", EstadoDia.INCUMPLIDO),
            dia("2026-11-25", EstadoDia.PENDIENTE)
        )

        val estados = tiraSemana(dias, miercoles).map { it.estado }

        assertEquals(
            listOf(
                EstadoDia.CUMPLIDO, EstadoDia.INCUMPLIDO, EstadoDia.PENDIENTE,
                EstadoDia.DESCANSO, EstadoDia.DESCANSO, EstadoDia.DESCANSO, EstadoDia.DESCANSO
            ),
            estados
        )
    }

    @Test
    fun `la tira de una semana que cruza dos meses usa los dias de ambos`() {
        val hoy = LocalDate.of(2026, 12, 2)
        val dias = listOf(dia("2026-11-30", EstadoDia.CUMPLIDO), dia("2026-12-06", EstadoDia.PENDIENTE))

        val tira = tiraSemana(dias, hoy)

        assertEquals(LocalDate.of(2026, 11, 30), tira.first().fecha)
        assertEquals(EstadoDia.CUMPLIDO, tira.first().estado)
        assertEquals(EstadoDia.PENDIENTE, tira.last().estado)
    }
}
