package com.example.pulseup.presentation.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.DiaPlan
import com.example.pulseup.domain.model.Rutina
import com.example.pulseup.domain.repository.PlanRepository
import com.example.pulseup.domain.repository.RutinasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlanSemanalUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val dias: List<DiaPlan> = emptyList(),
    val rutinas: List<Rutina> = emptyList(),
    /** Día (1-7) para el que se está eligiendo rutina; null = selector cerrado. */
    val diaEditando: Int? = null,
    val guardando: Boolean = false,
    /** Aviso para el usuario: conflicto de 48 h o confirmación. */
    val aviso: String? = null,
    val avisoEsError: Boolean = false
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

    fun abrirSelector(dia: Int) = _estado.update { it.copy(diaEditando = dia, aviso = null) }

    fun cerrarSelector() = _estado.update { it.copy(diaEditando = null) }

    fun cerrarAviso() = _estado.update { it.copy(aviso = null) }

    fun elegirRutina(idRutina: Int) {
        val dia = _estado.value.diaEditando ?: return
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, diaEditando = null) }
            when (val r = plan.asignar(dia, idRutina)) {
                is Resultado.Exito -> {
                    _estado.update { it.copy(guardando = false, aviso = "Plan actualizado", avisoEsError = false) }
                    recargarPlan()
                }
                // Aquí llega el mensaje de la regla de 48 h, por ejemplo: "Pecho ya se entrena el domingo…"
                is Resultado.Fallo ->
                    _estado.update { it.copy(guardando = false, aviso = r.mensaje, avisoEsError = true) }
            }
        }
    }

    fun dejarEnDescanso() {
        val dia = _estado.value.diaEditando ?: return
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, diaEditando = null) }
            when (val r = plan.quitar(dia)) {
                is Resultado.Exito -> {
                    _estado.update { it.copy(guardando = false, aviso = "Día en descanso", avisoEsError = false) }
                    recargarPlan()
                }
                is Resultado.Fallo ->
                    _estado.update { it.copy(guardando = false, aviso = r.mensaje, avisoEsError = true) }
            }
        }
    }

    private suspend fun recargarPlan() {
        val r = plan.obtenerPlan()
        if (r is Resultado.Exito) _estado.update { it.copy(dias = r.datos) }
    }
}
