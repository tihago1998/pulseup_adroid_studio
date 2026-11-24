package com.example.pulseup.presentation.rutinas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.AvisoDescanso
import com.example.pulseup.domain.model.DiaPrevisualizado
import com.example.pulseup.domain.model.Ejercicio
import com.example.pulseup.domain.model.ItemNuevaRutina
import com.example.pulseup.domain.model.NuevaRutina
import com.example.pulseup.domain.model.nombreDiaSemana
import com.example.pulseup.domain.repository.EjerciciosRepository
import com.example.pulseup.domain.repository.PlanRepository
import com.example.pulseup.domain.repository.RutinasRepository
import com.example.pulseup.presentation.plan.Nota
import com.example.pulseup.presentation.plan.TipoNota
import com.example.pulseup.presentation.plan.notasDeAvisos
import com.example.pulseup.presentation.plan.unirDias
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Un ejercicio ya elegido para la rutina, con los valores que el usuario está escribiendo. */
data class ItemEnEdicion(
    val ejercicio: Ejercicio,
    val series: String = "3",
    val repeticiones: String = "10",
    val descanso: String = "60"
)

data class CrearRutinaUiState(
    val nombre: String = "",
    val descripcion: String = "",
    val objetivo: String = "general",
    val nivel: String = "principiante",
    val duracion: String = "45",
    val items: List<ItemEnEdicion> = emptyList(),
    val catalogo: List<Ejercicio> = emptyList(),
    val guardando: Boolean = false,
    val error: String? = null,
    /** Hoja "¿Quieres ponerla en tu plan?" tras guardar; null = cerrada. */
    val ofrecerPlan: OfrecerPlanUiState? = null,
    /** true = terminar y volver (después de la hoja del plan). */
    val creada: Boolean = false
)

/** Días del plan para la rutina recién creada (se pueden elegir varios). */
data class OfrecerPlanUiState(
    val idRutina: Int,
    val cargando: Boolean = true,
    val error: String? = null,
    val dias: List<DiaPrevisualizado> = emptyList(),
    val seleccionados: Set<Int> = emptySet(),
    val guardando: Boolean = false,
    /** Resumen al terminar (días agregados, avisos de 48 h y errores); null = aún eligiendo. */
    val resumen: List<Nota>? = null
)

