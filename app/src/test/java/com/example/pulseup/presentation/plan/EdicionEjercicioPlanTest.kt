package com.example.pulseup.presentation.plan

import com.example.pulseup.domain.model.CambiosEjercicioPlan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EdicionEjercicioPlanTest {

    private fun edicion(
        unidad: String = "repeticiones",
        series: String = "3",
        repeticiones: String = "10",
        peso: String = "40",
        descanso: String = "60"
    ) = EdicionEjercicio.desde(ejercicioPlan(1, unidad = unidad))
        .copy(series = series, repeticiones = repeticiones, peso = peso, descanso = descanso)

    private fun invalida(e: EdicionEjercicio) = (validarEdicion(e) as ValidacionEdicion.Invalida).mensaje

    @Test
    fun `desde el item muestra el peso con coma y vacio si no tiene`() {
        assertEquals("72,5", EdicionEjercicio.desde(ejercicioPlan(1, peso = 72.5)).peso)
        assertEquals("", EdicionEjercicio.desde(ejercicioPlan(1, peso = null)).peso)
    }

    @Test
    fun `valores validos`() {
        assertEquals(
            ValidacionEdicion.Valida(CambiosEjercicioPlan(4, "8-10", 72.5, 90)),
            validarEdicion(edicion(series = "4", repeticiones = " 8-10 ", peso = "72,5", descanso = "90"))
        )
    }

    @Test
    fun `peso vacio es sin peso`() {
        val v = validarEdicion(edicion(peso = " ")) as ValidacionEdicion.Valida
        assertEquals(null, v.cambios.pesoKg)
    }

    @Test
    fun `limites de series`() {
        assertEquals("Las series deben estar entre 1 y 10", invalida(edicion(series = "0")))
        assertEquals("Las series deben estar entre 1 y 10", invalida(edicion(series = "11")))
        assertEquals("Las series deben estar entre 1 y 10", invalida(edicion(series = "")))
    }

    @Test
    fun `repeticiones o duracion vacias`() {
        assertEquals("Escribe las repeticiones", invalida(edicion(repeticiones = "")))
        assertEquals("Escribe la duración", invalida(edicion(unidad = "minutos", repeticiones = "")))
    }

    @Test
    fun `repeticiones muy largas`() {
        assertEquals("Las repeticiones admiten máximo 20 caracteres", invalida(edicion(repeticiones = "1".repeat(21))))
    }

    @Test
    fun `limites de peso`() {
        assertEquals("El peso debe estar entre 0 y 500 kg, o déjalo vacío", invalida(edicion(peso = "600")))
        assertEquals("El peso debe estar entre 0 y 500 kg, o déjalo vacío", invalida(edicion(peso = "abc")))
        assertTrue(validarEdicion(edicion(peso = "0")) is ValidacionEdicion.Valida)
        assertTrue(validarEdicion(edicion(peso = "500")) is ValidacionEdicion.Valida)
    }

    @Test
    fun `limites de descanso`() {
        assertEquals("El descanso debe estar entre 0 y 600 segundos", invalida(edicion(descanso = "601")))
        assertTrue(validarEdicion(edicion(descanso = "0")) is ValidacionEdicion.Valida)
    }

    @Test
    fun `cardio en minutos conserva series y descanso ocultos`() {
        val e = EdicionEjercicio.desde(ejercicioPlan(1, unidad = "minutos", peso = null)).copy(repeticiones = "40")
        assertTrue(e.esCardio)
        val v = validarEdicion(e) as ValidacionEdicion.Valida
        assertEquals("40", v.cambios.repeticiones)
        assertEquals(3, v.cambios.series)
        assertFalse(edicion().esCardio)
    }

    @Test
    fun `etiquetas segun la unidad`() {
        assertEquals("Repeticiones", etiquetaRepeticiones("repeticiones"))
        assertEquals("Duración (segundos)", etiquetaRepeticiones("segundos"))
        assertEquals("Duración (minutos)", etiquetaRepeticiones("minutos"))
    }
}
