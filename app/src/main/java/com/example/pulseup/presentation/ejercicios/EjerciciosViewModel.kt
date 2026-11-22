package com.example.pulseup.presentation.ejercicios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.Ejercicio
import com.example.pulseup.domain.model.FiltrosEjercicio
import com.example.pulseup.domain.repository.EjerciciosRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EjerciciosUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val ejercicios: List<Ejercicio> = emptyList(),
    val filtros: FiltrosEjercicio? = null,
    val musculo: String? = null,
    val equipo: String? = null,
    val texto: String = ""
)

class EjerciciosViewModel(private val repositorio: EjerciciosRepository) : ViewModel() {

    private val _estado = MutableStateFlow(EjerciciosUiState())
    val estado: StateFlow<EjerciciosUiState> = _estado.asStateFlow()

    private var busqueda: Job? = null

    init {
        cargarFiltros()
        buscar(conEspera = false)
    }

    fun elegirMusculo(musculo: String?) {
        _estado.update { it.copy(musculo = musculo) }
        buscar(conEspera = false)
    }

    fun elegirEquipo(equipo: String?) {
        _estado.update { it.copy(equipo = equipo) }
        buscar(conEspera = false)
    }

    fun cambiarTexto(texto: String) {
        _estado.update { it.copy(texto = texto) }
        buscar(conEspera = true) // espera un momento a que termine de escribir
    }

    fun reintentar() {
        if (_estado.value.filtros == null) cargarFiltros()
        buscar(conEspera = false)
    }

    private fun cargarFiltros() {
        viewModelScope.launch {
            val resultado = repositorio.filtros()
            if (resultado is Resultado.Exito) {
                _estado.update { it.copy(filtros = resultado.datos) }
            }
        }
    }

    private fun buscar(conEspera: Boolean) {
        busqueda?.cancel()
        busqueda = viewModelScope.launch {
            if (conEspera) delay(300)
            _estado.update { it.copy(cargando = true, error = null) }
            val actual = _estado.value
            when (val resultado = repositorio.listar(actual.musculo, actual.equipo, null, actual.texto)) {
                is Resultado.Exito ->
                    _estado.update { it.copy(cargando = false, ejercicios = resultado.datos) }
                is Resultado.Fallo ->
                    _estado.update { it.copy(cargando = false, error = resultado.mensaje) }
            }
        }
    }
}
