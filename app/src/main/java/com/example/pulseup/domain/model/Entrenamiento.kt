package com.example.pulseup.domain.model

/** Valores que acepta el backend para objetivo y nivel (se muestran como filtros). */
object Catalogos {
    val OBJETIVOS = listOf("general", "fuerza", "hipertrofia", "resistencia")
    val NIVELES = listOf("principiante", "intermedio", "avanzado")

    /** Dónde se puede hacer un ejercicio (filtro "Gimnasio / Casa / Calistenia" del catálogo). */
    val AMBIENTES = listOf("gimnasio", "casa", "calistenia")
}

/**
 * Ejercicio del listado. Los tres últimos campos solo llegan en el catálogo; las rutinas
 * traen la forma básica y quedan con sus valores por defecto.
 */
data class Ejercicio(
    val id: Int,
    val nombre: String,
    val grupoMuscular: String,
    val musculosSecundarios: List<String>,
    val equipo: String,
    val nivel: String,
    val instrucciones: List<String>,
    val musculoPrincipal: String = grupoMuscular,
    val ambientes: List<String> = emptyList(),
    /** URL completa de la foto de inicio, o null si el ejercicio no tiene fotos. */
    val imagenMiniatura: String? = null
)

/** Valores disponibles para los filtros del catálogo (GET api/ejercicios/filtros). */
data class FiltrosCatalogo(
    val ambientes: List<String>,
    val musculos: List<String>,
    val equipos: List<String>,
    val niveles: List<String>
)

data class EjercicioDeRutina(
    val orden: Int,
    val series: Int,
    val repeticiones: String,
    val descansoSeg: Int,
    val ejercicio: Ejercicio
)

data class Rutina(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val objetivo: String,
    val nivel: String,
    val duracionMin: Int,
    val totalEjercicios: Int,
    val esPlantilla: Boolean,
    val ejercicios: List<EjercicioDeRutina>
)

data class ItemNuevaRutina(
    val idEjercicio: Int,
    val series: Int,
    val repeticiones: String,
    val descansoSeg: Int
)

data class NuevaRutina(
    val nombre: String,
    val descripcion: String,
    val objetivo: String,
    val nivel: String,
    val duracionMin: Int,
    val items: List<ItemNuevaRutina>
)
