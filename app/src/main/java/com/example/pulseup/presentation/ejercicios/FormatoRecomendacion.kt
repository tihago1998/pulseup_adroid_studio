package com.example.pulseup.presentation.ejercicios

import com.example.pulseup.domain.model.RecomendacionEjercicio
import com.example.pulseup.presentation.perfil.formatearDecimal

// Lógica pura (sin Compose ni Android): textos de la tarjeta "Recomendado para ti".

/** Textos listos para mostrar. Los campos null no se muestran; el aviso siempre. */
data class TextosRecomendacion(
    val titulo: String,
    /** "3–4 series × 6–12 repeticiones", "3 series × 30–45 s" o "30–40 min · 3–4 días por semana · esfuerzo 5–6 de 10". */
    val principal: String,
    val descanso: String?,
    val reserva: String?,
    val peso: String?,
    val nota: String?,
    val aviso: String
)

/** 3 y 4 -> "3–4"; 3 y 3 -> "3". */
fun rango(min: Int, max: Int): String = if (min == max || max < min) "$min" else "$min–$max"

/** El backend manda los rangos de texto con guion ("2-3"); se muestran con raya corta ("2–3"). */
fun rangoDeTexto(texto: String): String = texto.trim().replace("-", "–")

private fun series(min: Int, max: Int): String =
    rango(min, max) + if (min == 1 && max <= 1) " serie" else " series"

/** 45 -> "45 s", 60 -> "1 min", 150 -> "2 min 30 s". */
fun duracionDescanso(segundos: Int): String {
    val min = segundos / 60
    val seg = segundos % 60
    return when {
        min == 0 -> "$seg s"
        seg == 0 -> "$min min"
        else -> "$min min $seg s"
    }
}

/** 72.5 -> "≈ 72,5 kg"; 70.0 -> "≈ 70 kg". */
fun pesoAproximado(kg: Double): String = "≈ ${formatearDecimal(kg)} kg"

fun textoPrincipal(r: RecomendacionEjercicio): String = when (r.unidad) {
    "minutos" -> listOfNotNull(
        "${rango(r.repsMin, r.repsMax)} min",
        r.diasSemana?.let { dias -> "${rangoDeTexto(dias)} ${if (dias.trim() == "1") "día" else "días"} por semana" },
        r.rpe?.let { "esfuerzo ${rangoDeTexto(it)} de 10" }
    ).joinToString(" · ")
    "segundos" -> "${series(r.seriesMin, r.seriesMax)} × ${rango(r.repsMin, r.repsMax)} s"
    // "repeticiones" y cualquier unidad desconocida
    else -> "${series(r.seriesMin, r.seriesMax)} × ${rango(r.repsMin, r.repsMax)} repeticiones"
}

/**
 * Series y duración de un ejercicio del plan, según su unidad:
 * "3 × 10 · 40 kg", "3 × 8–10", "3 × 40 s" o "35 min" (en minutos no se muestran series ni peso).
 */
fun textoSeriesPlan(series: Int, repeticiones: String, unidad: String, pesoKg: Double?): String {
    val valor = rangoDeTexto(repeticiones)
    return when (unidad) {
        "minutos" -> "$valor min"
        "segundos" -> "$series × $valor s" + (pesoKg?.let { " · ${formatearDecimal(it)} kg" } ?: "")
        else -> "$series × $valor" + (pesoKg?.let { " · ${formatearDecimal(it)} kg" } ?: "")
    }
}

/** "Descanso 1 min 15 s"; null con 0 o menos (el cardio no tiene descanso entre series). */
fun textoDescansoPlan(segundos: Int): String? =
    if (segundos <= 0) null else "Descanso ${duracionDescanso(segundos)}"

fun textosRecomendacion(r: RecomendacionEjercicio): TextosRecomendacion = TextosRecomendacion(
    titulo = "Recomendado para ti · nivel ${r.nivel}",
    principal = textoPrincipal(r),
    descanso = r.descansoSeg?.let { "Descanso entre series: ${duracionDescanso(it)}" },
    reserva = r.rir?.let { "Deja ${rangoDeTexto(it)} repeticiones en reserva" },
    peso = r.pesoKg?.let { pesoAproximado(it) },
    nota = r.nota,
    aviso = r.aviso
)
