package com.example.pulseup.presentation.ejercicios

import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.DiaPrevisualizado
import com.example.pulseup.domain.model.RecomendacionEjercicio
import com.example.pulseup.presentation.plan.PlanFalso
import com.example.pulseup.presentation.plan.aviso
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
class EjercicioDetalleViewModelTest {

    @get:Rule
    val main = ReglaMainDispatcher()

    private val recomendacion = RecomendacionEjercicio(
        "intermedio", "repeticiones", 6, 12, 3, 4, 150, "2-3", null, null, 72.5, null,
        "Es una guía general y no reemplaza a un entrenador ni a un médico."
    )

    private fun vm(
        repo: EjerciciosFalso,
        perfil: PerfilFisicoFalso = PerfilFisicoFalso(nivel = "intermedio"),
        plan: PlanFalso = PlanFalso()
    ) = EjercicioDetalleViewModel(repo, perfil, plan, 1)

    @Test
    fun `carga el detalle`() = runTest(main.dispatcher) {
        val vm = vm(EjerciciosFalso().apply { detalleRespuesta = detalle(recomendacion) })
        advanceUntilIdle()

        val s = vm.estado.value
        assertFalse(s.cargando)
        assertNull(s.error)
        assertEquals("Press de banca", s.ejercicio?.nombre)
        assertEquals(recomendacion, s.ejercicio?.recomendacion)
    }

    @Test
    fun `error y reintentar`() = runTest(main.dispatcher) {
        val repo = EjerciciosFalso().apply { fallarDetalle = "Ejercicio no encontrado" }
        val vm = vm(repo)
        advanceUntilIdle()
        assertEquals("Ejercicio no encontrado", vm.estado.value.error)
        assertNull(vm.estado.value.ejercicio)

        repo.fallarDetalle = null
        vm.cargar()
        advanceUntilIdle()
        assertNull(vm.estado.value.error)
        assertNotNull(vm.estado.value.ejercicio)
    }

    // ---------- Botón "Ir a mi perfil físico" ----------

    @Test
    fun `sin perfil fisico se ofrece ir a llenarlo`() = runTest(main.dispatcher) {
        val repo = EjerciciosFalso().apply { detalleRespuesta = detalle(null, "Completa tu perfil físico para ver la recomendación") }
        val vm = vm(repo, PerfilFisicoFalso(nivel = null))
        advanceUntilIdle()

        assertEquals(true, vm.estado.value.sinPerfilFisico)
        assertTrue(vm.estado.value.mostrarIrAPerfil)
    }

    @Test
    fun `con perfil pero ejercicio sin recomendacion se muestra solo el motivo`() = runTest(main.dispatcher) {
        // El motivo da igual: la decisión sale de que el perfil existe
        val repo = EjerciciosFalso().apply { detalleRespuesta = detalle(null, "Este ejercicio todavía no tiene recomendación") }
        val vm = vm(repo, PerfilFisicoFalso(nivel = "intermedio"))
        advanceUntilIdle()

        assertNull(vm.estado.value.ejercicio?.recomendacion)
        assertEquals("Este ejercicio todavía no tiene recomendación", vm.estado.value.ejercicio?.recomendacionMotivo)
        assertEquals(false, vm.estado.value.sinPerfilFisico)
        assertFalse(vm.estado.value.mostrarIrAPerfil)
    }

    @Test
    fun `con recomendacion no hay boton`() = runTest(main.dispatcher) {
        val vm = vm(EjerciciosFalso().apply { detalleRespuesta = detalle(recomendacion) }, PerfilFisicoFalso(nivel = null))
        advanceUntilIdle()

        assertFalse(vm.estado.value.mostrarIrAPerfil)
    }

