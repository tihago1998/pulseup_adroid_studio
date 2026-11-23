package com.example.pulseup.presentation.ejercicios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.Catalogos
import com.example.pulseup.domain.model.Ejercicio
import com.example.pulseup.domain.model.FiltroCatalogo
import com.example.pulseup.domain.model.FiltrosCatalogo
import com.example.pulseup.domain.repository.EjerciciosRepository
import com.example.pulseup.domain.repository.PerfilFisicoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Ejercicios que se piden en cada página del catálogo. */
const val TAMANO_PAGINA = 20

/** Espera tras la última tecla antes de buscar, para no pedir una página por letra. */
const val ESPERA_BUSQUEDA_MS = 300L

data class CatalogoUiState(
    val filtro: FiltroCatalogo = FiltroCatalogo(),
    /** Valores para los chips (null mientras cargan o si fallaron: se usan los de Catalogos). */
    val opciones: FiltrosCatalogo? = null,
    val ejercicios: List<Ejercicio> = emptyList(),
    val total: Int = 0,
    /** Cargando la primera página (con estos filtros). */
    val cargando: Boolean = true,
    /** Cargando la página siguiente al llegar al final de la lista. */
    val cargandoMas: Boolean = false,
    /** Error de la primera página: se muestra EstadoError con Reintentar. */
    val error: String? = null,
    /** Error al pedir más: la lista se conserva y se ofrece Reintentar al final. */
    val errorMas: String? = null
) {
    val hayMas: Boolean get() = ejercicios.size < total
    val vacio: Boolean get() = !cargando && error == null && ejercicios.isEmpty()
    val conFiltros: Boolean get() = filtro != FiltroCatalogo()
}

class EjerciciosViewModel(
    private val repositorio: EjerciciosRepository,
    private val perfilFisico: PerfilFisicoRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(CatalogoUiState())
    val estado: StateFlow<CatalogoUiState> = _estado.asStateFlow()

    /** Única petición del listado en curso: cambiar un filtro cancela la anterior (o la paginación). */
    private var trabajo: Job? = null

    init {
        cargarOpciones()
        viewModelScope.launch {
            // Nivel por defecto: el del perfil físico, si lo tiene. Si falla, se busca sin nivel.
            val nivel = (perfilFisico.obtener() as? Resultado.Exito)?.datos?.nivel
            if (nivel in Catalogos.NIVELES) _estado.update { it.copy(filtro = it.filtro.copy(nivel = nivel)) }
            buscar(conEspera = false)
        }
    }

    fun cambiarTexto(texto: String) = cambiarFiltro(conEspera = true) { it.copy(texto = texto) }
    fun elegirAmbiente(ambiente: String?) = cambiarFiltro { it.copy(ambiente = ambiente) }
    fun elegirMusculo(musculo: String?) = cambiarFiltro { it.copy(musculo = musculo) }
    fun elegirEquipo(equipo: String?) = cambiarFiltro { it.copy(equipo = equipo) }
    fun elegirNivel(nivel: String?) = cambiarFiltro { it.copy(nivel = nivel) }
    fun quitarFiltros() = cambiarFiltro { FiltroCatalogo() }

    fun reintentar() {
        if (_estado.value.opciones == null) cargarOpciones()
        buscar(conEspera = false)
    }

    /** La pantalla la llama al acercarse al final de la lista. */
    fun cargarMas() {
        val s = _estado.value
        if (s.cargando || s.cargandoMas || !s.hayMas || s.errorMas != null) return
        pedirSiguientePagina()
    }

    fun reintentarMas() {
        val s = _estado.value
        if (s.cargando || s.cargandoMas) return
        pedirSiguientePagina()
    }

    private fun cambiarFiltro(conEspera: Boolean = false, cambio: (FiltroCatalogo) -> FiltroCatalogo) {
        _estado.update { it.copy(filtro = cambio(it.filtro)) }
        buscar(conEspera)
    }

    private fun cargarOpciones() {
        viewModelScope.launch {
            val r = repositorio.filtros()
            if (r is Resultado.Exito) _estado.update { it.copy(opciones = r.datos) }
        }
    }

    private fun buscar(conEspera: Boolean) {
        trabajo?.cancel()
        trabajo = viewModelScope.launch {
            _estado.update {
                it.copy(cargando = true, cargandoMas = false, error = null, errorMas = null, ejercicios = emptyList(), total = 0)
            }
            if (conEspera) delay(ESPERA_BUSQUEDA_MS)
            val filtro = _estado.value.filtro
            when (val r = repositorio.buscar(filtro, TAMANO_PAGINA, offset = 0)) {
                is Resultado.Exito -> _estado.update {
                    it.copy(cargando = false, ejercicios = r.datos.ejercicios, total = r.datos.total)
                }
                is Resultado.Fallo -> _estado.update { it.copy(cargando = false, error = r.mensaje) }
            }
        }
    }

    private fun pedirSiguientePagina() {
        trabajo?.cancel()
        trabajo = viewModelScope.launch {
            _estado.update { it.copy(cargandoMas = true, errorMas = null) }
            val actual = _estado.value
            when (val r = repositorio.buscar(actual.filtro, TAMANO_PAGINA, offset = actual.ejercicios.size)) {
                is Resultado.Exito -> _estado.update {
                    val nuevos = r.datos.ejercicios
                    val lista = (it.ejercicios + nuevos).distinctBy { e -> e.id }
                    // Una página vacía cierra la lista aunque el total diga otra cosa (evita pedir sin fin)
                    val total = if (nuevos.isEmpty()) lista.size else r.datos.total
                    it.copy(cargandoMas = false, ejercicios = lista, total = total)
                }
                is Resultado.Fallo -> _estado.update { it.copy(cargandoMas = false, errorMas = r.mensaje) }
            }
        }
    }
}
