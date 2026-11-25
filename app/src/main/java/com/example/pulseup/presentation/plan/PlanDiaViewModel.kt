package com.example.pulseup.presentation.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.DiaPlan
import com.example.pulseup.domain.model.EjercicioPlan
import com.example.pulseup.domain.model.MAX_EJERCICIOS_POR_DIA
import com.example.pulseup.domain.model.nombreDiaSemana
import com.example.pulseup.domain.repository.EjerciciosRepository
import com.example.pulseup.domain.repository.PlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlanDiaUiState(
    val diaSemana: Int,
    val cargando: Boolean = true,
    val error: String? = null,
    val dia: DiaPlan? = null,
    /** null = sin edición abierta. */
    val edicion: EdicionEjercicio? = null,
    /** Ejercicio que espera la confirmación para quitarse. */
    val porQuitar: EjercicioPlan? = null,
    /** Agregando o quitando: se desactivan los botones. */
    val ocupado: Boolean = false,
    val notas: List<Nota> = emptyList()
) {
    val nombreDia: String get() = dia?.nombreDia?.ifBlank { null } ?: nombreDiaSemana(diaSemana)
    val ejercicios: List<EjercicioPlan> get() = dia?.ejercicios.orEmpty()
    val lleno: Boolean get() = ejercicios.size >= MAX_EJERCICIOS_POR_DIA
}

/**
 * Detalle de un día del plan: sus ejercicios, editarlos, quitarlos y agregar otros del catálogo.
 * Tras cada cambio se recarga el plan desde GET api/plan/ (el item_id puede cambiar si la rutina se copia).
 */