    @Test
    fun `si no se puede consultar el perfil no se ofrece el boton`() = runTest(main.dispatcher) {
        val repo = EjerciciosFalso().apply { detalleRespuesta = detalle(null) }
        val vm = vm(repo, PerfilFisicoFalso(nivel = null, falla = true))
        advanceUntilIdle()

        assertNull(vm.estado.value.sinPerfilFisico)
        assertFalse(vm.estado.value.mostrarIrAPerfil)
        assertNotNull(vm.estado.value.ejercicio) // el detalle se muestra igual
    }

    // ---------- Al volver del perfil físico ----------

    @Test
    fun `al volver del perfil fisico se actualiza la recomendacion y se quita el boton`() = runTest(main.dispatcher) {
        val repo = EjerciciosFalso().apply { detalleRespuesta = detalle(null) }
        val perfil = PerfilFisicoFalso(nivel = null)
        val vm = vm(repo, perfil)
        advanceUntilIdle()
        assertTrue(vm.estado.value.mostrarIrAPerfil)

        // El usuario llenó su perfil
        perfil.nivel = "intermedio"
        repo.detalleRespuesta = detalle(recomendacion)
        vm.refrescarRecomendacion()
        advanceUntilIdle()

        assertEquals(recomendacion, vm.estado.value.ejercicio?.recomendacion)
        assertEquals(false, vm.estado.value.sinPerfilFisico)
        assertFalse(vm.estado.value.mostrarIrAPerfil)
    }

    @Test
    fun `refrescar no borra lo que se ve si falla`() = runTest(main.dispatcher) {
        val repo = EjerciciosFalso().apply { detalleRespuesta = detalle(null) }
        val vm = vm(repo, PerfilFisicoFalso(nivel = null))
        advanceUntilIdle()

        repo.fallarDetalle = "Sin conexión"
        vm.refrescarRecomendacion()
        advanceUntilIdle()

        assertNull(vm.estado.value.error)
        assertEquals("Press de banca", vm.estado.value.ejercicio?.nombre)
        assertTrue(vm.estado.value.mostrarIrAPerfil)
    }

    // ---------- Agregar a mi plan ----------

    private fun planConAvisoElMartes() = PlanFalso().apply {
        previsualizacion = previsualizacion.map {
            if (it.dia == 2) DiaPrevisualizado(2, "martes", true, listOf(aviso(diaConflicto = 1))) else it
        }
    }

    @Test
    fun `abrir la hoja pide los dias con previsualizar del ejercicio`() = runTest(main.dispatcher) {
        val plan = PlanFalso()
        val vm = vm(EjerciciosFalso(), plan = plan)
        advanceUntilIdle()

        vm.abrirHojaPlan()
        advanceUntilIdle()

        val hoja = vm.estado.value.hojaPlan!!
        assertFalse(hoja.cargando)
        assertEquals(7, hoja.dias.size)
        assertEquals(listOf("ejercicio 1"), plan.previsualizadas)
    }

    @Test
    fun `dia sin avisos se agrega directo y muestra la confirmacion`() = runTest(main.dispatcher) {
        val plan = PlanFalso()
        val vm = vm(EjerciciosFalso(), plan = plan)
        advanceUntilIdle()
        vm.abrirHojaPlan()
        advanceUntilIdle()

        vm.elegirDia(vm.estado.value.hojaPlan!!.dias[3]) // jueves
        advanceUntilIdle()

        assertEquals(listOf(4 to 1), plan.agregados)
        assertNull(vm.estado.value.hojaPlan)
        val agregado = vm.estado.value.agregado!!
        assertEquals(4, agregado.dia)
        assertEquals("jueves", agregado.nombreDia)
        assertFalse(agregado.rutinaCopiada)
    }

