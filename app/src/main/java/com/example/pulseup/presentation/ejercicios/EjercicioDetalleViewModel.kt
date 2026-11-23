package com.example.pulseup.presentation.ejercicios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.EjercicioDetalle
import com.example.pulseup.domain.repository.EjerciciosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EjercicioDetalleUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val ejercicio: EjercicioDetalle? = null
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

    /**
     * Al volver a la pantalla (por ejemplo, después de llenar el perfil físico): si no había
     * recomendación, se pide otra vez el detalle sin quitar lo que ya se ve. Si falla, se deja como estaba.
     */
    fun refrescarRecomendacion() {
        val actual = _estado.value.ejercicio ?: return
        if (actual.recomendacion != null || _estado.value.cargando) return
        viewModelScope.launch {
            val resultado = repositorio.detalle(id)
            if (resultado is Resultado.Exito) _estado.update { it.copy(ejercicio = resultado.datos) }
        }
    }
}
