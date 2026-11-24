package com.example.pulseup.presentation.ejercicios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.EjercicioDetalle
import com.example.pulseup.domain.repository.EjerciciosRepository
import com.example.pulseup.domain.repository.PerfilFisicoRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EjercicioDetalleUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val ejercicio: EjercicioDetalle? = null,
    /** true si el usuario no tiene perfil físico; null si no se pudo saber (falló la consulta). */
    val sinPerfilFisico: Boolean? = null
) {
    /**
     * El botón "Ir a mi perfil físico" solo tiene sentido si falta el perfil. Si el perfil existe y aun
     * así no hay recomendación, es que el ejercicio no la tiene: se muestra solo el motivo.
     */
    val mostrarIrAPerfil: Boolean
        get() = ejercicio != null && ejercicio.recomendacion == null && sinPerfilFisico == true
}

class EjercicioDetalleViewModel(
    private val repositorio: EjerciciosRepository,
    private val perfilFisico: PerfilFisicoRepository,
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
            // Detalle y perfil a la vez: el perfil solo decide si se ofrece ir a llenarlo
            val perfil = async { consultarSinPerfil() }
            val resultado = repositorio.detalle(id)
            val sinPerfil = perfil.await()
            when (resultado) {
                is Resultado.Exito -> _estado.update {
                    it.copy(cargando = false, ejercicio = resultado.datos, sinPerfilFisico = sinPerfil)
                }
                is Resultado.Fallo -> _estado.update { it.copy(cargando = false, error = resultado.mensaje) }
            }
        }
    }

    /** true = no tiene perfil; false = sí lo tiene; null = no se pudo consultar. */
    private suspend fun consultarSinPerfil(): Boolean? =
        (perfilFisico.obtener() as? Resultado.Exito)?.let { it.datos == null }

    /**
     * Al volver a la pantalla (por ejemplo, después de llenar el perfil físico): si no había
     * recomendación, se pide otra vez el detalle sin quitar lo que ya se ve. Si falla, se deja como estaba.
     */
    fun refrescarRecomendacion() {
        val actual = _estado.value.ejercicio ?: return
        if (actual.recomendacion != null || _estado.value.cargando) return
        viewModelScope.launch {
            val perfil = async { consultarSinPerfil() }
            val resultado = repositorio.detalle(id)
            val sinPerfil = perfil.await()
            if (resultado is Resultado.Exito) {
                _estado.update { it.copy(ejercicio = resultado.datos, sinPerfilFisico = sinPerfil ?: it.sinPerfilFisico) }
            }
        }
    }
}
