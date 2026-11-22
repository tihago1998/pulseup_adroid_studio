package com.example.pulseup.domain.model

/** Rutina asignada a un día del plan (versión resumida). */
data class RutinaPlan(
    val id: Int,
    val nombre: String,
    val gruposMusculares: List<String>
)

/** Un día de la semana del plan: lunes = 1 … domingo = 7. Sin rutina = día de descanso. */
data class DiaPlan(
    val diaSemana: Int,
    val nombreDia: String,
    val rutina: RutinaPlan?
)

enum class EstadoDia { CUMPLIDO, PENDIENTE, INCUMPLIDO, DESCANSO }

data class DiaCalendario(
    val fecha: String, // YYYY-MM-DD
    val diaSemana: Int,
    val estado: EstadoDia,
    val rutina: RutinaPlan?
)

data class Racha(val actual: Int, val mejor: Int)

data class CalendarioMes(
    val mes: String, // YYYY-MM
    val dias: List<DiaCalendario>,
    val racha: Racha
)

data class SesionCumplida(
    val fecha: String,
    val nombreDia: String,
    val nombreRutina: String,
    val mensaje: String
)
