package com.example.pulseup.data.mapper

import com.example.pulseup.data.remote.dto.CreditosResponse
import com.example.pulseup.data.remote.dto.EjercicioDetalleResponse
import com.example.pulseup.data.remote.dto.EjerciciosListaResponse
import com.example.pulseup.data.remote.dto.FiltrosResponse
import com.example.pulseup.data.remote.dto.RecomendacionResponse
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** JSON tomados de respuestas reales del backend (rama dev, 2026-11-23). */
class CatalogoMapperTest {

    private val gson = Gson()
    private val base = "http://192.168.1.10:5050"

    private val itemJson = """{"ambientes":["casa","calistenia"],"equipo":"peso corporal","grupo_muscular":"abdomen","id":135,"imagen_miniatura":null,"mecanica":"aislamiento","musculo_principal":"abdomen","musculos_secundarios":[],"nivel":"principiante","nombre":"Abdominal bicicleta"}"""

    // ---------- URL de las fotos ----------

    @Test
    fun `la ruta relativa de la foto se completa con la URL del backend`() {
        assertEquals("$base/static/ejercicios/Pushups/0.jpg", urlAbsoluta(base, "/static/ejercicios/Pushups/0.jpg"))
        assertEquals("$base/static/x.jpg", urlAbsoluta("$base/", "/static/x.jpg"))
        assertEquals("$base/static/x.jpg", urlAbsoluta(base, "static/x.jpg"))
    }

    @Test
    fun `una URL absoluta se deja igual y una vacia da null`() {
        assertEquals("https://cdn.x/y.jpg", urlAbsoluta(base, "https://cdn.x/y.jpg"))
        assertNull(urlAbsoluta(base, null))
        assertNull(urlAbsoluta(base, ""))
    }

    // ---------- Lista paginada ----------

    @Test
    fun `la pagina se lee desde items con el total`() {
        val json = """{"ejercicios":[$itemJson],"items":[$itemJson],"limit":2,"offset":0,"total":150}"""

        val pagina = gson.fromJson(json, EjerciciosListaResponse::class.java).aPagina(base)

        assertEquals(150, pagina.total)
        assertEquals(1, pagina.ejercicios.size)
        val e = pagina.ejercicios[0]
        assertEquals("Abdominal bicicleta", e.nombre)
        assertEquals("abdomen", e.musculoPrincipal)
        assertEquals(listOf("casa", "calistenia"), e.ambientes)
        assertNull(e.imagenMiniatura)
    }

    @Test
    fun `sin items la pagina usa ejercicios`() {
        val json = """{"ejercicios":[$itemJson],"total":1}"""
        assertEquals(1, gson.fromJson(json, EjerciciosListaResponse::class.java).aPagina(base).ejercicios.size)
    }

    @Test
    fun `la miniatura del listado sale con la URL completa`() {
        val json = """{"items":[{"id":1,"nombre":"Flexiones","grupo_muscular":"pecho","musculo_principal":"pecho","imagen_miniatura":"/static/ejercicios/Pushups/0.jpg"}],"total":1}"""
        val e = gson.fromJson(json, EjerciciosListaResponse::class.java).aPagina(base).ejercicios[0]
        assertEquals("$base/static/ejercicios/Pushups/0.jpg", e.imagenMiniatura)
    }

    @Test
    fun `sin musculo_principal se usa grupo_muscular (forma de las rutinas)`() {
        val json = """{"ejercicios":[{"id":3,"nombre":"Sentadilla","grupo_muscular":"cuadriceps"}]}"""
        val e = gson.fromJson(json, EjerciciosListaResponse::class.java).aPagina(base).ejercicios[0]
        assertEquals("cuadriceps", e.musculoPrincipal)
    }

    // ---------- Filtros y créditos ----------

    @Test
    fun `filtros incluye los ambientes`() {
        val json = """{"ambientes":["gimnasio","casa","calistenia"],"equipos":["barra","mancuernas"],"musculos":["abdomen","pecho"],"niveles":["principiante","intermedio","avanzado"]}"""
        val filtros = gson.fromJson(json, FiltrosResponse::class.java).aDominio()
        assertEquals(listOf("gimnasio", "casa", "calistenia"), filtros.ambientes)
        assertEquals(3, filtros.niveles.size)
    }

    @Test
    fun `creditos de las fotos`() {
        val json = """{"creditos":[{"atribucion":"Fotos del repositorio free-exercise-db de yuhonas (github.com/yuhonas/free-exercise-db)","autor":null,"fuente":"free-exercise-db","imagenes":284,"licencia":"Unlicense (repositorio); origen de las fotos no documentado"}],"textos":"Pasos, respiración, errores comunes y variantes redactados por el equipo de PulseUp."}"""
        val creditos = gson.fromJson(json, CreditosResponse::class.java).aDominio()
        assertEquals(1, creditos.creditos.size)
        assertEquals("free-exercise-db", creditos.creditos[0].fuente)
        assertEquals(284, creditos.creditos[0].imagenes)
        assertNull(creditos.creditos[0].autor)
        assertNotNull(creditos.textos)
    }

    // ---------- Detalle ----------

