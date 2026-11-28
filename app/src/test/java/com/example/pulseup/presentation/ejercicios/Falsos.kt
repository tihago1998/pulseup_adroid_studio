package com.example.pulseup.presentation.ejercicios

import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.CreditosCatalogo
import com.example.pulseup.domain.model.Ejercicio
import com.example.pulseup.domain.model.EjercicioDetalle
import com.example.pulseup.domain.model.FiltroCatalogo
import com.example.pulseup.domain.model.FiltrosCatalogo
import com.example.pulseup.domain.model.NuevoPerfilFisico
import com.example.pulseup.domain.model.PaginaEjercicios
import com.example.pulseup.domain.model.PerfilFisico
import com.example.pulseup.domain.model.RecomendacionEjercicio
import com.example.pulseup.domain.repository.EjerciciosRepository
import com.example.pulseup.domain.repository.PerfilFisicoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** Reemplaza Dispatchers.Main (el de viewModelScope) por un dispatcher de pruebas con tiempo virtual. */
@OptIn(ExperimentalCoroutinesApi::class)
class ReglaMainDispatcher(val dispatcher: TestDispatcher = StandardTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

fun ejercicio(id: Int) = Ejercicio(id, "Ejercicio $id", "pecho", emptyList(), "barra", "intermedio", emptyList())

/** Catálogo falso de [total] ejercicios; registra cada búsqueda y puede fallar a pedido. */
class EjerciciosFalso(private val total: Int = 45) : EjerciciosRepository {
    val busquedas = mutableListOf<Triple<FiltroCatalogo, Int, Int>>()
    var fallarBusqueda: String? = null
    var fallarDetalle: String? = null
    var detalleRespuesta: EjercicioDetalle = detalle()

    override suspend fun listar(musculo: String?, equipo: String?, nivel: String?, texto: String?) =
        Resultado.Exito((1..total).map { ejercicio(it) })

    override suspend fun buscar(filtro: FiltroCatalogo, limit: Int, offset: Int): Resultado<PaginaEjercicios> {
        busquedas += Triple(filtro, limit, offset)
        fallarBusqueda?.let { return Resultado.Fallo(it) }
        val pagina = (offset + 1..minOf(offset + limit, total)).map { ejercicio(it) }
        return Resultado.Exito(PaginaEjercicios(pagina, total))
    }

    override suspend fun filtros() = Resultado.Exito(
        FiltrosCatalogo(listOf("gimnasio", "casa"), listOf("pecho"), listOf("barra"), listOf("principiante", "intermedio", "avanzado"))
    )

    /** Ids pedidos a detalle(), en orden: sirve para contar llamadas. */
    val detallesPedidos = mutableListOf<Int>()

    override suspend fun detalle(id: Int): Resultado<EjercicioDetalle> {
        detallesPedidos += id
        return fallarDetalle?.let { Resultado.Fallo(it) } ?: Resultado.Exito(detalleRespuesta)
    }

    override suspend fun recomendacion(id: Int): Resultado<RecomendacionEjercicio?> =
        Resultado.Exito(detalleRespuesta.recomendacion)

    override suspend fun creditos() = Resultado.Exito(CreditosCatalogo(emptyList(), null))
}

fun detalle(recomendacion: RecomendacionEjercicio? = null, motivo: String? = "Completa tu perfil físico para ver la recomendación") =
    EjercicioDetalle(
        id = 1, nombre = "Press de banca", musculoPrincipal = "pecho", musculosSecundarios = listOf("triceps"),
        equipo = "barra", nivel = "intermedio", ambientes = listOf("gimnasio"), imagenes = emptyList(),
        pasos = listOf("Acuéstate en el banco."), respiracion = null, erroresComunes = emptyList(),
        varianteFacil = null, varianteDificil = null, descansoSeg = 120,
        recomendacion = recomendacion, recomendacionMotivo = if (recomendacion == null) motivo else null
    )

/** Perfil físico falso: con [nivel] null responde que el usuario aún no tiene perfil. Se puede cambiar en la prueba. */
class PerfilFisicoFalso(var nivel: String?, var falla: Boolean = false) : PerfilFisicoRepository {
    override suspend fun obtener(): Resultado<PerfilFisico?> {
        val actual = nivel
        return when {
            falla -> Resultado.Fallo("Sin conexión")
            actual == null -> Resultado.Exito(null)
            else -> Resultado.Exito(PerfilFisico("1998-03-10", 28, "hombre", 72.5, 175.0, actual, ""))
        }
    }

    override suspend fun guardar(nuevo: NuevoPerfilFisico) = Resultado.Fallo("no se usa")
    override suspend fun eliminar() = Resultado.Fallo("no se usa")
}
