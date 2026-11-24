package com.example.pulseup.presentation.plan

import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.AvisoDescanso
import com.example.pulseup.domain.model.CalendarioMes
import com.example.pulseup.domain.model.CambiosEjercicioPlan
import com.example.pulseup.domain.model.DiaAsignado
import com.example.pulseup.domain.model.DiaPlan
import com.example.pulseup.domain.model.DiaPrevisualizado
import com.example.pulseup.domain.model.EjercicioAgregado
import com.example.pulseup.domain.model.EjercicioEditado
import com.example.pulseup.domain.model.EjercicioPlan
import com.example.pulseup.domain.model.EjercicioQuitado
import com.example.pulseup.domain.model.Racha
import com.example.pulseup.domain.model.RutinaPlan
import com.example.pulseup.domain.model.SesionCumplida
import com.example.pulseup.domain.model.nombreDiaSemana
import com.example.pulseup.domain.repository.PlanRepository

fun aviso(diaConflicto: Int = 1, musculo: String = "pecho") = AvisoDescanso(
    "descanso_48h", musculo, diaConflicto,
    "El ${nombreDiaSemana(diaConflicto)} ya trabajas $musculo: tu $musculo necesita 48 horas de descanso"
)

fun ejercicioPlan(itemId: Int, ejercicioId: Int = itemId, unidad: String = "repeticiones", peso: Double? = 40.0) =
    EjercicioPlan(itemId, ejercicioId, "Ejercicio $ejercicioId", "pecho", null, 3, "10", unidad, peso, 60)

fun diaConRutina(dia: Int, vararg ejercicios: EjercicioPlan) =
    DiaPlan(dia, nombreDiaSemana(dia), RutinaPlan(100 + dia, "Rutina $dia", listOf("pecho")), ejercicios.toList())

/** Plan falso en memoria: registra cada llamada y permite fijar respuestas y fallos. */
class PlanFalso : PlanRepository {
    var dias: List<DiaPlan> = (1..7).map { DiaPlan(it, nombreDiaSemana(it), null) }
    var fallarPlan: String? = null
    var vecesPlan = 0

    var previsualizacion: List<DiaPrevisualizado> =
        (1..7).map { DiaPrevisualizado(it, nombreDiaSemana(it), false, emptyList()) }
    var fallarPrevisualizar: String? = null
    val previsualizadas = mutableListOf<String>()

    /** Avisos que devuelve el PUT de cada día. */
    var avisosAlAsignar: Map<Int, List<AvisoDescanso>> = emptyMap()
    var diasQueFallanAlAsignar: Set<Int> = emptySet()
    val asignados = mutableListOf<Pair<Int, Int>>()

    var avisosAlAgregar: List<AvisoDescanso> = emptyList()
    var copiarAlAgregar = false
    var fallarAgregar: Resultado.Fallo? = null
    val agregados = mutableListOf<Pair<Int, Int>>()

    var copiarAlEditar = false
    var fallarEditar: String? = null
    val ediciones = mutableListOf<Triple<Int, Int, CambiosEjercicioPlan>>()

    var diaEnDescansoAlQuitar = false
    var fallarQuitar: String? = null
    val quitados = mutableListOf<Pair<Int, Int>>()

    override suspend fun obtenerPlan(): Resultado<List<DiaPlan>> {
        vecesPlan++
        return fallarPlan?.let { Resultado.Fallo(it) } ?: Resultado.Exito(dias)
    }

    override suspend fun asignar(diaSemana: Int, idRutina: Int): Resultado<DiaAsignado> {
        asignados += diaSemana to idRutina
        if (diaSemana in diasQueFallanAlAsignar) return Resultado.Fallo("Error del servidor (500)", 500)
        val dia = DiaPlan(diaSemana, nombreDiaSemana(diaSemana), RutinaPlan(idRutina, "Rutina $idRutina", listOf("pecho")))
        return Resultado.Exito(DiaAsignado(dia, avisosAlAsignar[diaSemana].orEmpty()))
    }

    override suspend fun quitar(diaSemana: Int): Resultado<Unit> = Resultado.Exito(Unit)

    override suspend fun completarHoy(): Resultado<SesionCumplida> = Resultado.Fallo("no se usa")

    override suspend fun calendario(mes: String?): Resultado<CalendarioMes> =
        Resultado.Exito(CalendarioMes("2026-11", emptyList(), Racha(0, 0)))

    override suspend fun previsualizarEjercicio(ejercicioId: Int): Resultado<List<DiaPrevisualizado>> {
        previsualizadas += "ejercicio $ejercicioId"
        return fallarPrevisualizar?.let { Resultado.Fallo(it) } ?: Resultado.Exito(previsualizacion)
    }

    override suspend fun previsualizarRutina(rutinaId: Int): Resultado<List<DiaPrevisualizado>> {
        previsualizadas += "rutina $rutinaId"
        return fallarPrevisualizar?.let { Resultado.Fallo(it) } ?: Resultado.Exito(previsualizacion)
    }

    override suspend fun agregarEjercicio(diaSemana: Int, ejercicioId: Int): Resultado<EjercicioAgregado> {
        agregados += diaSemana to ejercicioId
        fallarAgregar?.let { return it }
        return Resultado.Exito(EjercicioAgregado(ejercicioPlan(900, ejercicioId), avisosAlAgregar, copiarAlAgregar))
    }

    override suspend fun editarEjercicio(
        diaSemana: Int,
        itemId: Int,
        cambios: CambiosEjercicioPlan
    ): Resultado<EjercicioEditado> {
        ediciones += Triple(diaSemana, itemId, cambios)
        fallarEditar?.let { return Resultado.Fallo(it, 400) }
        val nuevoId = if (copiarAlEditar) itemId + 1000 else itemId
        return Resultado.Exito(EjercicioEditado(ejercicioPlan(nuevoId), copiarAlEditar))
    }

    override suspend fun quitarEjercicio(diaSemana: Int, itemId: Int): Resultado<EjercicioQuitado> {
        quitados += diaSemana to itemId
        fallarQuitar?.let { return Resultado.Fallo(it) }
        return Resultado.Exito(EjercicioQuitado(null, rutinaCopiada = false, diaEnDescanso = diaEnDescansoAlQuitar))
    }
}
