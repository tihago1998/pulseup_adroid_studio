package com.example.pulseup.data.repository

import com.example.pulseup.data.mapper.aDominio
import com.example.pulseup.data.mapper.aPagina
import com.example.pulseup.data.remote.EntrenamientoApi
import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.CreditosCatalogo
import com.example.pulseup.domain.model.Ejercicio
import com.example.pulseup.domain.model.EjercicioDetalle
import com.example.pulseup.domain.model.FiltroCatalogo
import com.example.pulseup.domain.model.FiltrosCatalogo
import com.example.pulseup.domain.model.PaginaEjercicios
import com.example.pulseup.domain.model.RecomendacionEjercicio
import com.example.pulseup.domain.repository.EjerciciosRepository

/** [baseUrl] es la URL del backend: completa las rutas relativas de las fotos. */
class EjerciciosRepositoryImpl(
    private val api: EntrenamientoApi,
    private val baseUrl: String,
    private val token: () -> String
) : EjerciciosRepository {

    override suspend fun listar(
        musculo: String?,
        equipo: String?,
        nivel: String?,
        texto: String?
    ): Resultado<List<Ejercicio>> = ejecutar(
        llamada = { api.getEjercicios(token(), musculo, equipo, nivel, texto?.trim()?.takeIf { it.isNotEmpty() }) },
        convertir = { cuerpo -> cuerpo.ejercicios.orEmpty().map { it.aDominio(baseUrl) } }
    )

    override suspend fun buscar(filtro: FiltroCatalogo, limit: Int, offset: Int): Resultado<PaginaEjercicios> = ejecutar(
        llamada = {
            api.buscarEjercicios(
                auth = token(),
                limit = limit,
                offset = offset,
                texto = filtro.texto.trim().takeIf { it.isNotEmpty() },
                ambiente = filtro.ambiente,
                equipo = filtro.equipo,
                musculo = filtro.musculo,
                nivel = filtro.nivel
            )
        },
        convertir = { it.aPagina(baseUrl) }
    )

    override suspend fun filtros(): Resultado<FiltrosCatalogo> = ejecutar(
        llamada = { api.getFiltros(token()) },
        convertir = { it.aDominio() }
    )

    override suspend fun detalle(id: Int): Resultado<EjercicioDetalle> = ejecutar(
        llamada = { api.getEjercicio(token(), id) },
        convertir = { cuerpo -> cuerpo.ejercicio?.aDominio(baseUrl) ?: throw IllegalStateException("Sin ejercicio") }
    )

    override suspend fun recomendacion(id: Int): Resultado<RecomendacionEjercicio?> = ejecutar(
        llamada = { api.getRecomendacion(token(), id) },
        convertir = { it.recomendacion?.aDominio() }
    )

    override suspend fun creditos(): Resultado<CreditosCatalogo> = ejecutar(
        llamada = { api.getCreditos(token()) },
        convertir = { it.aDominio() }
    )
}
