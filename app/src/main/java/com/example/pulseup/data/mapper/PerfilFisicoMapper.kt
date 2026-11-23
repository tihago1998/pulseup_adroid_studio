package com.example.pulseup.data.mapper

import com.example.pulseup.data.remote.dto.PerfilFisicoDto
import com.example.pulseup.data.remote.dto.PerfilFisicoRequest
import com.example.pulseup.data.remote.dto.PerfilFisicoResponse
import com.example.pulseup.domain.model.NuevoPerfilFisico
import com.example.pulseup.domain.model.PerfilFisico
import com.example.pulseup.domain.model.ValoresPerfilFisico

fun PerfilFisicoDto.aDominio() = PerfilFisico(
    fechaNacimiento = fechaNacimiento.orEmpty(),
    edad = edad ?: 0,
    sexo = sexo ?: ValoresPerfilFisico.SEXO_POR_DEFECTO,
    pesoKg = pesoKg ?: 0.0,
    estaturaCm = estaturaCm ?: 0.0,
    nivel = nivel.orEmpty(),
    actualizadoEn = actualizadoEn.orEmpty()
)

/** null = el usuario aún no tiene perfil ({"perfil": null}). */
fun PerfilFisicoResponse.perfilODominio(): PerfilFisico? = perfil?.aDominio()

fun NuevoPerfilFisico.aRequest() = PerfilFisicoRequest(
    fechaNacimiento = fechaNacimiento,
    sexo = sexo,
    pesoKg = pesoKg,
    estaturaCm = estaturaCm,
    nivel = nivel
)
