package com.example.pulseup.data.repository

import com.example.pulseup.data.mapper.aDominio
import com.example.pulseup.data.remote.PlanApi
import com.example.pulseup.data.remote.dto.AsignarDiaRequest
import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.CalendarioMes
import com.example.pulseup.domain.model.DiaPlan
import com.example.pulseup.domain.model.SesionCumplida
import com.example.pulseup.domain.repository.PlanRepository

class PlanRepositoryImpl(
    private val api: PlanApi,
    private val token: () -> String
) : PlanRepository {

    override suspend fun obtenerPlan(): Resultado<List<DiaPlan>> = ejecutar(
        llamada = { api.getPlan(token()) },
        convertir = { cuerpo -> cuerpo.plan.orEmpty().map { it.aDominio() }.sortedBy { it.diaSemana } }
    )

    override suspend fun asignar(diaSemana: Int, idRutina: Int): Resultado<DiaPlan> = ejecutar(
        llamada = { api.asignarDia(token(), diaSemana, AsignarDiaRequest(idRutina)) },
        convertir = { cuerpo -> cuerpo.dia?.aDominio() ?: throw IllegalStateException("Sin día") }
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
}
