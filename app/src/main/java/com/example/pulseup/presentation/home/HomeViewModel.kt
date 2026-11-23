package com.example.pulseup.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.CalendarioMes
import com.example.pulseup.domain.model.DiaCalendario
import com.example.pulseup.domain.model.DiaDeSemana
import com.example.pulseup.domain.model.ProgresoSemana
import com.example.pulseup.domain.model.mesesDeLaSemana
import com.example.pulseup.domain.model.progresoSemana
import com.example.pulseup.domain.model.tiraSemana
import com.example.pulseup.domain.repository.PlanRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val tira: List<DiaDeSemana> = emptyList(),
    val progreso: ProgresoSemana? = null
)

/**
 * Tira de la semana y progreso del Home. Los datos salen de /api/calendario/:
 * si la semana cruza dos meses, se piden los dos y se juntan los días.
 */
class HomeViewModel(
    private val plan: PlanRepository,
    private val hoy: () -> LocalDate = { LocalDate.now() }
) : ViewModel() {

    private val _estado = MutableStateFlow(HomeUiState())
    val estado: StateFlow<HomeUiState> = _estado.asStateFlow()

    private var carga: Job? = null

    /**
     * Recarga la semana. [mesYaCargado] es el mes que acaba de traer la tarjeta "Hoy toca":
     * si sirve, se reutiliza para no pedirlo dos veces.
     */
    fun cargar(mesYaCargado: CalendarioMes? = null) {
        carga?.cancel()
        carga = viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            val fecha = hoy()
            val dias = mutableListOf<DiaCalendario>()
            for (mes in mesesDeLaSemana(fecha)) {
                val clave = mes.toString() // YYYY-MM
                if (mesYaCargado != null && mesYaCargado.mes == clave) {
                    dias += mesYaCargado.dias
                    continue
                }
                when (val r = plan.calendario(clave)) {
                    is Resultado.Exito -> dias += r.datos.dias
                    is Resultado.Fallo -> {
                        _estado.update { it.copy(cargando = false, error = r.mensaje) }
                        return@launch
                    }
                }
            }
            _estado.value = HomeUiState(
                cargando = false,
                tira = tiraSemana(dias, fecha),
                progreso = progresoSemana(dias, fecha)
            )
        }
    }
}
