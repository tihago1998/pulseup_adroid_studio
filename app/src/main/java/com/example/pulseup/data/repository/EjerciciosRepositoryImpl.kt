package com.example.pulseup.data.repository

import com.example.pulseup.data.mapper.aDominio
import com.example.pulseup.data.remote.EntrenamientoApi
import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.Ejercicio
import com.example.pulseup.domain.model.FiltrosEjercicio
import com.example.pulseup.domain.repository.EjerciciosRepository

class EjerciciosRepositoryImpl(
    private val api: EntrenamientoApi,
    private val token: () -> String
) : EjerciciosRepository {

    override suspend fun listar(
        musculo: String?,
        equipo: String?,
        nivel: String?,
        texto: String?
    ): Resultado<List<Ejercicio>> = ejecutar(
        llamada = { api.getEjercicios(token(), musculo, equipo, nivel, texto?.trim()?.takeIf { it.isNotEmpty() }) },
        convertir = { cuerpo -> cuerpo.ejercicios.orEmpty().map { it.aDominio() } }
    )

    override suspend fun filtros(): Resultado<FiltrosEjercicio> = ejecutar(
        llamada = { api.getFiltros(token()) },
        convertir = { it.aDominio() }
    )

    override suspend fun detalle(id: Int): Resultado<Ejercicio> = ejecutar(
        llamada = { api.getEjercicio(token(), id) },
        convertir = { cuerpo -> cuerpo.ejercicio?.aDominio() ?: throw IllegalStateException("Sin ejercicio") }
    )
}