    @Test
    fun `detalle sin perfil trae el motivo y no la recomendacion`() {
        val json = """{"ejercicio":{"ambientes":["casa","calistenia"],"descanso_seg":30,"equipo":"peso corporal","errores_comunes":["Tirar del cuello con las manos.","Ir demasiado rápido."],"grupo_muscular":"abdomen","id":135,"imagen_miniatura":null,"imagenes":[],"instrucciones":["Acuéstate boca arriba."],"mecanica":"aislamiento","musculo_principal":"abdomen","musculos_secundarios":[],"nivel":"principiante","nombre":"Abdominal bicicleta","pasos":["Acuéstate boca arriba.","Eleva un poco los hombros del suelo."],"recomendacion":null,"recomendacion_motivo":"Completa tu perfil físico para ver la recomendación","respiracion":"Exhala en cada giro.","variante_dificil":"Ritmo más lento con pausa en cada giro.","variante_facil":"Mueve solo las piernas."}}"""

        val d = gson.fromJson(json, EjercicioDetalleResponse::class.java).ejercicio!!.aDominio(base)

        assertEquals("Abdominal bicicleta", d.nombre)
        assertEquals(2, d.pasos.size)
        assertEquals("Exhala en cada giro.", d.respiracion)
        assertEquals(2, d.erroresComunes.size)
        assertEquals("Mueve solo las piernas.", d.varianteFacil)
        assertEquals(30, d.descansoSeg)
        assertNull(d.recomendacion)
        assertEquals("Completa tu perfil físico para ver la recomendación", d.recomendacionMotivo)
    }

    @Test
    fun `las fotos del detalle quedan ordenadas y con URL completa`() {
        val json = """{"ejercicio":{"id":1,"nombre":"Flexiones","imagenes":[{"orden":1,"url":"/static/ejercicios/Pushups/1.jpg"},{"orden":0,"url":"/static/ejercicios/Pushups/0.jpg"},{"orden":2,"url":null}]}}"""

        val d = gson.fromJson(json, EjercicioDetalleResponse::class.java).ejercicio!!.aDominio(base)

        assertEquals(listOf(0, 1), d.imagenes.map { it.orden })
        assertEquals("$base/static/ejercicios/Pushups/0.jpg", d.imagenes[0].url)
    }

    @Test
    fun `sin pasos se usan las instrucciones`() {
        val json = """{"ejercicio":{"id":2,"nombre":"X","instrucciones":["Paso viejo"],"pasos":[]}}"""
        val d = gson.fromJson(json, EjercicioDetalleResponse::class.java).ejercicio!!.aDominio(base)
        assertEquals(listOf("Paso viejo"), d.pasos)
    }

    // ---------- Recomendación ----------

    @Test
    fun `recomendacion en repeticiones`() {
        val json = """{"recomendacion":{"aviso":"Es una guía general y no reemplaza a un entrenador ni a un médico.","descanso_seg":60,"dias_semana":null,"nivel":"intermedio","nota":null,"peso_kg":null,"reps_max":20,"reps_min":15,"rir":null,"rpe":null,"series_max":4,"series_min":3,"unidad":"repeticiones"},"recomendacion_motivo":null}"""

        val r = gson.fromJson(json, RecomendacionResponse::class.java).recomendacion!!.aDominio()

        assertEquals("repeticiones", r.unidad)
        assertEquals(15, r.repsMin)
        assertEquals(20, r.repsMax)
        assertEquals(3, r.seriesMin)
        assertEquals(4, r.seriesMax)
        assertEquals(60, r.descansoSeg)
        assertNull(r.pesoKg)
        assertNull(r.rir)
    }

    @Test
    fun `recomendacion de cardio en minutos con dias y esfuerzo`() {
        val json = """{"recomendacion":{"aviso":"Es una guía general y no reemplaza a un entrenador ni a un médico.","descanso_seg":null,"dias_semana":"3-4","nivel":"intermedio","nota":"Opción de intervalos.","peso_kg":null,"reps_max":40,"reps_min":30,"rir":null,"rpe":"5-6","series_max":1,"series_min":1,"unidad":"minutos"},"recomendacion_motivo":null}"""

        val r = gson.fromJson(json, RecomendacionResponse::class.java).recomendacion!!.aDominio()

        assertEquals("minutos", r.unidad)
        assertEquals("3-4", r.diasSemana)
        assertEquals("5-6", r.rpe)
        assertNull(r.descansoSeg)
        assertEquals("Opción de intervalos.", r.nota)
    }

    @Test
    fun `recomendacion con peso y RIR`() {
        val json = """{"recomendacion":{"aviso":"x","descanso_seg":150,"nivel":"intermedio","peso_kg":72.5,"reps_max":12,"reps_min":6,"rir":"2-3","series_max":4,"series_min":3,"unidad":"repeticiones"}}"""

        val r = gson.fromJson(json, RecomendacionResponse::class.java).recomendacion!!.aDominio()

        assertEquals(72.5, r.pesoKg!!, 0.0)
        assertEquals("2-3", r.rir)
    }

    @Test
    fun `sin aviso del backend se usa el aviso por defecto`() {
        val json = """{"recomendacion":{"unidad":"segundos","reps_min":30,"reps_max":45,"series_min":3,"series_max":3}}"""
        val r = gson.fromJson(json, RecomendacionResponse::class.java).recomendacion!!.aDominio()
        assertEquals(AVISO_RECOMENDACION, r.aviso)
    }
}
