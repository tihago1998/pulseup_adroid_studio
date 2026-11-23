package com.example.pulseup.data.remote.dto

import com.google.gson.annotations.SerializedName

// Nombres exactos de las respuestas de /api/plan/ y /api/calendario/. Todo nulable por Gson.

data class RutinaPlanDto(
    val id: Int?,
    val nombre: String?,
    @SerializedName("grupos_musculares") val gruposMusculares: List<String>?
)

data class DiaPlanDto(
    @SerializedName("dia_semana") val diaSemana: Int?,
    @SerializedName("nombre_dia") val nombreDia: String?,
    val rutina: RutinaPlanDto?
)

data class PlanResponse(val plan: List<DiaPlanDto>?)

data class AsignarDiaRequest(@SerializedName("id_rutina") val idRutina: Int)

data class AsignarDiaResponse(val dia: DiaPlanDto?, val message: String?)

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
