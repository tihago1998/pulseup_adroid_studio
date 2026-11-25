package com.example.pulseup.data.mapper

import com.example.pulseup.data.remote.dto.EjercicioDeRutinaDto
import com.example.pulseup.data.remote.dto.EjercicioDto
import com.example.pulseup.data.remote.dto.RutinaDto
import com.example.pulseup.domain.model.ItemNuevaRutina
import com.example.pulseup.domain.model.NuevaRutina
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EntrenamientoMapperTest {

    @Test
    fun ejercicioConCamposNulosUsaValoresPorDefecto() {
        val ejercicio = EjercicioDto(1, null, null, null, null, null, null).aDominio()
        assertEquals("", ejercicio.nombre)
        assertTrue(ejercicio.instrucciones.isEmpty())
    }

    @Test
    fun rutinaTraeSusEjerciciosYSaltaLosIncompletos() {
        val dto = RutinaDto(
            id = 7, nombre = "Pierna", descripcion = null, objetivo = "fuerza", nivel = "intermedio",
            duracionMin = 60, totalEjercicios = null, esPlantilla = true,
            ejercicios = listOf(
                EjercicioDeRutinaDto(1, 4, "6-8", 120, EjercicioDto(3, "Sentadilla", "cuadriceps", listOf("gluteos"), "barra", "avanzado", listOf("Baja"))),
                EjercicioDeRutinaDto(2, 3, "10", 60, null)
            )
        )
        val rutina = dto.aDominio()
        assertEquals(1, rutina.ejercicios.size)
        assertEquals("Sentadilla", rutina.ejercicios[0].ejercicio.nombre)
        assertTrue(rutina.esPlantilla)
        assertEquals("", rutina.descripcion)
    }

    @Test
    fun nuevaRutinaSeConvierteAlFormatoDelBackend() {
        val request = NuevaRutina("Mi rutina", "", "fuerza", "intermedio", 50, listOf(ItemNuevaRutina(5, 4, "8-10", 90))).aRequest()
        assertEquals(50, request.duracionMin)
        assertEquals(5, request.ejercicios[0].idEjercicio)
        assertFalse(request.ejercicios.isEmpty())
    }
}
