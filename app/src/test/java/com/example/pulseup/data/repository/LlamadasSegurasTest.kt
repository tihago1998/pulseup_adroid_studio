package com.example.pulseup.data.repository

import com.example.pulseup.domain.Resultado
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class LlamadasSegurasTest {

    private fun error(codigo: Int, json: String): Response<String> =
        Response.error(codigo, json.toResponseBody("application/json".toMediaType()))

    @Test
    fun `el 409 trae el mensaje del backend y el codigo`() = runTest {
        val r = ejecutar({ error(409, """{"error": "Ese ejercicio ya está en este día"}""") }, { it })
        assertEquals(Resultado.Fallo("Ese ejercicio ya está en este día", 409), r)
    }

    @Test
    fun `sin conexion no hay codigo`() = runTest {
        val r = ejecutar<String, String>({ throw IOException() }, { it })
        assertNull((r as Resultado.Fallo).codigo)
    }
}
