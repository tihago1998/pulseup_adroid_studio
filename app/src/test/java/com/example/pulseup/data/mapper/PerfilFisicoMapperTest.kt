package com.example.pulseup.data.mapper

import com.example.pulseup.data.remote.dto.PerfilFisicoResponse
import com.example.pulseup.domain.model.NuevoPerfilFisico
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** JSON copiados tal cual de ejemplos_api_perfil_fisico.md (backend). */
class PerfilFisicoMapperTest {

    private val gson = Gson()

    @Test
    fun `GET con perfil completo se convierte al dominio`() {
        val json = """{"perfil":{"actualizado_en":"2026-11-22T19:56:01-05:00","edad":28,"estatura_cm":175.0,"fecha_nacimiento":"1998-03-10","nivel":"intermedio","peso_kg":72.5,"sexo":"hombre"}}"""

        val perfil = gson.fromJson(json, PerfilFisicoResponse::class.java).perfilODominio()

        assertNotNull(perfil)
        assertEquals("1998-03-10", perfil!!.fechaNacimiento)
        assertEquals(28, perfil.edad)
        assertEquals("hombre", perfil.sexo)
        assertEquals(72.5, perfil.pesoKg, 0.0)
        assertEquals(175.0, perfil.estaturaCm, 0.0)
        assertEquals("intermedio", perfil.nivel)
        assertEquals("2026-11-22T19:56:01-05:00", perfil.actualizadoEn)
    }

    @Test
    fun `respuesta del PUT trae el mensaje y el perfil`() {
        val json = """{"message":"Perfil guardado","perfil":{"actualizado_en":"2026-11-22T19:56:01-05:00","edad":28,"estatura_cm":175.0,"fecha_nacimiento":"1998-03-10","nivel":"intermedio","peso_kg":72.5,"sexo":"hombre"}}"""

        val respuesta = gson.fromJson(json, PerfilFisicoResponse::class.java)

        assertEquals("Perfil guardado", respuesta.message)
        assertEquals(28, respuesta.perfilODominio()?.edad)
    }

    @Test
    fun `GET con perfil null significa que aun no lo lleno`() {
        val respuesta = gson.fromJson("""{"perfil":null}""", PerfilFisicoResponse::class.java)

        assertNull(respuesta.perfil)
        assertNull(respuesta.perfilODominio())
    }

    @Test
    fun `el cuerpo del PUT usa los nombres del backend y no envia la edad`() {
        val nuevo = NuevoPerfilFisico("1998-03-10", "hombre", 72.5, 175.0, "intermedio")

        val json = gson.toJsonTree(nuevo.aRequest()).asJsonObject

        assertEquals(
            setOf("fecha_nacimiento", "sexo", "peso_kg", "estatura_cm", "nivel"),
            json.keySet()
        )
        assertEquals("1998-03-10", json["fecha_nacimiento"].asString)
        assertEquals(72.5, json["peso_kg"].asDouble, 0.0)
        assertEquals(175.0, json["estatura_cm"].asDouble, 0.0)
    }
}
