package com.example.pulseup.presentation.ejercicios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.CreditosCatalogo
import com.example.pulseup.domain.repository.EjerciciosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreditosUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val creditos: CreditosCatalogo? = null
)

class CreditosViewModel(private val repositorio: EjerciciosRepository) : ViewModel() {

    private val _estado = MutableStateFlow(CreditosUiState())
    val estado: StateFlow<CreditosUiState> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            when (val r = repositorio.creditos()) {
                is Resultado.Exito -> _estado.update { it.copy(cargando = false, creditos = r.datos) }
                is Resultado.Fallo -> _estado.update { it.copy(cargando = false, error = r.mensaje) }
            }
        }
    }
}
