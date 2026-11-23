package com.example.pulseup.domain.model

/** Filtros de una búsqueda del catálogo. null o vacío = sin filtrar por ese campo. */
data class FiltroCatalogo(
    val texto: String = "",
    val ambiente: String? = null,
    val musculo: String? = null,
    val equipo: String? = null,
    val nivel: String? = null
)

/** Una página del catálogo: los ejercicios pedidos y cuántos hay en total con esos filtros. */
data class PaginaEjercicios(
    val ejercicios: List<Ejercicio>,
    val total: Int
)

/** Foto de un ejercicio: orden 0 = posición inicial, 1 = posición final. */
data class ImagenEjercicio(
    val orden: Int,
    val url: String
)

/**
 * Recomendación personalizada según el perfil físico.
 * [unidad]: "repeticiones", "segundos" o "minutos" (cardio). Con minutos, repsMin/repsMax son minutos.
 * [rir], [diasSemana] y [rpe] llegan como texto, por ejemplo "2-3".
 */
data class RecomendacionEjercicio(
    val nivel: String,
    val unidad: String,
    val repsMin: Int,
    val repsMax: Int,
    val seriesMin: Int,
    val seriesMax: Int,
    val descansoSeg: Int?,
    val rir: String?,
    val diasSemana: String?,
    val rpe: String?,
    val pesoKg: Double?,
    val nota: String?,
    val aviso: String
)

data class EjercicioDetalle(
    val id: Int,
    val nombre: String,
    val musculoPrincipal: String,
    val musculosSecundarios: List<String>,
    val equipo: String,
    val nivel: String,
    val ambientes: List<String>,
    val imagenes: List<ImagenEjercicio>,
    val pasos: List<String>,
    val respiracion: String?,
    val erroresComunes: List<String>,
    val varianteFacil: String?,
    val varianteDificil: String?,
    val descansoSeg: Int?,
    /** null si el usuario no tiene perfil físico o el ejercicio no tiene regla: ver [recomendacionMotivo]. */
    val recomendacion: RecomendacionEjercicio?,
    val recomendacionMotivo: String?
)

/** Fuente y licencia de un grupo de fotos del catálogo. */
data class CreditoImagenes(
    val fuente: String,
    val licencia: String,
    val autor: String?,
    val atribucion: String?,
    val imagenes: Int
)

data class CreditosCatalogo(
    val creditos: List<CreditoImagenes>,
    /** Nota sobre los textos (pasos, respiración...), que no son fotos. */
    val textos: String?
)
