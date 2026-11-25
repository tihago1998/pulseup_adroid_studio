package com.example.pulseup.domain.repository

import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.CalendarioMes
import com.example.pulseup.domain.model.CambiosEjercicioPlan
import com.example.pulseup.domain.model.DiaAsignado
import com.example.pulseup.domain.model.DiaPlan
import com.example.pulseup.domain.model.DiaPrevisualizado
import com.example.pulseup.domain.model.EjercicioAgregado
import com.example.pulseup.domain.model.EjercicioEditado
import com.example.pulseup.domain.model.EjercicioQuitado
import com.example.pulseup.domain.model.SesionCumplida

interface PlanRepository {
    /** Los 7 días, cada uno con su rutina y sus ejercicios. */
    suspend fun obtenerPlan(): Resultado<List<DiaPlan>>

    /** Asigna la rutina aunque choque con la regla de 48 h: los choques llegan como avisos. */
    suspend fun asignar(diaSemana: Int, idRutina: Int): Resultado<DiaAsignado>

    suspend fun quitar(diaSemana: Int): Resultado<Unit>

    /** Marca como cumplida la rutina de HOY (solo el día que toca). */
    suspend fun completarHoy(): Resultado<SesionCumplida>

    /** [mes] en formato YYYY-MM; null = mes actual. */
    suspend fun calendario(mes: String? = null): Resultado<CalendarioMes>

    /** Avisos de 48 h de cada día si se le agregara este ejercicio. */
    suspend fun previsualizarEjercicio(ejercicioId: Int): Resultado<List<DiaPrevisualizado>>

    /** Avisos de 48 h de cada día si se le asignara esta rutina. */
    suspend fun previsualizarRutina(rutinaId: Int): Resultado<List<DiaPrevisualizado>>

    /**
     * Agrega un ejercicio del catálogo al día. Sin series ni peso: el backend los rellena con la
     * recomendación. Si ya está ese día falla con código 409.
     */
    suspend fun agregarEjercicio(diaSemana: Int, ejercicioId: Int): Resultado<EjercicioAgregado>

    suspend fun editarEjercicio(diaSemana: Int, itemId: Int, cambios: CambiosEjercicioPlan): Resultado<EjercicioEditado>

    suspend fun quitarEjercicio(diaSemana: Int, itemId: Int): Resultado<EjercicioQuitado>
}