    @Test
    fun `dia con aviso pide confirmar y Agregar igual lo agrega`() = runTest(main.dispatcher) {
        val plan = planConAvisoElMartes().apply { avisosAlAgregar = listOf(aviso(diaConflicto = 1)) }
        val vm = vm(EjerciciosFalso(), plan = plan)
        advanceUntilIdle()
        vm.abrirHojaPlan()
        advanceUntilIdle()

        vm.elegirDia(vm.estado.value.hojaPlan!!.dias[1]) // martes, con aviso
        advanceUntilIdle()
        assertTrue(plan.agregados.isEmpty()) // todavía no: espera la confirmación
        assertEquals(2, vm.estado.value.hojaPlan?.porConfirmar?.dia)

        vm.confirmarDia()
        advanceUntilIdle()
        assertEquals(listOf(2 to 1), plan.agregados)
        assertEquals(1, vm.estado.value.agregado?.avisos?.size)
    }

    @Test
    fun `cancelar la confirmacion no agrega y deja la hoja abierta`() = runTest(main.dispatcher) {
        val plan = planConAvisoElMartes()
        val vm = vm(EjerciciosFalso(), plan = plan)
        advanceUntilIdle()
        vm.abrirHojaPlan()
        advanceUntilIdle()

        vm.elegirDia(vm.estado.value.hojaPlan!!.dias[1])
        vm.cancelarConfirmacion()
        advanceUntilIdle()

        assertTrue(plan.agregados.isEmpty())
        assertNull(vm.estado.value.hojaPlan?.porConfirmar)
        assertNotNull(vm.estado.value.hojaPlan)
    }

    @Test
    fun `el 409 muestra un mensaje claro y deja elegir otro dia`() = runTest(main.dispatcher) {
        val plan = PlanFalso().apply { fallarAgregar = Resultado.Fallo("Ese ejercicio ya está en este día", 409) }
        val vm = vm(EjerciciosFalso(), plan = plan)
        advanceUntilIdle()
        vm.abrirHojaPlan()
        advanceUntilIdle()

        vm.elegirDia(vm.estado.value.hojaPlan!!.dias[0])
        advanceUntilIdle()

        val hoja = vm.estado.value.hojaPlan!!
        assertEquals("Este ejercicio ya está en tu plan del lunes. Elige otro día.", hoja.errorAgregar)
        assertFalse(hoja.guardando)
        assertNull(vm.estado.value.agregado)
    }

    @Test
    fun `otro error muestra el mensaje del backend`() = runTest(main.dispatcher) {
        val plan = PlanFalso().apply { fallarAgregar = Resultado.Fallo("Máximo 20 ejercicios por día", 400) }
        val vm = vm(EjerciciosFalso(), plan = plan)
        advanceUntilIdle()
        vm.abrirHojaPlan()
        advanceUntilIdle()

        vm.elegirDia(vm.estado.value.hojaPlan!!.dias[0])
        advanceUntilIdle()

        assertEquals("Máximo 20 ejercicios por día", vm.estado.value.hojaPlan?.errorAgregar)
    }

    @Test
    fun `rutina copiada se informa en la confirmacion`() = runTest(main.dispatcher) {
        val plan = PlanFalso().apply { copiarAlAgregar = true }
        val vm = vm(EjerciciosFalso(), plan = plan)
        advanceUntilIdle()
        vm.abrirHojaPlan()
        advanceUntilIdle()

        vm.elegirDia(vm.estado.value.hojaPlan!!.dias[0])
        advanceUntilIdle()

        assertTrue(vm.estado.value.agregado!!.rutinaCopiada)
    }

    @Test
    fun `si previsualizar falla se puede reintentar`() = runTest(main.dispatcher) {
        val plan = PlanFalso().apply { fallarPrevisualizar = "No se pudo conectar con el servidor. Revisa tu conexión." }
        val vm = vm(EjerciciosFalso(), plan = plan)
        advanceUntilIdle()
        vm.abrirHojaPlan()
        advanceUntilIdle()
        assertEquals("No se pudo conectar con el servidor. Revisa tu conexión.", vm.estado.value.hojaPlan?.error)

        plan.fallarPrevisualizar = null
        vm.reintentarHojaPlan()
        advanceUntilIdle()
        assertNull(vm.estado.value.hojaPlan?.error)
        assertEquals(7, vm.estado.value.hojaPlan?.dias?.size)
    }
}
