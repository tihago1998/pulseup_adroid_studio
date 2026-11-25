package com.example.pulseup.data.repository

import com.example.pulseup.data.mapper.aDominio
import com.example.pulseup.data.mapper.cuerpoEdicion
import com.example.pulseup.data.remote.PlanApi
import com.example.pulseup.data.remote.dto.AgregarEjercicioPlanRequest
import com.example.pulseup.data.remote.dto.AsignarDiaRequest
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
import com.example.pulseup.domain.repository.PlanRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class PlanRepositoryImpl(
    private val api: PlanApi,
    /** Para armar la URL completa de las fotos de los ejercicios. */
    private val baseUrl: String,
    private val token: () -> String
) : PlanRepository {

    override suspend fun obtenerPlan(): Resultado<List<DiaPlan>> = ejecutar(
        llamada = { api.getPlan(token()) },
        convertir = { cuerpo -> cuerpo.plan.orEmpty().map { it.aDominio(baseUrl) }.sortedBy { it.diaSemana } }
    )

    override suspend fun asignar(diaSemana: Int, idRutina: Int): Resultado<DiaAsignado> = ejecutar(
        llamada = { api.asignarDia(token(), diaSemana, AsignarDiaRequest(idRutina)) },
        convertir = { it.aDominio(baseUrl) }
    )

    override suspend fun quitar(diaSemana: Int): Resultado<Unit> = ejecutar(
        llamada = { api.quitarDia(token(), diaSemana) },
        convertir = { }
    )

    override suspend fun completarHoy(): Resultado<SesionCumplida> = ejecutar(
        llamada = { api.completarHoy(token()) },
        convertir = { it.aDominio() }
    )

    override suspend fun calendario(mes: String?): Resultado<CalendarioMes> = ejecutar(
        llamada = { api.getCalendario(token(), mes) },
        convertir = { it.aDominio() }
    )

    override suspend fun previsualizarEjercicio(ejercicioId: Int): Resultado<List<DiaPrevisualizado>> = ejecutar(
        llamada = { api.previsualizar(token(), ejercicioId = ejercicioId) },
        convertir = { it.aDominio() }
    )

    override suspend fun previsualizarRutina(rutinaId: Int): Resultado<List<DiaPrevisualizado>> = ejecutar(
        llamada = { api.previsualizar(token(), rutinaId = rutinaId) },
        convertir = { it.aDominio() }
    )

    override suspend fun agregarEjercicio(diaSemana: Int, ejercicioId: Int): Resultado<EjercicioAgregado> = ejecutar(
        llamada = { api.agregarEjercicio(token(), diaSemana, AgregarEjercicioPlanRequest(ejercicioId)) },
        convertir = { it.aDominio(baseUrl) }
    )

    override suspend fun editarEjercicio(
        diaSemana: Int,
        itemId: Int,
        cambios: CambiosEjercicioPlan
    ): Resultado<EjercicioEditado> = ejecutar(
        llamada = {
            val cuerpo = cambios.cuerpoEdicion().toRequestBody(JSON)
            api.editarEjercicio(token(), diaSemana, itemId, cuerpo)
        },
        convertir = { it.aDominio(baseUrl) }
    )

    override suspend fun quitarEjercicio(diaSemana: Int, itemId: Int): Resultado<EjercicioQuitado> = ejecutar(
        llamada = { api.quitarEjercicio(token(), diaSemana, itemId) },
        convertir = { it.aDominio(baseUrl) }
    )

    private companion object {
        val JSON = "application/json; charset=utf-8".toMediaType()
    }
}
