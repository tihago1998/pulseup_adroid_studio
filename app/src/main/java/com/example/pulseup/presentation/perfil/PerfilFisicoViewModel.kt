package com.example.pulseup.presentation.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.PerfilFisico
import com.example.pulseup.domain.model.ValoresPerfilFisico
import com.example.pulseup.domain.repository.PerfilFisicoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PerfilFisicoUiState(
    val cargando: Boolean = true,
    /** Error al cargar el perfil: se muestra EstadoError con Reintentar. */
    val errorCarga: String? = null,
    /** true si el backend ya tiene un perfil guardado (se muestra "Borrar mis datos"). */
    val tienePerfil: Boolean = false,
    val fechaNacimiento: LocalDate? = null,
    val sexo: String = ValoresPerfilFisico.SEXO_POR_DEFECTO,
    val peso: String = "",
    val estatura: String = "",
    val nivel: String? = null,
    val errores: ErroresPerfilFisico = ErroresPerfilFisico(),
    val guardando: Boolean = false,
    val borrando: Boolean = false,
    /** Mensaje del backend al guardar o borrar (por ejemplo, un 400). */
    val errorServidor: String? = null,
    /** Confirmación corta, por ejemplo después de borrar. */
    val aviso: String? = null,
    /** Pasa a true cuando el PUT sale bien: la pantalla confirma y vuelve atrás. */
    val guardado: Boolean = false
)

class PerfilFisicoViewModel(
    private val repositorio: PerfilFisicoRepository,
    private val hoy: () -> LocalDate = { LocalDate.now() }
) : ViewModel() {

    private val _estado = MutableStateFlow(PerfilFisicoUiState())
    val estado: StateFlow<PerfilFisicoUiState> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, errorCarga = null) }
            when (val r = repositorio.obtener()) {
                is Resultado.Exito -> _estado.value = estadoDesde(r.datos)
                is Resultado.Fallo -> _estado.update { it.copy(cargando = false, errorCarga = r.mensaje) }
            }
        }
    }

    fun cambiarFecha(fecha: LocalDate) = _estado.update {
        it.copy(fechaNacimiento = fecha, errores = it.errores.copy(fecha = null), errorServidor = null)
    }

    fun cambiarSexo(sexo: String) = _estado.update { it.copy(sexo = sexo, errorServidor = null) }

    fun cambiarPeso(texto: String) {
        if (!esEntradaDecimalValida(texto)) return
        _estado.update { it.copy(peso = texto, errores = it.errores.copy(peso = null), errorServidor = null) }
    }

    fun cambiarEstatura(texto: String) {
        if (!esEntradaDecimalValida(texto)) return
        _estado.update { it.copy(estatura = texto, errores = it.errores.copy(estatura = null), errorServidor = null) }
    }

    fun cambiarNivel(nivel: String) = _estado.update {
        it.copy(nivel = nivel, errores = it.errores.copy(nivel = null), errorServidor = null)
    }

    fun cerrarAviso() = _estado.update { it.copy(aviso = null) }

    fun guardar() {
        val actual = _estado.value
        if (actual.guardando || actual.borrando) return
        val validacion = validarPerfilFisico(
            fechaNacimiento = actual.fechaNacimiento,
            sexo = actual.sexo,
            peso = actual.peso,
            estatura = actual.estatura,
            nivel = actual.nivel,
            hoy = hoy()
        )
        when (validacion) {
            is ValidacionPerfilFisico.Invalido ->
                _estado.update { it.copy(errores = validacion.errores, errorServidor = null, aviso = null) }
            is ValidacionPerfilFisico.Valido -> viewModelScope.launch {
                _estado.update {
                    it.copy(guardando = true, errores = ErroresPerfilFisico(), errorServidor = null, aviso = null)
                }
                when (val r = repositorio.guardar(validacion.nuevo)) {
                    is Resultado.Exito -> _estado.update {
                        it.copy(guardando = false, tienePerfil = true, guardado = true)
                    }
                    is Resultado.Fallo -> _estado.update { it.copy(guardando = false, errorServidor = r.mensaje) }
                }
            }
        }
    }

    fun borrar() {
        if (_estado.value.guardando || _estado.value.borrando) return
        viewModelScope.launch {
            _estado.update { it.copy(borrando = true, errorServidor = null, aviso = null) }
            when (val r = repositorio.eliminar()) {
                // El formulario queda vacío, como si nunca lo hubiera llenado
                is Resultado.Exito -> _estado.value = PerfilFisicoUiState(
                    cargando = false,
                    aviso = "Tus datos físicos se borraron"
                )
                is Resultado.Fallo -> _estado.update { it.copy(borrando = false, errorServidor = r.mensaje) }
            }
        }
    }

    private fun estadoDesde(perfil: PerfilFisico?): PerfilFisicoUiState {
        if (perfil == null) return PerfilFisicoUiState(cargando = false)
        return PerfilFisicoUiState(
            cargando = false,
            tienePerfil = true,
            fechaNacimiento = runCatching { LocalDate.parse(perfil.fechaNacimiento) }.getOrNull(),
            sexo = perfil.sexo.takeIf { it in ValoresPerfilFisico.SEXOS } ?: ValoresPerfilFisico.SEXO_POR_DEFECTO,
            peso = formatearDecimal(perfil.pesoKg),
            estatura = formatearDecimal(perfil.estaturaCm),
            nivel = perfil.nivel.ifBlank { null }
        )
    }
}
