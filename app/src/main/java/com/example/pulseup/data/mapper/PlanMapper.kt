package com.example.pulseup.data.mapper

import com.example.pulseup.data.remote.dto.CalendarioResponse
import com.example.pulseup.data.remote.dto.CompletarResponse
import com.example.pulseup.data.remote.dto.DiaCalendarioDto
import com.example.pulseup.data.remote.dto.DiaPlanDto
import com.example.pulseup.data.remote.dto.RutinaPlanDto
import com.example.pulseup.domain.model.CalendarioMes
import com.example.pulseup.domain.model.DiaCalendario
import com.example.pulseup.domain.model.DiaPlan
import com.example.pulseup.domain.model.EstadoDia
import com.example.pulseup.domain.model.Racha
import com.example.pulseup.domain.model.RutinaPlan
import com.example.pulseup.domain.model.SesionCumplida

fun RutinaPlanDto.aDominio() = RutinaPlan(
    id = id ?: 0,
    nombre = nombre.orEmpty(),
    gruposMusculares = gruposMusculares.orEmpty()
)

fun DiaPlanDto.aDominio() = DiaPlan(
    diaSemana = diaSemana ?: 0,
    nombreDia = nombreDia.orEmpty(),
    rutina = rutina?.aDominio()
)

/** Un estado desconocido (backend más nuevo que la app) se trata como descanso. */
fun estadoDeTexto(texto: String?): EstadoDia = when (texto) {
    "cumplido" -> EstadoDia.CUMPLIDO
    "pendiente" -> EstadoDia.PENDIENTE
    "incumplido" -> EstadoDia.INCUMPLIDO
    else -> EstadoDia.DESCANSO
}

fun DiaCalendarioDto.aDominio() = DiaCalendario(
    fecha = fecha.orEmpty(),
    diaSemana = diaSemana ?: 0,
    estado = estadoDeTexto(estado),
    rutina = rutina?.aDominio()
)

fun CalendarioResponse.aDominio() = CalendarioMes(
    mes = mes.orEmpty(),
    dias = dias.orEmpty().map { it.aDominio() },
    racha = Racha(actual = racha?.actual ?: 0, mejor = racha?.mejor ?: 0)
)

fun CompletarResponse.aDominio() = SesionCumplida(
    fecha = sesion?.fecha.orEmpty(),
    nombreDia = sesion?.nombreDia.orEmpty(),
    nombreRutina = sesion?.rutina?.nombre.orEmpty(),
    mensaje = message ?: "¡Rutina de hoy cumplida!"
)
