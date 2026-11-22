package com.example.pulseup.presentation.ejercicios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.Ejercicio
import com.example.pulseup.domain.repository.EjerciciosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EjercicioDetalleUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val ejercicio: Ejercicio? = null
)

class EjercicioDetalleViewModel(
    private val repositorio: EjerciciosRepository,
    private val id: Int
) : ViewModel() {

    private val _estado = MutableStateFlow(EjercicioDetalleUiState())
    val estado: StateFlow<EjercicioDetalleUiState> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            when (val resultado = repositorio.detalle(id)) {
                is Resultado.Exito ->
                    _estado.update { it.copy(cargando = false, ejercicio = resultado.datos) }
                is Resultado.Fallo ->
                    _estado.update { it.copy(cargando = false, error = resultado.mensaje) }
            }
        }
    }
}
