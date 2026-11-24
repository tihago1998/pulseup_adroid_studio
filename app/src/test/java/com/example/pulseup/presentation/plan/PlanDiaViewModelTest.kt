package com.example.pulseup.presentation.plan

import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.CambiosEjercicioPlan
import com.example.pulseup.domain.model.RecomendacionEjercicio
import com.example.pulseup.presentation.ejercicios.EjerciciosFalso
import com.example.pulseup.presentation.ejercicios.ReglaMainDispatcher
import com.example.pulseup.presentation.ejercicios.detalle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlanDiaViewModelTest {

    @get:Rule
    val main = ReglaMainDispatcher()

    private val recomendacion = RecomendacionEjercicio(
        "intermedio", "repeticiones", 6, 12, 3, 4, 150, "2-3", null, null, 72.5, null,
        "Es una guía general y no reemplaza a un entrenador ni a un médico."
    )

    private fun planConLunes() = PlanFalso().apply {
        dias = dias.map { if (it.diaSemana == 1) diaConRutina(1, ejercicioPlan(36, 3), ejercicioPlan(37, 41)) else it }
    }

    private fun vm(plan: PlanFalso = planConLunes(), ejercicios: EjerciciosFalso = EjerciciosFalso(), dia: Int = 1) =
        PlanDiaViewModel(plan, ejercicios, dia)

    @Test
    fun `carga solo el dia pedido`() = runTest(main.dispatcher) {
        val vm = vm()
        advanceUntilIdle()

        val s = vm.estado.value
        assertFalse(s.cargando)
        assertEquals("lunes", s.nombreDia)
        assertEquals(listOf(36, 37), s.ejercicios.map { it.itemId })
        assertFalse(s.lleno)
    }

    @Test
    fun `dia de descanso sin ejercicios`() = runTest(main.dispatcher) {
        val vm = vm(dia = 4)
        advanceUntilIdle()
        assertNull(vm.estado.value.dia?.rutina)
        assertTrue(vm.estado.value.ejercicios.isEmpty())
        assertEquals("jueves", vm.estado.value.nombreDia)
    }

    @Test
    fun `error al cargar`() = runTest(main.dispatcher) {
        val vm = vm(PlanFalso().apply { fallarPlan = "Tu sesión expiró. Inicia sesión de nuevo." })
        advanceUntilIdle()
        assertEquals("Tu sesión expiró. Inicia sesión de nuevo.", vm.estado.value.error)
    }

    @Test
    fun `con 20 ejercicios el dia esta lleno`() = runTest(main.dispatcher) {
        val plan = PlanFalso().apply {
            dias = dias.map { if (it.diaSemana == 1) diaConRutina(1, *(1..20).map { n -> ejercicioPlan(n) }.toTypedArray()) else it }
        }
        val vm = vm(plan)
        advanceUntilIdle()
        assertTrue(vm.estado.value.lleno)
    }

    // ---------- Editar ----------

    @Test
    fun `abrir la edicion trae los valores y la recomendacion como guia`() = runTest(main.dispatcher) {
        val ejercicios = EjerciciosFalso().apply { detalleRespuesta = detalle(recomendacion) }
        val vm = vm(ejercicios = ejercicios)
        advanceUntilIdle()

        vm.abrirEdicion(vm.estado.value.ejercicios[0])
        advanceUntilIdle()

        val e = vm.estado.value.edicion!!
        assertEquals("3", e.series)
        assertEquals("10", e.repeticiones)
        assertEquals("40", e.peso)
        assertEquals("60", e.descanso)
        assertFalse(e.cargandoRecomendacion)
        assertEquals(recomendacion, e.recomendacion)
    }

    @Test
    fun `guardar envia los cambios sin imponer la recomendacion y recarga el plan`() = runTest(main.dispatcher) {
        val plan = planConLunes()
        val vm = vm(plan, EjerciciosFalso().apply { detalleRespuesta = detalle(recomendacion) })
        advanceUntilIdle()
        val cargasAntes = plan.vecesPlan

        vm.abrirEdicion(vm.estado.value.ejercicios[0])
        vm.cambiarSeries("5")
        vm.cambiarRepeticiones("8-10")
        vm.cambiarPeso("62,5")
        vm.cambiarDescanso("90")
        vm.guardarEdicion()
        advanceUntilIdle()

        assertEquals(Triple(1, 36, CambiosEjercicioPlan(5, "8-10", 62.5, 90)), plan.ediciones.single())
        assertNull(vm.estado.value.edicion)
        assertEquals(TipoNota.EXITO, vm.estado.value.notas.single().tipo)
        assertEquals(cargasAntes + 1, plan.vecesPlan)
    }

    @Test
    fun `peso vacio se envia como null`() = runTest(main.dispatcher) {
        val plan = planConLunes()
        val vm = vm(plan)
        advanceUntilIdle()

        vm.abrirEdicion(vm.estado.value.ejercicios[0])
        vm.cambiarPeso("")
        vm.guardarEdicion()
        advanceUntilIdle()

        assertNull(plan.ediciones.single().third.pesoKg)
    }

    @Test
    fun `valor invalido no llama al backend`() = runTest(main.dispatcher) {
        val plan = planConLunes()
        val vm = vm(plan)
        advanceUntilIdle()

        vm.abrirEdicion(vm.estado.value.ejercicios[0])
        vm.cambiarSeries("12")
        vm.guardarEdicion()
        advanceUntilIdle()

        assertTrue(plan.ediciones.isEmpty())
        assertEquals("Las series deben estar entre 1 y 10", vm.estado.value.edicion?.error)
    }

    @Test
    fun `rutina copiada al editar se informa`() = runTest(main.dispatcher) {
        val plan = planConLunes().apply { copiarAlEditar = true }
        val vm = vm(plan)
        advanceUntilIdle()

        vm.abrirEdicion(vm.estado.value.ejercicios[0])
        vm.guardarEdicion()
        advanceUntilIdle()

        val notas = vm.estado.value.notas
        assertEquals(listOf(TipoNota.EXITO, TipoNota.INFO), notas.map { it.tipo })
        assertEquals(textoRutinaCopiada("lunes"), notas[1].texto)
    }

    @Test
    fun `error del backend al editar se queda en el dialogo`() = runTest(main.dispatcher) {
        val plan = planConLunes().apply { fallarEditar = "peso_kg debe ser un número entre 0 y 500, o null" }
        val vm = vm(plan)
        advanceUntilIdle()

        vm.abrirEdicion(vm.estado.value.ejercicios[0])
        vm.guardarEdicion()
        advanceUntilIdle()

        val e = vm.estado.value.edicion
        assertNotNull(e)
        assertFalse(e!!.guardando)
        assertEquals("peso_kg debe ser un número entre 0 y 500, o null", e.error)
    }

    // ---------- Quitar ----------

    @Test
    fun `quitar pide confirmacion`() = runTest(main.dispatcher) {
        val plan = planConLunes()
        val vm = vm(plan)
        advanceUntilIdle()

        vm.pedirQuitar(vm.estado.value.ejercicios[1])
        assertEquals(37, vm.estado.value.porQuitar?.itemId)
        vm.cancelarQuitar()
        advanceUntilIdle()

        assertTrue(plan.quitados.isEmpty())
        assertNull(vm.estado.value.porQuitar)
    }

    @Test
    fun `confirmar quitar llama al backend y recarga`() = runTest(main.dispatcher) {
        val plan = planConLunes()
        val vm = vm(plan)
        advanceUntilIdle()
        val cargasAntes = plan.vecesPlan

        vm.pedirQuitar(vm.estado.value.ejercicios[1])
        vm.confirmarQuitar()
        advanceUntilIdle()

        assertEquals(listOf(1 to 37), plan.quitados)
        assertEquals(cargasAntes + 1, plan.vecesPlan)
        assertFalse(vm.estado.value.ocupado)
    }

    @Test
    fun `quitar el ultimo avisa que el dia queda en descanso`() = runTest(main.dispatcher) {
        val plan = planConLunes().apply { diaEnDescansoAlQuitar = true }
        val vm = vm(plan)
        advanceUntilIdle()

        vm.pedirQuitar(vm.estado.value.ejercicios[0])
        vm.confirmarQuitar()
        advanceUntilIdle()

        assertTrue(vm.estado.value.notas.any { it.texto == "El lunes quedó en descanso." })
    }

    // ---------- Agregar desde el catálogo ----------

    @Test
    fun `agregar con avisos los muestra como advertencia sin bloquear`() = runTest(main.dispatcher) {
        val plan = planConLunes().apply { avisosAlAgregar = listOf(aviso(diaConflicto = 7)) }
        val vm = vm(plan)
        advanceUntilIdle()

        vm.agregar(15)
        advanceUntilIdle()

        assertEquals(listOf(1 to 15), plan.agregados)
        val notas = vm.estado.value.notas
        assertEquals(TipoNota.EXITO, notas[0].tipo)
        assertEquals(TipoNota.ADVERTENCIA, notas[1].tipo)
        assertEquals("El domingo ya trabajas pecho: tu pecho necesita 48 horas de descanso", notas[1].texto)
    }

    @Test
    fun `agregar repetido muestra el 409 claro`() = runTest(main.dispatcher) {
        val plan = planConLunes().apply { fallarAgregar = Resultado.Fallo("Ese ejercicio ya está en este día", 409) }
        val vm = vm(plan)
        advanceUntilIdle()

        vm.agregar(3)
        advanceUntilIdle()

        val nota = vm.estado.value.notas.single()
        assertEquals(TipoNota.ERROR, nota.tipo)
        assertEquals("Este ejercicio ya está en tu plan del lunes. Elige otro día.", nota.texto)
    }

    @Test
    fun `agregar con rutina copiada lo informa`() = runTest(main.dispatcher) {
        val plan = planConLunes().apply { copiarAlAgregar = true }
        val vm = vm(plan)
        advanceUntilIdle()

        vm.agregar(15)
        advanceUntilIdle()

        assertTrue(vm.estado.value.notas.any { it.tipo == TipoNota.INFO && it.texto == textoRutinaCopiada("lunes") })
    }
}
