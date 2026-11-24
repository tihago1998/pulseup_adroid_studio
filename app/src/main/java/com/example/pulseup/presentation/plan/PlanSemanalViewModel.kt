package com.example.pulseup.presentation.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.AvisoDescanso
import com.example.pulseup.domain.model.DiaPlan
import com.example.pulseup.domain.model.Rutina
import com.example.pulseup.domain.model.nombreDiaSemana
import com.example.pulseup.domain.repository.PlanRepository
import com.example.pulseup.domain.repository.RutinasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Rutina elegida para un día que choca con la regla de 48 h: se pide "Asignar igual". */
data class AsignacionPorConfirmar(
    val dia: Int,
    val nombreDia: String,
    val idRutina: Int,
    val nombreRutina: String,
    val avisos: List<AvisoDescanso>
)

data class PlanSemanalUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val dias: List<DiaPlan> = emptyList(),
    val rutinas: List<Rutina> = emptyList(),
    /** Día (1-7) para el que se está eligiendo rutina; null = selector cerrado. */
    val diaEditando: Int? = null,
    val guardando: Boolean = false,
    val porConfirmar: AsignacionPorConfirmar? = null,
    /** Resultado del último cambio: guardado, avisos de 48 h o error. */
    val notas: List<Nota> = emptyList()
)

class PlanSemanalViewModel(
    private val plan: PlanRepository,
    private val rutinas: RutinasRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(PlanSemanalUiState())
    val estado: StateFlow<PlanSemanalUiState> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            val resultadoPlan = plan.obtenerPlan()
            val resultadoRutinas = rutinas.listar()
            when {
                resultadoPlan is Resultado.Fallo ->
                    _estado.update { it.copy(cargando = false, error = resultadoPlan.mensaje) }
                resultadoRutinas is Resultado.Fallo ->
                    _estado.update { it.copy(cargando = false, error = resultadoRutinas.mensaje) }
                else -> _estado.update {
                    it.copy(
                        cargando = false,
                        dias = (resultadoPlan as Resultado.Exito).datos,
                        rutinas = (resultadoRutinas as Resultado.Exito).datos
                    )
                }
            }
        }
    }

    /** Al volver al plan (por ejemplo, del detalle de un día) se recarga sin pantalla de carga. */
    fun refrescar() {
        if (_estado.value.cargando || _estado.value.guardando) return
        viewModelScope.launch { recargarPlan() }
    }

    fun abrirSelector(dia: Int) = _estado.update { it.copy(diaEditando = dia, notas = emptyList()) }

    fun cerrarSelector() = _estado.update { it.copy(diaEditando = null) }

    fun cerrarNota(nota: Nota) = _estado.update { it.copy(notas = it.notas - nota) }

    /**
     * Antes de asignar se piden los avisos de 48 h de la rutina en ese día. Sin avisos se asigna
     * directo; con avisos se pide confirmar (la regla solo avisa: siempre se puede seguir).
     */
    fun elegirRutina(idRutina: Int) {
        val dia = _estado.value.diaEditando ?: return
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, diaEditando = null, notas = emptyList()) }
            when (val r = plan.previsualizarRutina(idRutina)) {
                is Resultado.Exito -> {
                    val avisos = r.datos.firstOrNull { it.dia == dia }?.avisos.orEmpty()
                    if (avisos.isEmpty()) {
                        asignar(dia, idRutina)
                    } else {
                        val nombreRutina = _estado.value.rutinas.firstOrNull { it.id == idRutina }?.nombre.orEmpty()
                        _estado.update {
                            it.copy(
                                guardando = false,
                                porConfirmar = AsignacionPorConfirmar(dia, nombreDia(dia), idRutina, nombreRutina, avisos)
                            )
                        }
                    }
                }
                is Resultado.Fallo -> _estado.update {
                    it.copy(guardando = false, notas = listOf(Nota(r.mensaje, TipoNota.ERROR)))
                }
            }
        }
    }

    /** "Asignar igual". */
    fun confirmarAsignacion() {
        val p = _estado.value.porConfirmar ?: return
        viewModelScope.launch {
            _estado.update { it.copy(porConfirmar = null, guardando = true) }
            asignar(p.dia, p.idRutina)
        }
    }

    fun cancelarAsignacion() = _estado.update { it.copy(porConfirmar = null) }

    /** PUT api/plan/{dia}: guarda siempre y devuelve los avisos de 48 h, que se muestran. */
    private suspend fun asignar(dia: Int, idRutina: Int) {
        when (val r = plan.asignar(dia, idRutina)) {
            is Resultado.Exito -> {
                val nombreRutina = r.datos.dia.rutina?.nombre
                val texto = if (nombreRutina.isNullOrBlank()) "Plan actualizado" else "Plan actualizado: el ${nombreDia(dia)} toca $nombreRutina."
                _estado.update {
                    it.copy(guardando = false, notas = listOf(Nota(texto, TipoNota.EXITO)) + notasDeAvisos(r.datos.avisos))
                }
                recargarPlan()
            }
            is Resultado.Fallo -> _estado.update {
                it.copy(guardando = false, notas = listOf(Nota(r.mensaje, TipoNota.ERROR)))
            }
        }
    }

    fun dejarEnDescanso() {
        val dia = _estado.value.diaEditando ?: return
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, diaEditando = null, notas = emptyList()) }
            when (val r = plan.quitar(dia)) {
                is Resultado.Exito -> {
                    _estado.update { it.copy(guardando = false, notas = listOf(Nota("Día en descanso", TipoNota.EXITO))) }
                    recargarPlan()
                }
                is Resultado.Fallo ->
                    _estado.update { it.copy(guardando = false, notas = listOf(Nota(r.mensaje, TipoNota.ERROR))) }
            }
        }
    }

    private fun nombreDia(dia: Int): String =
        _estado.value.dias.firstOrNull { it.diaSemana == dia }?.nombreDia?.ifBlank { null } ?: nombreDiaSemana(dia)

    private suspend fun recargarPlan() {
        val r = plan.obtenerPlan()
        if (r is Resultado.Exito) _estado.update { it.copy(dias = r.datos) }
    }
}
