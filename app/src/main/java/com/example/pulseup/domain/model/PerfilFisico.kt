package com.example.pulseup.domain.model

/** Datos físicos del usuario tal como los guarda el backend. */
data class PerfilFisico(
    val fechaNacimiento: String, // YYYY-MM-DD
    val edad: Int,               // la calcula el servidor
    val sexo: String,            // uno de ValoresPerfilFisico.SEXOS
    val pesoKg: Double,
    val estaturaCm: Double,
    val nivel: String,           // uno de Catalogos.NIVELES
    val actualizadoEn: String    // ISO 8601 con desfase
)

/** Lo que se envía en el PUT. La edad no se envía: la calcula el servidor. */
data class NuevoPerfilFisico(
    val fechaNacimiento: String, // YYYY-MM-DD
    val sexo: String,
    val pesoKg: Double,
    val estaturaCm: Double,
    val nivel: String
)

/** Valores y límites que acepta el backend para el perfil físico. Los niveles están en Catalogos.NIVELES. */
object ValoresPerfilFisico {
    val SEXOS = listOf("hombre", "mujer", "no_indica")
    const val SEXO_POR_DEFECTO = "no_indica"

    const val EDAD_MINIMA = 14
    const val EDAD_MAXIMA = 100
    const val PESO_MIN_KG = 30.0
    const val PESO_MAX_KG = 300.0
    const val ESTATURA_MIN_CM = 100.0
    const val ESTATURA_MAX_CM = 250.0
}
