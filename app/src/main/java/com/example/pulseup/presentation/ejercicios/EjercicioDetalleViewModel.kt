package com.example.pulseup.presentation.ejercicios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.AvisoDescanso
import com.example.pulseup.domain.model.DiaPrevisualizado
import com.example.pulseup.domain.model.EjercicioDetalle
import com.example.pulseup.domain.repository.EjerciciosRepository
import com.example.pulseup.domain.repository.PerfilFisicoRepository
import com.example.pulseup.domain.repository.PlanRepository
import com.example.pulseup.presentation.plan.textoYaEstaEnElDia
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Hoja "¿Qué día?" de "Agregar a mi plan". */
data class HojaPlanUiState(
    val cargando: Boolean = true,
    /** No se pudieron leer los días (con Reintentar). */
    val error: String? = null,
    val dias: List<DiaPrevisualizado> = emptyList(),
    /** Día elegido que tiene avisos de 48 h: se pide confirmar con "Agregar igual". */
    val porConfirmar: DiaPrevisualizado? = null,
    val guardando: Boolean = false,
    /** No se pudo agregar (por ejemplo, ya estaba ese día). La hoja sigue abierta para elegir otro. */
    val errorAgregar: String? = null
)

/** Confirmación tras agregar, con el botón "Ver día". */
data class AgregadoAlPlan(
    val dia: Int,
    val nombreDia: String,
    val avisos: List<AvisoDescanso>,
    val rutinaCopiada: Boolean
)

data class EjercicioDetalleUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val ejercicio: EjercicioDetalle? = null,
    /** true si el usuario no tiene perfil físico; null si no se pudo saber (falló la consulta). */
    val sinPerfilFisico: Boolean? = null,
    /** null = hoja cerrada. */
    val hojaPlan: HojaPlanUiState? = null,
    val agregado: AgregadoAlPlan? = null
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
    private val plan: PlanRepository,
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

    // ---------- Agregar a mi plan ----------

    /** Abre "¿Qué día?" y pide los avisos de 48 h que tendría el ejercicio en cada día. */
    fun abrirHojaPlan() {
        _estado.update { it.copy(hojaPlan = HojaPlanUiState(), agregado = null) }
        cargarDias()
    }

    fun reintentarHojaPlan() = cargarDias()

    fun cerrarHojaPlan() {
        if (_estado.value.hojaPlan?.guardando == true) return
        _estado.update { it.copy(hojaPlan = null) }
    }

    private fun cargarDias() {
        viewModelScope.launch {
            actualizarHoja { it.copy(cargando = true, error = null) }
            when (val r = plan.previsualizarEjercicio(id)) {
                is Resultado.Exito -> actualizarHoja { it.copy(cargando = false, dias = r.datos) }
                is Resultado.Fallo -> actualizarHoja { it.copy(cargando = false, error = r.mensaje) }
            }
        }
    }

    /** Sin avisos se agrega directo; con avisos se pide confirmar (la regla solo avisa, no bloquea). */
    fun elegirDia(dia: DiaPrevisualizado) {
        val hoja = _estado.value.hojaPlan ?: return
        if (hoja.guardando) return
        if (dia.avisos.isEmpty()) agregarAlDia(dia)
        else actualizarHoja { it.copy(porConfirmar = dia, errorAgregar = null) }
    }

    /** "Agregar igual". */
    fun confirmarDia() {
        val dia = _estado.value.hojaPlan?.porConfirmar ?: return
        agregarAlDia(dia)
    }

    fun cancelarConfirmacion() = actualizarHoja { it.copy(porConfirmar = null) }

    fun cerrarAgregado() = _estado.update { it.copy(agregado = null) }

    private fun agregarAlDia(dia: DiaPrevisualizado) {
        viewModelScope.launch {
            actualizarHoja { it.copy(guardando = true, porConfirmar = null, errorAgregar = null) }
            // Sin series ni peso: el backend los rellena con la recomendación del usuario
            when (val r = plan.agregarEjercicio(dia.dia, id)) {
                is Resultado.Exito -> _estado.update {
                    it.copy(
                        hojaPlan = null,
                        agregado = AgregadoAlPlan(dia.dia, dia.nombreDia, r.datos.avisos, r.datos.rutinaCopiada)
                    )
                }
                is Resultado.Fallo -> {
                    val mensaje = if (r.codigo == 409) textoYaEstaEnElDia(dia.nombreDia) else r.mensaje
                    actualizarHoja { it.copy(guardando = false, errorAgregar = mensaje) }
                }
            }
        }
    }

    private fun actualizarHoja(cambio: (HojaPlanUiState) -> HojaPlanUiState) =
        _estado.update { e -> e.hojaPlan?.let { e.copy(hojaPlan = cambio(it)) } ?: e }
}
