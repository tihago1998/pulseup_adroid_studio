package com.example.pulseup.data.remote.dto

import com.google.gson.annotations.SerializedName

// Nombres exactos de las respuestas de /api/plan/ y /api/calendario/. Todo nulable por Gson.

data class RutinaPlanDto(
    val id: Int?,
    val nombre: String?,
    @SerializedName("grupos_musculares") val gruposMusculares: List<String>?
)

/** Ejercicio de un día del plan. `repeticiones` es texto ("9", "8-10", "40"); peso_kg puede ser null. */
data class EjercicioPlanDto(
    @SerializedName("item_id") val itemId: Int?,
    @SerializedName("ejercicio_id") val ejercicioId: Int?,
    val nombre: String?,
    @SerializedName("musculo_principal") val musculoPrincipal: String?,
    @SerializedName("imagen_miniatura") val imagenMiniatura: String?,
    val series: Int?,
    val repeticiones: String?,
    val unidad: String?,
    @SerializedName("peso_kg") val pesoKg: Double?,
    @SerializedName("descanso_seg") val descansoSeg: Int?
)

data class DiaPlanDto(
    @SerializedName("dia_semana") val diaSemana: Int?,
    @SerializedName("nombre_dia") val nombreDia: String?,
    val rutina: RutinaPlanDto?,
    val ejercicios: List<EjercicioPlanDto>? = null
)

data class PlanResponse(val plan: List<DiaPlanDto>?)

data class AsignarDiaRequest(@SerializedName("id_rutina") val idRutina: Int)

/** PUT api/plan/{dia}: ya no responde 409 por la regla de 48 h; los choques llegan en "avisos". */
data class AsignarDiaResponse(
    val dia: DiaPlanDto?,
    val message: String?,
    val avisos: List<AvisoDto>? = null
)

/** Aviso de la regla de 48 h ("tipo" es "descanso_48h"). */
data class AvisoDto(
    val tipo: String?,
    val musculo: String?,
    @SerializedName("dia_conflicto") val diaConflicto: Int?,
    val mensaje: String?
)

data class DiaPrevisualizadoDto(
    val dia: Int?,
    @SerializedName("nombre_dia") val nombreDia: String?,
    @SerializedName("tiene_rutina") val tieneRutina: Boolean?,
    val avisos: List<AvisoDto>?
)

data class PrevisualizarResponse(val dias: List<DiaPrevisualizadoDto>?)

/** Solo el ejercicio: series, repeticiones, peso y descanso los rellena el backend con la recomendación. */
data class AgregarEjercicioPlanRequest(@SerializedName("ejercicio_id") val ejercicioId: Int)

data class AgregarEjercicioPlanResponse(
    val item: EjercicioPlanDto?,
    val avisos: List<AvisoDto>?,
    @SerializedName("rutina_copiada") val rutinaCopiada: Boolean?
)

/**
 * Cuerpo de PUT api/plan/{dia}/ejercicios/{item_id}. peso_kg se envía SIEMPRE, también null
 * (= sin peso); por eso se serializa con nulos (ver cuerpoEdicion). La unidad no se edita.
 */
data class EditarEjercicioPlanRequest(
    val series: Int,
    val repeticiones: String,
    @SerializedName("peso_kg") val pesoKg: Double?,
    @SerializedName("descanso_seg") val descansoSeg: Int
)

data class EditarEjercicioPlanResponse(
    val item: EjercicioPlanDto?,
    @SerializedName("rutina_copiada") val rutinaCopiada: Boolean?
)

data class QuitarEjercicioPlanResponse(
    val message: String?,
    @SerializedName("rutina_copiada") val rutinaCopiada: Boolean?,
    @SerializedName("dia_en_descanso") val diaEnDescanso: Boolean?,
    val dia: DiaPlanDto?
)

data class SesionDto(
    @SerializedName("completada_en") val completadaEn: String?,
    @SerializedName("dia_semana") val diaSemana: Int?,
    val fecha: String?,
    @SerializedName("nombre_dia") val nombreDia: String?,
    val rutina: RutinaPlanDto?
)

data class CompletarResponse(val message: String?, val sesion: SesionDto?)

data class DiaCalendarioDto(
    @SerializedName("dia_semana") val diaSemana: Int?,
    val estado: String?,
    val fecha: String?,
    val rutina: RutinaPlanDto?
)

data class RachaDto(val actual: Int?, val mejor: Int?)

data class CalendarioResponse(
    val dias: List<DiaCalendarioDto>?,
    val mes: String?,
    val racha: RachaDto?
)
