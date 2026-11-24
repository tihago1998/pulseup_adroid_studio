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
    val instrucciones: List<String>?,
    // Solo en el catálogo (las rutinas traen la forma básica de arriba)
    @SerializedName("musculo_principal") val musculoPrincipal: String? = null,
    val ambientes: List<String>? = null,
    @SerializedName("imagen_miniatura") val imagenMiniatura: String? = null
)

/**
 * GET api/ejercicios/. Sin limit/offset: {"ejercicios", "total"} con todos.
 * Paginado: además "items" (la misma lista), "limit" y "offset".
 */
data class EjerciciosListaResponse(
    val ejercicios: List<EjercicioDto>?,
    val total: Int? = null,
    val items: List<EjercicioDto>? = null
)

data class ImagenDto(val orden: Int?, val url: String?)

data class RecomendacionDto(
    val nivel: String?,
    val unidad: String?,
    @SerializedName("reps_min") val repsMin: Int?,
    @SerializedName("reps_max") val repsMax: Int?,
    @SerializedName("series_min") val seriesMin: Int?,
    @SerializedName("series_max") val seriesMax: Int?,
    @SerializedName("descanso_seg") val descansoSeg: Int?,
    val rir: String?,
    @SerializedName("dias_semana") val diasSemana: String?,
    val rpe: String?,
    @SerializedName("peso_kg") val pesoKg: Double?,
    val nota: String?,
    val aviso: String?
)

/** Detalle: la forma del listado + pasos, fotos y recomendación. */
data class EjercicioDetalleDto(
    val id: Int,
    val nombre: String?,
    @SerializedName("grupo_muscular") val grupoMuscular: String?,
    @SerializedName("musculo_principal") val musculoPrincipal: String?,
    @SerializedName("musculos_secundarios") val musculosSecundarios: List<String>?,
    val equipo: String?,
    val nivel: String?,
    val ambientes: List<String>?,
    val instrucciones: List<String>?,
    val pasos: List<String>?,
    val respiracion: String?,
    @SerializedName("errores_comunes") val erroresComunes: List<String>?,
    @SerializedName("variante_facil") val varianteFacil: String?,
    @SerializedName("variante_dificil") val varianteDificil: String?,
    @SerializedName("descanso_seg") val descansoSeg: Int?,
    val imagenes: List<ImagenDto>?,
    val recomendacion: RecomendacionDto?,
    @SerializedName("recomendacion_motivo") val recomendacionMotivo: String?
)

/** GET api/ejercicios/{id} responde {"ejercicio": {...}}. */
data class EjercicioDetalleResponse(val ejercicio: EjercicioDetalleDto?)

/** GET api/ejercicios/{id}/recomendacion. */
data class RecomendacionResponse(
    val recomendacion: RecomendacionDto?,
    @SerializedName("recomendacion_motivo") val recomendacionMotivo: String?
)

data class FiltrosResponse(
    val musculos: List<String>?,
    val equipos: List<String>?,
    val niveles: List<String>?,
    val ambientes: List<String>? = null
)

data class CreditoDto(
    val fuente: String?,
    val licencia: String?,
    val autor: String?,
    val atribucion: String?,
    val imagenes: Int?
)

/** GET api/ejercicios/creditos. */
data class CreditosResponse(val creditos: List<CreditoDto>?, val textos: String?)

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
    val ejercicios: List<EjercicioDeRutinaDto>?,
    /** "plan" si la creó el plan semanal al agregar ejercicios a un día; null en las demás. */
    val origen: String? = null
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
