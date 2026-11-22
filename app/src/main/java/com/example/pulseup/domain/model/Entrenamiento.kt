package com.example.pulseup.domain.model

/** Valores que acepta el backend para objetivo y nivel (se muestran como filtros). */
object Catalogos {
    val OBJETIVOS = listOf("general", "fuerza", "hipertrofia", "resistencia")
    val NIVELES = listOf("principiante", "intermedio", "avanzado")
}

data class Ejercicio(
    val id: Int,
    val nombre: String,
    val grupoMuscular: String,
    val musculosSecundarios: List<String>,
    val equipo: String,
    val nivel: String,
    val instrucciones: List<String>
)

data class FiltrosEjercicio(
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
