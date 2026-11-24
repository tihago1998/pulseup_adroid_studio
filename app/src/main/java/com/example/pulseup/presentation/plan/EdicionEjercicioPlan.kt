package com.example.pulseup.presentation.plan

import com.example.pulseup.domain.model.CambiosEjercicioPlan
import com.example.pulseup.domain.model.EjercicioPlan
import com.example.pulseup.domain.model.RecomendacionEjercicio
import com.example.pulseup.presentation.perfil.formatearDecimal
import com.example.pulseup.presentation.perfil.leerDecimal

// Lógica pura de la edición de un ejercicio del plan (los mismos límites que valida el backend).

/** Ejercicio del plan que se está editando, con lo que el usuario escribe en cada campo. */
data class EdicionEjercicio(
    val item: EjercicioPlan,
    val series: String,
    /** Repeticiones ("8-10") o duración en segundos o minutos, según item.unidad. */
    val repeticiones: String,
    /** Vacío = sin peso. Acepta coma o punto decimal. */
    val peso: String,
    val descanso: String,
    /** Guía, no se impone: null si no hay recomendación o aún no llega. */
    val recomendacion: RecomendacionEjercicio? = null,
    val cargandoRecomendacion: Boolean = true,
    val error: String? = null,
    val guardando: Boolean = false
) {
    /** En minutos (cardio) solo se edita la duración: sin series, peso ni descanso. */
    val esCardio: Boolean get() = item.unidad == "minutos"

    companion object {
        fun desde(item: EjercicioPlan) = EdicionEjercicio(
            item = item,
            series = item.series.toString(),
            repeticiones = item.repeticiones,
            peso = item.pesoKg?.let { formatearDecimal(it) }.orEmpty(),
            descanso = item.descansoSeg.toString()
        )
    }
}

/** Etiqueta del campo de repeticiones según la unidad. */
fun etiquetaRepeticiones(unidad: String): String = when (unidad) {
    "segundos" -> "Duración (segundos)"
    "minutos" -> "Duración (minutos)"
    else -> "Repeticiones"
}

sealed interface ValidacionEdicion {
    data class Valida(val cambios: CambiosEjercicioPlan) : ValidacionEdicion
    data class Invalida(val mensaje: String) : ValidacionEdicion
}

const val MAX_LARGO_REPETICIONES = 20

fun validarEdicion(e: EdicionEjercicio): ValidacionEdicion {
    val series = e.series.trim().toIntOrNull()?.takeIf { it in 1..10 }
        ?: return ValidacionEdicion.Invalida("Las series deben estar entre 1 y 10")

    val repeticiones = e.repeticiones.trim()
    if (repeticiones.isEmpty()) {
        return ValidacionEdicion.Invalida(
            if (e.item.unidad == "repeticiones") "Escribe las repeticiones" else "Escribe la duración"
        )
    }
    if (repeticiones.length > MAX_LARGO_REPETICIONES) {
        return ValidacionEdicion.Invalida("Las repeticiones admiten máximo $MAX_LARGO_REPETICIONES caracteres")
    }

    val pesoTexto = e.peso.trim()
    val peso = if (pesoTexto.isEmpty()) null else {
        leerDecimal(pesoTexto)?.takeIf { it in 0.0..500.0 }
            ?: return ValidacionEdicion.Invalida("El peso debe estar entre 0 y 500 kg, o déjalo vacío")
    }

    val descanso = e.descanso.trim().toIntOrNull()?.takeIf { it in 0..600 }
        ?: return ValidacionEdicion.Invalida("El descanso debe estar entre 0 y 600 segundos")

    return ValidacionEdicion.Valida(CambiosEjercicioPlan(series, repeticiones, peso, descanso))
}
