package com.example.pulseup.presentation.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.CalendarioMes
import com.example.pulseup.domain.model.DiaCalendario
import com.example.pulseup.domain.model.EstadoDia
import com.example.pulseup.domain.repository.PlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class CalendarioUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val mes: YearMonth = YearMonth.now(),
    val calendario: CalendarioMes? = null,
    val seleccionado: DiaCalendario? = null,
    val completando: Boolean = false,
    val aviso: String? = null,
    val avisoEsError: Boolean = false
) {
    /** El día de hoy dentro del mes cargado, si el mes visible es el actual. */
    val hoy: DiaCalendario?
        get() = calendario?.dias?.firstOrNull { it.fecha == LocalDate.now().toString() }

    /** Solo se puede marcar el día que toca: hoy, con rutina y sin cumplir. */
    val puedeMarcarHoy: Boolean
        get() = hoy?.let { it.estado == EstadoDia.PENDIENTE && it.rutina != null } == true
}

/** Sirve a la pantalla del calendario y a la tarjeta "Hoy" del Home. */
class CalendarioViewModel(private val plan: PlanRepository) : ViewModel() {

    private val _estado = MutableStateFlow(CalendarioUiState())
    val estado: StateFlow<CalendarioUiState> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            when (val r = plan.calendario(_estado.value.mes.toString())) {
                is Resultado.Exito -> _estado.update { it.copy(cargando = false, calendario = r.datos) }
                is Resultado.Fallo -> _estado.update { it.copy(cargando = false, error = r.mensaje) }
            }
        }
    }

    fun mesAnterior() = cambiarMes(_estado.value.mes.minusMonths(1))

    fun mesSiguiente() = cambiarMes(_estado.value.mes.plusMonths(1))

    private fun cambiarMes(mes: YearMonth) {
        _estado.update { it.copy(mes = mes, seleccionado = null) }
        cargar()
    }

    fun seleccionar(dia: DiaCalendario) = _estado.update { it.copy(seleccionado = dia) }

    fun cerrarAviso() = _estado.update { it.copy(aviso = null) }

    fun marcarHoy() {
        if (_estado.value.completando) return
        viewModelScope.launch {
            _estado.update { it.copy(completando = true, aviso = null) }
            when (val r = plan.completarHoy()) {
                is Resultado.Exito -> {
                    _estado.update { it.copy(completando = false, aviso = r.datos.mensaje, avisoEsError = false) }
                    cargar()
                }
                is Resultado.Fallo ->
                    _estado.update { it.copy(completando = false, aviso = r.mensaje, avisoEsError = true) }
            }
        }
    }
}
