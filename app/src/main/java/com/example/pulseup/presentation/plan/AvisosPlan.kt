package com.example.pulseup.presentation.plan

import com.example.pulseup.domain.model.AvisoDescanso

// Lógica pura (sin Compose): mensajes que se muestran después de cambiar el plan.

/** EXITO = se guardó; INFO = dato útil; ADVERTENCIA = regla de 48 h (no bloquea); ERROR = no se pudo. */
enum class TipoNota { EXITO, INFO, ADVERTENCIA, ERROR }

data class Nota(val texto: String, val tipo: TipoNota)

/** Cada aviso de 48 h como advertencia, sin repetir el mismo mensaje. */
fun notasDeAvisos(avisos: List<AvisoDescanso>): List<Nota> =
    avisos.map { it.mensaje }.distinct().map { Nota(it, TipoNota.ADVERTENCIA) }

/** Lo que significa rutina_copiada = true: la rutina era compartida y se separó solo para ese día. */
fun textoRutinaCopiada(nombreDia: String): String =
    "La rutina del $nombreDia era compartida: se creó una copia solo para ese día y la original no cambió."

/** 409 al agregar: el ejercicio ya está ese día. */
fun textoYaEstaEnElDia(nombreDia: String): String =
    "Este ejercicio ya está en tu plan del $nombreDia. Elige otro día."

/** Confirmación antes de asignar una rutina a días que ya tienen ejercicios: "Esto reemplazará los ejercicios de lunes". */
fun textoReemplazo(nombresDias: List<String>): String = "Esto reemplazará los ejercicios de ${unirDias(nombresDias)}"

/** ["lunes"] -> "lunes"; ["lunes", "jueves"] -> "lunes y jueves"; con tres o más, comas y "y" al final. */
fun unirDias(nombres: List<String>): String = when (nombres.size) {
    0 -> ""
    1 -> nombres[0]
    else -> nombres.dropLast(1).joinToString(", ") + " y " + nombres.last()
}
