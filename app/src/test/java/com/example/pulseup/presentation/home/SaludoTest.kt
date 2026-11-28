package com.example.pulseup.presentation.home

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

/** Saludo e icono del Home según la hora del teléfono, en los límites de cada franja. */
class SaludoTest {

    private fun saludo(h: Int, m: Int) = saludoPara(LocalTime.of(h, m))

    @Test
    fun `04 59 es de noche con luna`() =
        assertEquals(Saludo("Buenas noches", IconoSaludo.LUNA), saludo(4, 59))

    @Test
    fun `05 00 empieza la mañana con sol`() =
        assertEquals(Saludo("Buenos días", IconoSaludo.SOL), saludo(5, 0))

    @Test
    fun `11 59 sigue la mañana`() =
        assertEquals(Saludo("Buenos días", IconoSaludo.SOL), saludo(11, 59))

    @Test
    fun `12 00 empieza la tarde con sol`() =
        assertEquals(Saludo("Buenas tardes", IconoSaludo.SOL), saludo(12, 0))

    @Test
    fun `18 59 sigue la tarde`() =
        assertEquals(Saludo("Buenas tardes", IconoSaludo.SOL), saludo(18, 59))

    @Test
    fun `19 00 empieza la noche con luna`() =
        assertEquals(Saludo("Buenas noches", IconoSaludo.LUNA), saludo(19, 0))

    @Test
    fun `00 00 es de noche`() =
        assertEquals(Saludo("Buenas noches", IconoSaludo.LUNA), saludo(0, 0))
}
