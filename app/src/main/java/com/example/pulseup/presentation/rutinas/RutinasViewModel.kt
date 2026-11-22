package com.example.pulseup.presentation.rutinas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.Rutina
import com.example.pulseup.domain.repository.RutinasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RutinasUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val rutinas: List<Rutina> = emptyList(),
    val objetivo: String? = null
)

class RutinasViewModel(private val repositorio: RutinasRepository) : ViewModel() {

    private val _estado = MutableStateFlow(RutinasUiState())
    val estado: StateFlow<RutinasUiState> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun elegirObjetivo(objetivo: String?) {
        _estado.update { it.copy(objetivo = objetivo) }
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            when (val resultado = repositorio.listar(_estado.value.objetivo)) {
                is Resultado.Exito ->
                    _estado.update { it.copy(cargando = false, rutinas = resultado.datos) }
                is Resultado.Fallo ->
                    _estado.update { it.copy(cargando = false, error = resultado.mensaje) }
            }
        }
    }
}
