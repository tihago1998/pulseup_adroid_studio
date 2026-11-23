package com.example.pulseup.domain.repository

import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.CalendarioMes
import com.example.pulseup.domain.model.DiaPlan
import com.example.pulseup.domain.model.SesionCumplida

interface PlanRepository {
    suspend fun obtenerPlan(): Resultado<List<DiaPlan>>

    /** Falla con el mensaje del backend si rompe la regla de descanso de 48 h. */
    suspend fun asignar(diaSemana: Int, idRutina: Int): Resultado<DiaPlan>

    suspend fun quitar(diaSemana: Int): Resultado<Unit>

    /** Marca como cumplida la rutina de HOY (solo el día que toca). */
    suspend fun completarHoy(): Resultado<SesionCumplida>

    /** [mes] en formato YYYY-MM; null = mes actual. */
    suspend fun calendario(mes: String? = null): Resultado<CalendarioMes>
}
