package com.example.pulseup.presentation.perfil

import com.example.pulseup.domain.model.Catalogos
import com.example.pulseup.domain.model.NuevoPerfilFisico
import com.example.pulseup.domain.model.ValoresPerfilFisico
import java.time.LocalDate
import java.time.Period
import kotlin.math.roundToLong

// Lógica pura (sin Compose ni Android) para validar el formulario con las mismas reglas del backend.

/** Mensaje de error de cada campo; null = el campo está bien. */
data class ErroresPerfilFisico(
    val fecha: String? = null,
    val peso: String? = null,
    val estatura: String? = null,
    val nivel: String? = null
) {
    val hayErrores: Boolean get() = fecha != null || peso != null || estatura != null || nivel != null
}

sealed interface ValidacionPerfilFisico {
    data class Valido(val nuevo: NuevoPerfilFisico) : ValidacionPerfilFisico
    data class Invalido(val errores: ErroresPerfilFisico) : ValidacionPerfilFisico
}

/** Años cumplidos a [hoy]. */
fun calcularEdad(fechaNacimiento: LocalDate, hoy: LocalDate): Int =
    Period.between(fechaNacimiento, hoy).years

/** Acepta coma o punto decimal: "72,5" y "72.5" dan 72.5. null si no es un número. */
fun leerDecimal(texto: String): Double? = texto.trim().replace(',', '.').toDoubleOrNull()

private val PATRON_ENTRADA_DECIMAL = Regex("""^\d{0,3}([.,]\d?)?$""")

/** Lo que se puede escribir en peso y estatura: hasta 3 cifras y como mucho 1 decimal. */
fun esEntradaDecimalValida(texto: String): Boolean = PATRON_ENTRADA_DECIMAL.matches(texto)

/** 175.0 -> "175", 72.5 -> "72,5". Para rellenar el formulario con lo que devuelve el backend. */
fun formatearDecimal(valor: Double): String {
    val redondeado = redondearUnDecimal(valor)
    return if (redondeado % 1.0 == 0.0) redondeado.toLong().toString()
    else redondeado.toString().replace('.', ',')
}

private fun redondearUnDecimal(valor: Double): Double = (valor * 10).roundToLong() / 10.0

fun validarPerfilFisico(
    fechaNacimiento: LocalDate?,
    sexo: String,
    peso: String,
    estatura: String,
    nivel: String?,
    hoy: LocalDate
): ValidacionPerfilFisico {
    val errorFecha = when {
        fechaNacimiento == null -> "Elige tu fecha de nacimiento"
        fechaNacimiento.isAfter(hoy) -> "La fecha de nacimiento no puede ser futura"
        calcularEdad(fechaNacimiento, hoy) < ValoresPerfilFisico.EDAD_MINIMA ->
            "Debes tener al menos 14 años para usar PulseUp"
        calcularEdad(fechaNacimiento, hoy) > ValoresPerfilFisico.EDAD_MAXIMA ->
            "Revisa la fecha de nacimiento: la edad máxima es 100 años"
        else -> null
    }

    val pesoKg = leerDecimal(peso)?.let(::redondearUnDecimal)
    val errorPeso = when {
        peso.isBlank() -> "Escribe tu peso en kg"
        pesoKg == null -> "El peso debe ser un número"
        pesoKg < ValoresPerfilFisico.PESO_MIN_KG || pesoKg > ValoresPerfilFisico.PESO_MAX_KG ->
            "El peso debe estar entre 30 y 300 kg"
        else -> null
    }

    val estaturaCm = leerDecimal(estatura)?.let(::redondearUnDecimal)
    val errorEstatura = when {
        estatura.isBlank() -> "Escribe tu estatura en cm"
        estaturaCm == null -> "La estatura debe ser un número"
        estaturaCm < ValoresPerfilFisico.ESTATURA_MIN_CM || estaturaCm > ValoresPerfilFisico.ESTATURA_MAX_CM ->
            "La estatura debe estar entre 100 y 250 cm"
        else -> null
    }

    val errorNivel = if (nivel.isNullOrBlank() || nivel !in Catalogos.NIVELES) "Elige tu nivel" else null

    val errores = ErroresPerfilFisico(errorFecha, errorPeso, errorEstatura, errorNivel)
    if (errores.hayErrores) return ValidacionPerfilFisico.Invalido(errores)

    return ValidacionPerfilFisico.Valido(
        NuevoPerfilFisico(
            fechaNacimiento = fechaNacimiento.toString(), // LocalDate.toString() = YYYY-MM-DD
            sexo = if (sexo in ValoresPerfilFisico.SEXOS) sexo else ValoresPerfilFisico.SEXO_POR_DEFECTO,
            pesoKg = pesoKg!!,
            estaturaCm = estaturaCm!!,
            nivel = nivel!!
        )
    )
}
