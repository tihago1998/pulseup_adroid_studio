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

data class RutinaDetalleUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val rutina: Rutina? = null,
    val eliminando: Boolean = false,
    val eliminada: Boolean = false,
    val errorAlEliminar: String? = null
)

class RutinaDetalleViewModel(
    private val repositorio: RutinasRepository,
    private val id: Int
) : ViewModel() {

    private val _estado = MutableStateFlow(RutinaDetalleUiState())
    val estado: StateFlow<RutinaDetalleUiState> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            when (val resultado = repositorio.detalle(id)) {
                is Resultado.Exito ->
                    _estado.update { it.copy(cargando = false, rutina = resultado.datos) }
                is Resultado.Fallo ->
                    _estado.update { it.copy(cargando = false, error = resultado.mensaje) }
            }
        }
    }

    fun eliminar() {
        if (_estado.value.eliminando) return
        viewModelScope.launch {
            _estado.update { it.copy(eliminando = true, errorAlEliminar = null) }
            when (val resultado = repositorio.eliminar(id)) {
                is Resultado.Exito ->
                    _estado.update { it.copy(eliminando = false, eliminada = true) }
                is Resultado.Fallo ->
                    _estado.update { it.copy(eliminando = false, errorAlEliminar = resultado.mensaje) }
            }
        }
    }
}
