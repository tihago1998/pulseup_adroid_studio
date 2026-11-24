package com.example.pulseup.presentation.plan

import com.example.pulseup.domain.model.nombreDiaSemana
import org.junit.Assert.assertEquals
import org.junit.Test

class AvisosPlanTest {

    @Test
    fun `avisos como advertencias sin repetir`() {
        val notas = notasDeAvisos(listOf(aviso(1), aviso(1), aviso(3, "espalda")))
        assertEquals(2, notas.size)
        assertEquals(TipoNota.ADVERTENCIA, notas[0].tipo)
        assertEquals("El miércoles ya trabajas espalda: tu espalda necesita 48 horas de descanso", notas[1].texto)
    }

    @Test
    fun `unir dias`() {
        assertEquals("", unirDias(emptyList()))
        assertEquals("lunes", unirDias(listOf("lunes")))
        assertEquals("lunes y jueves", unirDias(listOf("lunes", "jueves")))
        assertEquals("lunes, martes y jueves", unirDias(listOf("lunes", "martes", "jueves")))
    }

    @Test
    fun `nombre de cada dia como el backend`() {
        assertEquals("lunes", nombreDiaSemana(1))
        assertEquals("miércoles", nombreDiaSemana(3))
        assertEquals("domingo", nombreDiaSemana(7))
        assertEquals("día 9", nombreDiaSemana(9))
    }

    @Test
    fun `textos del 409 y de la copia`() {
        assertEquals("Este ejercicio ya está en tu plan del martes. Elige otro día.", textoYaEstaEnElDia("martes"))
        assertEquals(
            "La rutina del lunes era compartida: se creó una copia solo para ese día y la original no cambió.",
            textoRutinaCopiada("lunes")
        )
    }
}
