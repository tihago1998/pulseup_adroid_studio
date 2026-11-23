package com.example.pulseup.data.remote.dto

import com.google.gson.annotations.SerializedName

// Nombres exactos de /api/perfil-fisico/ (ver ejemplos_api_perfil_fisico.md del backend). Todo nulable por Gson.

data class PerfilFisicoDto(
    @SerializedName("actualizado_en") val actualizadoEn: String?,
    val edad: Int?,
    @SerializedName("estatura_cm") val estaturaCm: Double?,
    @SerializedName("fecha_nacimiento") val fechaNacimiento: String?,
    val nivel: String?,
    @SerializedName("peso_kg") val pesoKg: Double?,
    val sexo: String?
)

/** GET responde {"perfil": ...} o {"perfil": null}; PUT responde además "message". */
data class PerfilFisicoResponse(
    val message: String?,
    val perfil: PerfilFisicoDto?
)

/** Cuerpo del PUT. "edad" no se envía: el servidor la calcula. */
data class PerfilFisicoRequest(
    @SerializedName("fecha_nacimiento") val fechaNacimiento: String,
    val sexo: String,
    @SerializedName("peso_kg") val pesoKg: Double,
    @SerializedName("estatura_cm") val estaturaCm: Double,
    val nivel: String
)
