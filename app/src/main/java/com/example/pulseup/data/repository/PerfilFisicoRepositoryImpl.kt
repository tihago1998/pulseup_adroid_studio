package com.example.pulseup.data.repository

import com.example.pulseup.data.mapper.aRequest
import com.example.pulseup.data.mapper.perfilODominio
import com.example.pulseup.data.remote.PerfilFisicoApi
import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.NuevoPerfilFisico
import com.example.pulseup.domain.model.PerfilFisico
import com.example.pulseup.domain.repository.PerfilFisicoRepository

class PerfilFisicoRepositoryImpl(
    private val api: PerfilFisicoApi,
    private val token: () -> String
) : PerfilFisicoRepository {

    override suspend fun obtener(): Resultado<PerfilFisico?> = ejecutar(
        llamada = { api.getPerfil(token()) },
        convertir = { it.perfilODominio() }
    )

    override suspend fun guardar(nuevo: NuevoPerfilFisico): Resultado<PerfilFisico> = ejecutar(
        llamada = { api.guardarPerfil(token(), nuevo.aRequest()) },
        convertir = { cuerpo -> cuerpo.perfilODominio() ?: throw IllegalStateException("Sin perfil") }
    )

    override suspend fun eliminar(): Resultado<Unit> = ejecutar(
        llamada = { api.eliminarPerfil(token()) },
        convertir = { }
    )
}
