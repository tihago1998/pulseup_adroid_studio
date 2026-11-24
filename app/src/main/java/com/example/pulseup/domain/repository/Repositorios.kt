package com.example.pulseup.domain.repository

import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.CreditosCatalogo
import com.example.pulseup.domain.model.Ejercicio
import com.example.pulseup.domain.model.EjercicioDetalle
import com.example.pulseup.domain.model.FiltroCatalogo
import com.example.pulseup.domain.model.FiltrosCatalogo
import com.example.pulseup.domain.model.NuevaRutina
import com.example.pulseup.domain.model.PaginaEjercicios
import com.example.pulseup.domain.model.RecomendacionEjercicio
import com.example.pulseup.domain.model.Rutina

interface EjerciciosRepository {
    /** Todos los ejercicios sin paginar (lo usa el selector al crear una rutina). */
    suspend fun listar(
        musculo: String? = null,
        equipo: String? = null,
        nivel: String? = null,
        texto: String? = null
    ): Resultado<List<Ejercicio>>

    /** Una página del catálogo. */
    suspend fun buscar(filtro: FiltroCatalogo, limit: Int, offset: Int): Resultado<PaginaEjercicios>

    suspend fun filtros(): Resultado<FiltrosCatalogo>

    /** Detalle con pasos, fotos y la recomendación para el perfil físico del usuario. */
    suspend fun detalle(id: Int): Resultado<EjercicioDetalle>

    /** Éxito con null = sin recomendación; el motivo viene en el detalle. Se usará en la Fase 2. */
    suspend fun recomendacion(id: Int): Resultado<RecomendacionEjercicio?>

    suspend fun creditos(): Resultado<CreditosCatalogo>
}

interface RutinasRepository {
    suspend fun listar(objetivo: String? = null, nivel: String? = null): Resultado<List<Rutina>>
    suspend fun detalle(id: Int): Resultado<Rutina>
    suspend fun crear(nueva: NuevaRutina): Resultado<Int>
    suspend fun eliminar(id: Int): Resultado<Unit>
}
