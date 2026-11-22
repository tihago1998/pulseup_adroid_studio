package com.example.pulseup.data.remote.dto

import com.google.gson.annotations.SerializedName

// Los nombres con @SerializedName son EXACTAMENTE los que envía el backend (snake_case).
// Todo es nulable porque Gson no respeta la nulabilidad de Kotlin: el mapper pone los valores por defecto.

data class EjercicioDto(
    val id: Int,
    val nombre: String?,
    @SerializedName("grupo_muscular") val grupoMuscular: String?,
    @SerializedName("musculos_secundarios") val musculosSecundarios: List<String>?,
    val equipo: String?,
    val nivel: String?,
    val instrucciones: List<String>?
)

data class EjerciciosListaResponse(val ejercicios: List<EjercicioDto>?)

data class EjercicioResponse(val ejercicio: EjercicioDto?)

data class FiltrosResponse(
    val musculos: List<String>?,
    val equipos: List<String>?,
    val niveles: List<String>?
)

data class EjercicioDeRutinaDto(
    val orden: Int?,
    val series: Int?,
    val repeticiones: String?,
    @SerializedName("descanso_seg") val descansoSeg: Int?,
    val ejercicio: EjercicioDto?
)

data class RutinaDto(
    val id: Int,
    val nombre: String?,
    val descripcion: String?,
    val objetivo: String?,
    val nivel: String?,
    @SerializedName("duracion_min") val duracionMin: Int?,
    @SerializedName("total_ejercicios") val totalEjercicios: Int?,
    @SerializedName("es_plantilla") val esPlantilla: Boolean?,
    val ejercicios: List<EjercicioDeRutinaDto>?
)

data class RutinasListaResponse(val rutinas: List<RutinaDto>?)

data class RutinaResponse(val rutina: RutinaDto?)

data class ItemRutinaRequest(
    @SerializedName("id_ejercicio") val idEjercicio: Int,
    val series: Int,
    val repeticiones: String,
    @SerializedName("descanso_seg") val descansoSeg: Int
)

data class NuevaRutinaRequest(
    val nombre: String,
    val descripcion: String,
    val objetivo: String,
    val nivel: String,
    @SerializedName("duracion_min") val duracionMin: Int,
    val ejercicios: List<ItemRutinaRequest>
)

data class CrearRutinaResponse(val message: String?, val id: Int?)

// El backend responde { "error": "..." } cuando algo sale mal
data class ErrorResponse(val error: String?)
