package com.example.pulseup.presentation.perfil

import com.example.pulseup.domain.model.NuevoPerfilFisico
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ValidacionPerfilFisicoTest {

    // "Hoy" fijo para que los tests no dependan del día en que se ejecutan
    private val hoy = LocalDate.of(2026, 11, 22)

    private fun validar(
        fecha: LocalDate? = LocalDate.of(1998, 3, 10),
        sexo: String = "hombre",
        peso: String = "72,5",
        estatura: String = "175",
        nivel: String? = "intermedio"
    ) = validarPerfilFisico(fecha, sexo, peso, estatura, nivel, hoy)

    private fun errores(v: ValidacionPerfilFisico): ErroresPerfilFisico {
        assertTrue("Se esperaba Invalido y llegó $v", v is ValidacionPerfilFisico.Invalido)
        return (v as ValidacionPerfilFisico.Invalido).errores
    }

    // ---------- Edad y fecha ----------

    @Test
    fun `13 anos se rechaza`() {
        // Cumple 14 mañana
        val e = errores(validar(fecha = LocalDate.of(2012, 11, 23)))
        assertEquals("Debes tener al menos 14 años para usar PulseUp", e.fecha)
    }

    @Test
    fun `14 anos exactos se acepta`() {
        val v = validar(fecha = LocalDate.of(2012, 11, 22))
        assertTrue(v is ValidacionPerfilFisico.Valido)
    }

    @Test
    fun `100 anos se acepta`() {
        val v = validar(fecha = LocalDate.of(1926, 11, 22))
        assertTrue(v is ValidacionPerfilFisico.Valido)
    }

    @Test
    fun `101 anos se rechaza`() {
        val e = errores(validar(fecha = LocalDate.of(1925, 11, 22)))
        assertEquals("Revisa la fecha de nacimiento: la edad máxima es 100 años", e.fecha)
    }

    @Test
    fun `fecha futura se rechaza`() {
        val e = errores(validar(fecha = LocalDate.of(2026, 11, 23)))
        assertEquals("La fecha de nacimiento no puede ser futura", e.fecha)
    }

    @Test
    fun `sin fecha se rechaza`() {
        assertEquals("Elige tu fecha de nacimiento", errores(validar(fecha = null)).fecha)
    }

    // ---------- Peso ----------

    @Test
    fun `peso 29 se rechaza`() {
        assertEquals("El peso debe estar entre 30 y 300 kg", errores(validar(peso = "29")).peso)
    }

    @Test
    fun `peso 301 se rechaza`() {
        assertEquals("El peso debe estar entre 30 y 300 kg", errores(validar(peso = "301")).peso)
    }

    @Test
    fun `peso vacio se rechaza`() {
        assertEquals("Escribe tu peso en kg", errores(validar(peso = "")).peso)
    }

    // ---------- Estatura ----------

    @Test
    fun `estatura 99 se rechaza`() {
        assertEquals("La estatura debe estar entre 100 y 250 cm", errores(validar(estatura = "99")).estatura)
    }

    @Test
    fun `estatura 251 se rechaza`() {
        assertEquals("La estatura debe estar entre 100 y 250 cm", errores(validar(estatura = "251")).estatura)
    }

    // ---------- Nivel ----------

    @Test
    fun `nivel vacio se rechaza`() {
        assertEquals("Elige tu nivel", errores(validar(nivel = "")).nivel)
        assertEquals("Elige tu nivel", errores(validar(nivel = null)).nivel)
    }

    // ---------- Caso válido ----------

    @Test
    fun `caso valido arma lo que se envia al backend`() {
        val v = validar()
        assertTrue(v is ValidacionPerfilFisico.Valido)
        assertEquals(
            NuevoPerfilFisico("1998-03-10", "hombre", 72.5, 175.0, "intermedio"),
            (v as ValidacionPerfilFisico.Valido).nuevo
        )
    }

    @Test
    fun `solo se marca el campo con error`() {
        val e = errores(validar(peso = "29"))
        assertNull(e.fecha)
        assertNull(e.estatura)
        assertNull(e.nivel)
    }

    // ---------- Ayudas de texto ----------

    @Test
    fun `acepta coma o punto decimal`() {
        assertEquals(72.5, leerDecimal("72,5")!!, 0.0)
        assertEquals(72.5, leerDecimal("72.5")!!, 0.0)
    }

    @Test
    fun `la entrada permite como mucho un decimal`() {
        assertTrue(esEntradaDecimalValida("72,5"))
        assertTrue(esEntradaDecimalValida("175."))
        assertTrue(!esEntradaDecimalValida("72,55"))
        assertTrue(!esEntradaDecimalValida("7a"))
    }

    @Test
    fun `formatea lo que devuelve el backend`() {
        assertEquals("175", formatearDecimal(175.0))
        assertEquals("72,5", formatearDecimal(72.5))
    }
}