class CrearRutinaViewModel(
    private val rutinas: RutinasRepository,
    private val ejercicios: EjerciciosRepository,
    private val plan: PlanRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(CrearRutinaUiState())
    val estado: StateFlow<CrearRutinaUiState> = _estado.asStateFlow()

    init {
        // El catálogo se usa para el selector "Agregar ejercicio"
        viewModelScope.launch {
            val resultado = ejercicios.listar()
            if (resultado is Resultado.Exito) {
                _estado.update { it.copy(catalogo = resultado.datos) }
            }
        }
    }

    fun cambiarNombre(valor: String) = _estado.update { it.copy(nombre = valor, error = null) }
    fun cambiarDescripcion(valor: String) = _estado.update { it.copy(descripcion = valor) }
    fun cambiarObjetivo(valor: String) = _estado.update { it.copy(objetivo = valor) }
    fun cambiarNivel(valor: String) = _estado.update { it.copy(nivel = valor) }
    fun cambiarDuracion(valor: String) = _estado.update { it.copy(duracion = valor.filter(Char::isDigit), error = null) }

    fun agregar(ejercicio: Ejercicio) =
        _estado.update { it.copy(items = it.items + ItemEnEdicion(ejercicio), error = null) }

    fun quitar(indice: Int) =
        _estado.update { it.copy(items = it.items.filterIndexed { i, _ -> i != indice }) }

    fun cambiarSeries(indice: Int, valor: String) =
        modificar(indice) { it.copy(series = valor.filter(Char::isDigit)) }

    fun cambiarRepeticiones(indice: Int, valor: String) =
        modificar(indice) { it.copy(repeticiones = valor) }

    fun cambiarDescanso(indice: Int, valor: String) =
        modificar(indice) { it.copy(descanso = valor.filter(Char::isDigit)) }

    private fun modificar(indice: Int, cambio: (ItemEnEdicion) -> ItemEnEdicion) {
        _estado.update { e ->
            e.copy(items = e.items.mapIndexed { i, item -> if (i == indice) cambio(item) else item }, error = null)
        }
    }

    private fun fallar(mensaje: String) = _estado.update { it.copy(error = mensaje) }

    fun guardar() {
        val e = _estado.value
        if (e.guardando) return

        // Se valida aquí para avisar rápido; el backend vuelve a validar todo
        val nombre = e.nombre.trim()
        if (nombre.isEmpty()) return fallar("Escribe un nombre para la rutina")
        if (e.items.isEmpty()) return fallar("Agrega al menos un ejercicio")
        val duracion = e.duracion.toIntOrNull()?.takeIf { it in 5..240 }
            ?: return fallar("La duración debe estar entre 5 y 240 minutos")

        val items = mutableListOf<ItemNuevaRutina>()
        for (item in e.items) {
            val series = item.series.toIntOrNull()?.takeIf { it in 1..10 }
                ?: return fallar("Las series de ${item.ejercicio.nombre} deben estar entre 1 y 10")
            val repeticiones = item.repeticiones.trim()
            if (repeticiones.isEmpty()) return fallar("Escribe las repeticiones de ${item.ejercicio.nombre}")
            val descanso = item.descanso.toIntOrNull()?.takeIf { it in 0..600 }
                ?: return fallar("El descanso de ${item.ejercicio.nombre} debe estar entre 0 y 600 segundos")
            items += ItemNuevaRutina(item.ejercicio.id, series, repeticiones, descanso)
        }

        val nueva = NuevaRutina(nombre, e.descripcion.trim(), e.objetivo, e.nivel, duracion, items)
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, error = null) }
            when (val resultado = rutinas.crear(nueva)) {
                is Resultado.Exito -> {
                    _estado.update { it.copy(guardando = false, ofrecerPlan = OfrecerPlanUiState(resultado.datos)) }
                    cargarDiasPlan()
                }
                is Resultado.Fallo -> _estado.update { it.copy(guardando = false, error = resultado.mensaje) }
            }
        }
    }

    // ---------- ¿Quieres ponerla en tu plan? ----------

    fun reintentarDiasPlan() = cargarDiasPlan()

    private fun cargarDiasPlan() {
        val id = _estado.value.ofrecerPlan?.idRutina ?: return
        viewModelScope.launch {
            actualizarOferta { it.copy(cargando = true, error = null) }
            when (val r = plan.previsualizarRutina(id)) {
                is Resultado.Exito -> actualizarOferta { it.copy(cargando = false, dias = r.datos) }
                is Resultado.Fallo -> actualizarOferta { it.copy(cargando = false, error = r.mensaje) }
            }
        }
    }

    fun alternarDia(dia: DiaPrevisualizado) = actualizarOferta {
        when {
            it.guardando || it.resumen != null -> it
            dia.dia in it.seleccionados -> it.copy(seleccionados = it.seleccionados - dia.dia)
            else -> it.copy(seleccionados = it.seleccionados + dia.dia)
        }
    }

    /** Asigna la rutina a cada día elegido (PUT api/plan/{dia}) y resume avisos y errores al terminar. */
    fun agregarAlPlan() {
        val oferta = _estado.value.ofrecerPlan ?: return
        if (oferta.guardando || oferta.seleccionados.isEmpty()) return
        viewModelScope.launch {
            actualizarOferta { it.copy(guardando = true) }
            val agregados = mutableListOf<String>()
            val avisos = mutableListOf<AvisoDescanso>()
            val errores = mutableListOf<Nota>()
            for (dia in oferta.seleccionados.sorted()) {
                val nombre = oferta.dias.firstOrNull { it.dia == dia }?.nombreDia?.ifBlank { null } ?: nombreDiaSemana(dia)
                when (val r = plan.asignar(dia, oferta.idRutina)) {
                    is Resultado.Exito -> {
                        agregados += nombre
                        avisos += r.datos.avisos
                    }
                    is Resultado.Fallo -> errores += Nota("No se pudo agregar al $nombre: ${r.mensaje}", TipoNota.ERROR)
                }
            }
            val resumen = buildList {
                if (agregados.isNotEmpty()) add(Nota("Rutina agregada a tu plan: ${unirDias(agregados)}.", TipoNota.EXITO))
                addAll(notasDeAvisos(avisos))
                addAll(errores)
            }
            actualizarOferta { it.copy(guardando = false, resumen = resumen) }
        }
    }

    /** "Ahora no" o "Listo": la rutina ya está guardada, se vuelve a la lista. */
    fun terminar() {
        if (_estado.value.ofrecerPlan?.guardando == true) return
        _estado.update { it.copy(ofrecerPlan = null, creada = true) }
    }

    private fun actualizarOferta(cambio: (OfrecerPlanUiState) -> OfrecerPlanUiState) =
        _estado.update { e -> e.ofrecerPlan?.let { e.copy(ofrecerPlan = cambio(it)) } ?: e }
}
