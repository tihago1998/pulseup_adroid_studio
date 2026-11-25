package com.example.pulseup.data.mapper

import com.example.pulseup.data.remote.dto.AgregarEjercicioPlanResponse
import com.example.pulseup.data.remote.dto.AsignarDiaResponse
import com.example.pulseup.data.remote.dto.AvisoDto
import com.example.pulseup.data.remote.dto.CalendarioResponse
import com.example.pulseup.data.remote.dto.CompletarResponse
import com.example.pulseup.data.remote.dto.DiaCalendarioDto
import com.example.pulseup.data.remote.dto.DiaPlanDto
import com.example.pulseup.data.remote.dto.DiaPrevisualizadoDto
import com.example.pulseup.data.remote.dto.EditarEjercicioPlanRequest
import com.example.pulseup.data.remote.dto.EditarEjercicioPlanResponse
import com.example.pulseup.data.remote.dto.EjercicioPlanDto
import com.example.pulseup.data.remote.dto.PrevisualizarResponse
import com.example.pulseup.data.remote.dto.QuitarEjercicioPlanResponse
import com.example.pulseup.data.remote.dto.RutinaPlanDto
import com.example.pulseup.domain.model.AvisoDescanso
import com.example.pulseup.domain.model.CalendarioMes
import com.example.pulseup.domain.model.CambiosEjercicioPlan
import com.example.pulseup.domain.model.DiaAsignado
import com.example.pulseup.domain.model.DiaCalendario
import com.example.pulseup.domain.model.DiaPlan
import com.example.pulseup.domain.model.DiaPrevisualizado
import com.example.pulseup.domain.model.EjercicioAgregado
import com.example.pulseup.domain.model.EjercicioEditado
import com.example.pulseup.domain.model.EjercicioPlan
import com.example.pulseup.domain.model.EjercicioQuitado
import com.example.pulseup.domain.model.EstadoDia
import com.example.pulseup.domain.model.Racha
import com.example.pulseup.domain.model.RutinaPlan
import com.example.pulseup.domain.model.SesionCumplida
import com.google.gson.GsonBuilder

fun RutinaPlanDto.aDominio() = RutinaPlan(
    id = id ?: 0,
    nombre = nombre.orEmpty(),
    gruposMusculares = gruposMusculares.orEmpty()
)

/** [baseUrl] convierte la ruta de la foto ("/static/...") en una URL completa. */
fun EjercicioPlanDto.aDominio(baseUrl: String = "") = EjercicioPlan(
    itemId = itemId ?: 0,
    ejercicioId = ejercicioId ?: 0,
    nombre = nombre.orEmpty(),
    musculoPrincipal = musculoPrincipal.orEmpty(),
    imagenMiniatura = urlAbsoluta(baseUrl, imagenMiniatura),
    series = series ?: 0,
    repeticiones = repeticiones.orEmpty(),
    unidad = unidad ?: "repeticiones",
    pesoKg = pesoKg,
    descansoSeg = descansoSeg ?: 0
)

fun DiaPlanDto.aDominio(baseUrl: String = "") = DiaPlan(
    diaSemana = diaSemana ?: 0,
    nombreDia = nombreDia.orEmpty(),
    rutina = rutina?.aDominio(),
    ejercicios = ejercicios.orEmpty().map { it.aDominio(baseUrl) }
)

fun AvisoDto.aDominio() = AvisoDescanso(
    tipo = tipo.orEmpty(),
    musculo = musculo.orEmpty(),
    diaConflicto = diaConflicto ?: 0,
    mensaje = mensaje.orEmpty()
)

/** Los avisos sin mensaje no se pueden mostrar: se descartan. */
fun List<AvisoDto>?.avisosDominio(): List<AvisoDescanso> =
    orEmpty().map { it.aDominio() }.filter { it.mensaje.isNotBlank() }

fun DiaPrevisualizadoDto.aDominio() = DiaPrevisualizado(
    dia = dia ?: 0,
    nombreDia = nombreDia.orEmpty(),
    tieneRutina = tieneRutina ?: false,
    avisos = avisos.avisosDominio()
)

/** Días válidos (1-7) ordenados de lunes a domingo. */
fun PrevisualizarResponse.aDominio(): List<DiaPrevisualizado> =
    dias.orEmpty().map { it.aDominio() }.filter { it.dia in 1..7 }.sortedBy { it.dia }

fun AsignarDiaResponse.aDominio(baseUrl: String = ""): DiaAsignado = DiaAsignado(
    dia = dia?.aDominio(baseUrl) ?: throw IllegalStateException("Sin día"),
    avisos = avisos.avisosDominio()
)

fun AgregarEjercicioPlanResponse.aDominio(baseUrl: String = ""): EjercicioAgregado = EjercicioAgregado(
    item = item?.aDominio(baseUrl) ?: throw IllegalStateException("Sin item"),
    avisos = avisos.avisosDominio(),
    rutinaCopiada = rutinaCopiada ?: false
)

fun EditarEjercicioPlanResponse.aDominio(baseUrl: String = ""): EjercicioEditado = EjercicioEditado(
    item = item?.aDominio(baseUrl) ?: throw IllegalStateException("Sin item"),
    rutinaCopiada = rutinaCopiada ?: false
)

fun QuitarEjercicioPlanResponse.aDominio(baseUrl: String = "") = EjercicioQuitado(
    dia = dia?.aDominio(baseUrl),
    rutinaCopiada = rutinaCopiada ?: false,
    diaEnDescanso = diaEnDescanso ?: false
)

/** Gson que escribe también los null: así "peso_kg": null llega al backend y quita el peso. */
private val gsonConNulos = GsonBuilder().serializeNulls().create()

/** JSON del PUT de un ejercicio del plan (series, repeticiones, peso_kg y descanso_seg). */
fun CambiosEjercicioPlan.cuerpoEdicion(): String =
    gsonConNulos.toJson(EditarEjercicioPlanRequest(series, repeticiones, pesoKg, descansoSeg))

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