class PlanDiaViewModel(
    private val plan: PlanRepository,
    private val ejercicios: EjerciciosRepository,
    private val diaSemana: Int
) : ViewModel() {

    private val _estado = MutableStateFlow(PlanDiaUiState(diaSemana))
    val estado: StateFlow<PlanDiaUiState> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            when (val r = plan.obtenerPlan()) {
                is Resultado.Exito -> _estado.update {
                    it.copy(cargando = false, dia = r.datos.firstOrNull { d -> d.diaSemana == diaSemana })
                }
                is Resultado.Fallo -> _estado.update { it.copy(cargando = false, error = r.mensaje) }
            }
        }
    }

    /** Recarga sin pantalla de carga; si falla, se deja lo que se ve. */
    private suspend fun recargar() {
        val r = plan.obtenerPlan()
        if (r is Resultado.Exito) _estado.update { it.copy(dia = r.datos.firstOrNull { d -> d.diaSemana == diaSemana }) }
    }

    fun cerrarNota(nota: Nota) = _estado.update { it.copy(notas = it.notas - nota) }

    // ---------- Editar ----------

    /** Abre la edición y pide la recomendación del ejercicio como guía (sin imponerla). */
    fun abrirEdicion(item: EjercicioPlan) {
        _estado.update { it.copy(edicion = EdicionEjercicio.desde(item), notas = emptyList()) }
        viewModelScope.launch {
            val r = ejercicios.recomendacion(item.ejercicioId)
            actualizarEdicion(item) {
                it.copy(cargandoRecomendacion = false, recomendacion = (r as? Resultado.Exito)?.datos)
            }
        }
    }

    fun cerrarEdicion() {
        if (_estado.value.edicion?.guardando == true) return
        _estado.update { it.copy(edicion = null) }
    }

    fun cambiarSeries(valor: String) = cambiarCampo { it.copy(series = valor.filter(Char::isDigit).take(2)) }
    fun cambiarRepeticiones(valor: String) = cambiarCampo { it.copy(repeticiones = valor.take(MAX_LARGO_REPETICIONES)) }
    fun cambiarPeso(valor: String) = cambiarCampo { it.copy(peso = valor.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(6)) }
    fun cambiarDescanso(valor: String) = cambiarCampo { it.copy(descanso = valor.filter(Char::isDigit).take(3)) }

    private fun cambiarCampo(cambio: (EdicionEjercicio) -> EdicionEjercicio) =
        _estado.update { e -> e.edicion?.let { e.copy(edicion = cambio(it).copy(error = null)) } ?: e }

    fun guardarEdicion() {
        val edicion = _estado.value.edicion ?: return
        if (edicion.guardando) return
        val cambios = when (val v = validarEdicion(edicion)) {
            is ValidacionEdicion.Invalida -> {
                _estado.update { it.copy(edicion = edicion.copy(error = v.mensaje)) }
                return
            }
            is ValidacionEdicion.Valida -> v.cambios
        }
        viewModelScope.launch {
            _estado.update { it.copy(edicion = edicion.copy(guardando = true, error = null)) }
            when (val r = plan.editarEjercicio(diaSemana, edicion.item.itemId, cambios)) {
                is Resultado.Exito -> {
                    val notas = buildList {
                        add(Nota("Cambios guardados en ${edicion.item.nombre}.", TipoNota.EXITO))
                        if (r.datos.rutinaCopiada) add(Nota(textoRutinaCopiada(_estado.value.nombreDia), TipoNota.INFO))
                    }
                    _estado.update { it.copy(edicion = null, notas = notas) }
                    recargar()
                }
                is Resultado.Fallo -> _estado.update {
                    it.copy(edicion = edicion.copy(guardando = false, error = r.mensaje))
                }
            }
        }
    }

    private fun actualizarEdicion(item: EjercicioPlan, cambio: (EdicionEjercicio) -> EdicionEjercicio) =
        _estado.update { e ->
            val actual = e.edicion
            if (actual != null && actual.item.itemId == item.itemId) e.copy(edicion = cambio(actual)) else e
        }

    // ---------- Quitar ----------

    fun pedirQuitar(item: EjercicioPlan) = _estado.update { it.copy(porQuitar = item) }

    fun cancelarQuitar() = _estado.update { it.copy(porQuitar = null) }

    fun confirmarQuitar() {
        val item = _estado.value.porQuitar ?: return
        if (_estado.value.ocupado) return
        viewModelScope.launch {
            _estado.update { it.copy(porQuitar = null, ocupado = true, notas = emptyList()) }
            when (val r = plan.quitarEjercicio(diaSemana, item.itemId)) {
                is Resultado.Exito -> {
                    val nombreDia = _estado.value.nombreDia
                    val notas = buildList {
                        add(Nota("Quitaste ${item.nombre}.", TipoNota.EXITO))
                        if (r.datos.diaEnDescanso) add(Nota("El $nombreDia quedó en descanso.", TipoNota.INFO))
                        if (r.datos.rutinaCopiada) add(Nota(textoRutinaCopiada(nombreDia), TipoNota.INFO))
                    }
                    _estado.update { it.copy(ocupado = false, notas = notas) }
                    recargar()
                }
                is Resultado.Fallo -> _estado.update {
                    it.copy(ocupado = false, notas = listOf(Nota(r.mensaje, TipoNota.ERROR)))
                }
            }
        }
    }

    // ---------- Agregar desde el catálogo ----------

    /** Llega al volver del catálogo con el ejercicio elegido. Los avisos de 48 h solo informan. */
    fun agregar(ejercicioId: Int) {
        if (_estado.value.ocupado) return
        viewModelScope.launch {
            _estado.update { it.copy(ocupado = true, notas = emptyList()) }
            when (val r = plan.agregarEjercicio(diaSemana, ejercicioId)) {
                is Resultado.Exito -> {
                    val nombreDia = _estado.value.nombreDia
                    val notas = buildList {
                        add(Nota("Agregaste ${r.datos.item.nombre} al $nombreDia.", TipoNota.EXITO))
                        addAll(notasDeAvisos(r.datos.avisos))
                        if (r.datos.rutinaCopiada) add(Nota(textoRutinaCopiada(nombreDia), TipoNota.INFO))
                    }
                    _estado.update { it.copy(ocupado = false, notas = notas) }
                    recargar()
                }
                is Resultado.Fallo -> {
                    val mensaje = if (r.codigo == 409) textoYaEstaEnElDia(_estado.value.nombreDia) else r.mensaje
                    _estado.update { it.copy(ocupado = false, notas = listOf(Nota(mensaje, TipoNota.ERROR))) }
                }
            }
        }
    }
}
