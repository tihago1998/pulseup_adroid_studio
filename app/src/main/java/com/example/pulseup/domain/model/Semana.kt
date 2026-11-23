package com.example.pulseup.domain.model

import java.time.LocalDate
import java.time.YearMonth

// Lógica pura de la semana actual (lunes a domingo) a partir de los días del calendario.

/** Rutinas hechas frente a las planificadas en la semana. */
data class ProgresoSemana(val hechas: Int, val planificadas: Int) {
    /** true si la semana no tiene ninguna rutina asignada. */
    val sinPlan: Boolean get() = planificadas == 0

    /** De 0 a 1, para la barra de progreso. */
    val fraccion: Float get() = if (planificadas == 0) 0f else hechas.toFloat() / planificadas
}

/** Un día de la tira de la semana del Home. */
data class DiaDeSemana(
    val fecha: LocalDate,
    val estado: EstadoDia,
    val esHoy: Boolean
)

/** Lunes de la semana que contiene [fecha] (las semanas empiezan en lunes). */
fun lunesDe(fecha: LocalDate): LocalDate = fecha.minusDays((fecha.dayOfWeek.value - 1).toLong())

/** Los 7 días, de lunes a domingo, de la semana que contiene [hoy]. */
fun diasDeLaSemana(hoy: LocalDate): List<LocalDate> {
    val lunes = lunesDe(hoy)
    return (0L until 7L).map { lunes.plusDays(it) }
}

/** Meses que hay que pedir al calendario para cubrir la semana: uno, o dos si la semana cruza de mes. */
fun mesesDeLaSemana(hoy: LocalDate): List<YearMonth> =
    diasDeLaSemana(hoy).map { YearMonth.from(it) }.distinct()

/**
 * Arma la tira L a D de la semana de [hoy] con el estado de cada día.
 * Un día que no venga en [dias] se muestra como descanso.
 */
fun tiraSemana(dias: List<DiaCalendario>, hoy: LocalDate): List<DiaDeSemana> {
    val porFecha = dias.associateBy { it.fecha }
    return diasDeLaSemana(hoy).map { fecha ->
        DiaDeSemana(
            fecha = fecha,
            estado = porFecha[fecha.toString()]?.estado ?: EstadoDia.DESCANSO,
            esHoy = fecha == hoy
        )
    }
}

/**
 * Progreso de la semana (lunes a domingo) que contiene [hoy].
 * hechas = días CUMPLIDO; planificadas = CUMPLIDO + PENDIENTE + INCUMPLIDO. Los DESCANSO no cuentan.
 * [dias] puede traer días de otras semanas (el mes completo, o dos meses juntos): se ignoran.
 */
fun progresoSemana(dias: List<DiaCalendario>, hoy: LocalDate): ProgresoSemana {
    val estados = tiraSemana(dias, hoy).map { it.estado }
    return ProgresoSemana(
        hechas = estados.count { it == EstadoDia.CUMPLIDO },
        planificadas = estados.count { it != EstadoDia.DESCANSO }
    )
}
