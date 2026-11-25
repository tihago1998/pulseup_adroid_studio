package com.example.pulseup.data.repository

import com.example.pulseup.data.mapper.aDominio
import com.example.pulseup.data.mapper.aRequest
import com.example.pulseup.data.remote.EntrenamientoApi
import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.NuevaRutina
import com.example.pulseup.domain.model.Rutina
import com.example.pulseup.domain.repository.RutinasRepository

class RutinasRepositoryImpl(
    private val api: EntrenamientoApi,
    private val token: () -> String
) : RutinasRepository {

    override suspend fun listar(objetivo: String?, nivel: String?): Resultado<List<Rutina>> = ejecutar(
        llamada = { api.getRutinas(token(), objetivo, nivel) },
        convertir = { cuerpo -> cuerpo.rutinas.orEmpty().map { it.aDominio() } }
    )

    override suspend fun detalle(id: Int): Resultado<Rutina> = ejecutar(
        llamada = { api.getRutina(token(), id) },
        convertir = { cuerpo -> cuerpo.rutina?.aDominio() ?: throw IllegalStateException("Sin rutina") }
    )

    override suspend fun crear(nueva: NuevaRutina): Resultado<Int> = ejecutar(
        llamada = { api.crearRutina(token(), nueva.aRequest()) },
        convertir = { cuerpo -> cuerpo.id ?: 0 }
    )

    override suspend fun eliminar(id: Int): Resultado<Unit> = ejecutar(
        llamada = { api.eliminarRutina(token(), id) },
        convertir = { }
    )
}
