package com.example.pulseup.domain.model

/** Rutina asignada a un día del plan (versión resumida). */
data class RutinaPlan(
    val id: Int,
    val nombre: String,
    val gruposMusculares: List<String>
)

/** Un día de la semana del plan: lunes = 1 … domingo = 7. Sin rutina = día de descanso. */
data class DiaPlan(
    val diaSemana: Int,
    val nombreDia: String,
    val rutina: RutinaPlan?,
    /** Ejercicios de la rutina de ese día (vacío en descanso o si el backend no los manda). */
    val ejercicios: List<EjercicioPlan> = emptyList()
) {
    /** Tiene rutina o ejercicios: asignarle otra rutina los reemplaza. */
    val tieneContenido: Boolean get() = rutina != null || ejercicios.isNotEmpty()
}

private val NOMBRES_DIAS = listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")

/** 1 -> "lunes" … 7 -> "domingo" (igual que nombre_dia del backend); fuera de rango, "día N". */
fun nombreDiaSemana(dia: Int): String = NOMBRES_DIAS.getOrNull(dia - 1) ?: "día $dia"

/** Máximo de ejercicios por día que acepta el backend. */
const val MAX_EJERCICIOS_POR_DIA = 20

/**
 * Un ejercicio dentro de un día del plan. [itemId] identifica la fila y puede CAMBIAR al editar o quitar
 * (si la rutina se copia): siempre se recarga el plan después.
 * [repeticiones] es texto ("9", "8-10", "40") y se lee según [unidad]: repeticiones, segundos o minutos.
 */
data class EjercicioPlan(
    val itemId: Int,
    val ejercicioId: Int,
    val nombre: String,
    val musculoPrincipal: String,
    /** URL completa de la foto, o null si no tiene. */
    val imagenMiniatura: String?,
    val series: Int,
    val repeticiones: String,
    val unidad: String,
    val pesoKg: Double?,
    /** 0 en cardio: no se muestra. */
    val descansoSeg: Int
)

/** Aviso de la regla de 48 h. Solo informa: el usuario siempre puede continuar. */
data class AvisoDescanso(
    val tipo: String,
    val musculo: String,
    val diaConflicto: Int,
    val mensaje: String
)

/** Cómo quedaría cada día si se le agrega un ejercicio o una rutina (GET api/plan/previsualizar). */
data class DiaPrevisualizado(
    val dia: Int,
    val nombreDia: String,
    val tieneRutina: Boolean,
    val avisos: List<AvisoDescanso>
)

/** Rutina asignada a un día con PUT api/plan/{dia}: el día ya actualizado y los avisos de 48 h. */
data class DiaAsignado(val dia: DiaPlan, val avisos: List<AvisoDescanso>)

/** Respuesta de agregar un ejercicio. [rutinaCopiada] = la rutina era compartida y se copió solo para ese día. */
data class EjercicioAgregado(
    val item: EjercicioPlan,
    val avisos: List<AvisoDescanso>,
    val rutinaCopiada: Boolean
)

data class EjercicioEditado(val item: EjercicioPlan, val rutinaCopiada: Boolean)

/** [diaEnDescanso] = era el último ejercicio y el día quedó sin rutina. */
data class EjercicioQuitado(
    val dia: DiaPlan?,
    val rutinaCopiada: Boolean,
    val diaEnDescanso: Boolean
)

/** Valores editables de un ejercicio del plan. [pesoKg] null = sin peso (se envía null al backend). */
data class CambiosEjercicioPlan(
    val series: Int,
    val repeticiones: String,
    val pesoKg: Double?,
    val descansoSeg: Int
)

enum class EstadoDia { CUMPLIDO, PENDIENTE, INCUMPLIDO, DESCANSO }

data class DiaCalendario(
    val fecha: String, // YYYY-MM-DD
    val diaSemana: Int,
    val estado: EstadoDia,
    val rutina: RutinaPlan?
)

data class Racha(val actual: Int, val mejor: Int)

data class CalendarioMes(
    val mes: String, // YYYY-MM
    val dias: List<DiaCalendario>,
    val racha: Racha
)

data class SesionCumplida(
    val fecha: String,
    val nombreDia: String,
    val nombreRutina: String,
    val mensaje: String
)
