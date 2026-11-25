package com.example.pulseup.data.mapper

import com.example.pulseup.data.remote.dto.AgregarEjercicioPlanResponse
import com.example.pulseup.data.remote.dto.AsignarDiaResponse
import com.example.pulseup.data.remote.dto.DiaPlanDto
import com.example.pulseup.data.remote.dto.EditarEjercicioPlanResponse
import com.example.pulseup.data.remote.dto.PlanResponse
import com.example.pulseup.data.remote.dto.PrevisualizarResponse
import com.example.pulseup.data.remote.dto.QuitarEjercicioPlanResponse
import com.example.pulseup.domain.model.CambiosEjercicioPlan
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** JSON tomados del back real (informe_plan_ejercicios.txt y verificación sobre una base temporal). */
class PlanEjerciciosMapperTest {

    private val gson = Gson()
    private val base = "http://10.0.2.2:5050"

    private val planJson = """
        {"plan":[
          {"dia_semana":1,"ejercicios":[
             {"descanso_seg":150,"ejercicio_id":3,"imagen_miniatura":"/static/ejercicios/Barbell_Bench_Press/0.jpg","item_id":36,
              "musculo_principal":"pecho","nombre":"Press de banca con barra","peso_kg":70.0,"repeticiones":"9","series":3,"unidad":"repeticiones"},
             {"descanso_seg":45,"ejercicio_id":41,"imagen_miniatura":null,"item_id":37,"musculo_principal":"abdomen",
              "nombre":"Plancha","peso_kg":null,"repeticiones":"40","series":3,"unidad":"segundos"}],
           "nombre_dia":"lunes","rutina":{"grupos_musculares":["abdomen","pecho"],"id":8,"nombre":"Lunes · pecho"}},
          {"dia_semana":2,"ejercicios":[],"nombre_dia":"martes","rutina":null}
        ]}
    """.trimIndent()

    private val avisoJson = """{"dia_conflicto":1,"mensaje":"El lunes ya trabajas pecho: tu pecho necesita 48 horas de descanso","musculo":"pecho","tipo":"descanso_48h"}"""

    // ---------- Ejercicios del plan ----------

    @Test
    fun `plan real con ejercicios por dia`() {
        val dias = gson.fromJson(planJson, PlanResponse::class.java).plan!!.map { it.aDominio(base) }

        val lunes = dias[0]
        assertEquals("Lunes · pecho", lunes.rutina?.nombre)
        assertEquals(2, lunes.ejercicios.size)
        val banca = lunes.ejercicios[0]
        assertEquals(36, banca.itemId)
        assertEquals(3, banca.ejercicioId)
        assertEquals("Press de banca con barra", banca.nombre)
        assertEquals("pecho", banca.musculoPrincipal)
        assertEquals("$base/static/ejercicios/Barbell_Bench_Press/0.jpg", banca.imagenMiniatura)
        assertEquals(3, banca.series)
        assertEquals("9", banca.repeticiones)
        assertEquals("repeticiones", banca.unidad)
        assertEquals(70.0, banca.pesoKg!!, 0.0)
        assertEquals(150, banca.descansoSeg)

        val plancha = lunes.ejercicios[1]
        assertEquals("segundos", plancha.unidad)
        assertEquals("40", plancha.repeticiones)
        assertNull(plancha.pesoKg)
        assertNull(plancha.imagenMiniatura)

        assertNull(dias[1].rutina)
        assertTrue(dias[1].ejercicios.isEmpty())
    }

    @Test
    fun `dia sin la lista de ejercicios (back anterior) queda vacio`() {
        val dia = DiaPlanDto(3, "miércoles", null).aDominio()
        assertTrue(dia.ejercicios.isEmpty())
    }

    @Test
    fun `ejercicio con campos nulos usa valores seguros`() {
        val json = """{"dia_semana":4,"nombre_dia":"jueves","rutina":null,"ejercicios":[{"item_id":5,"ejercicio_id":9}]}"""
        val e = gson.fromJson(json, DiaPlanDto::class.java).aDominio().ejercicios.single()
        assertEquals("repeticiones", e.unidad)
        assertEquals(0, e.descansoSeg)
        assertEquals("", e.repeticiones)
        assertNull(e.pesoKg)
    }

    // ---------- Previsualizar y avisos ----------

    @Test
    fun `previsualizar real con avisos por dia`() {
        val json = """
            {"dias":[
              {"avisos":[{"dia_conflicto":2,"mensaje":"El martes ya trabajas pecho: tu pecho necesita 48 horas de descanso","musculo":"pecho","tipo":"descanso_48h"}],
               "dia":1,"nombre_dia":"lunes","tiene_rutina":true},
              {"avisos":[],"dia":4,"nombre_dia":"jueves","tiene_rutina":false},
              {"avisos":[$avisoJson],"dia":7,"nombre_dia":"domingo","tiene_rutina":false}
            ]}
        """.trimIndent()
        val dias = gson.fromJson(json, PrevisualizarResponse::class.java).aDominio()

        assertEquals(listOf(1, 4, 7), dias.map { it.dia })
        assertTrue(dias[0].tieneRutina)
        assertEquals("lunes", dias[0].nombreDia)
        val aviso = dias[0].avisos.single()
        assertEquals("descanso_48h", aviso.tipo)
        assertEquals("pecho", aviso.musculo)
        assertEquals(2, aviso.diaConflicto)
        assertEquals("El martes ya trabajas pecho: tu pecho necesita 48 horas de descanso", aviso.mensaje)
        assertTrue(dias[1].avisos.isEmpty())
        assertFalse(dias[1].tieneRutina)
    }

    @Test
    fun `previsualizar ordena los dias y descarta los invalidos y avisos sin mensaje`() {
        val json = """{"dias":[{"dia":3,"avisos":[{"tipo":"descanso_48h"}]},{"dia":9},{"dia":1,"avisos":null}]}"""
        val dias = gson.fromJson(json, PrevisualizarResponse::class.java).aDominio()
        assertEquals(listOf(1, 3), dias.map { it.dia })
        assertTrue(dias[1].avisos.isEmpty())
        assertFalse(dias[0].tieneRutina)
    }

    @Test
    fun `asignar rutina devuelve el dia con ejercicios y los avisos`() {
        val json = """
            {"avisos":[$avisoJson],
             "dia":{"dia_semana":3,"ejercicios":[{"descanso_seg":60,"ejercicio_id":15,"imagen_miniatura":null,"item_id":39,
               "musculo_principal":"espalda","nombre":"Remo con barra","peso_kg":50.0,"repeticiones":"8-10","series":3,"unidad":"repeticiones"}],
               "nombre_dia":"miércoles","rutina":{"grupos_musculares":["abdomen","espalda","pecho"],"id":10,"nombre":"Torso"}},
             "message":"Rutina asignada"}
        """.trimIndent()
        val r = gson.fromJson(json, AsignarDiaResponse::class.java).aDominio()
        assertEquals(3, r.dia.diaSemana)
        assertEquals("Torso", r.dia.rutina?.nombre)
        assertEquals("8-10", r.dia.ejercicios.single().repeticiones)
        assertEquals(1, r.avisos.size)
    }

    @Test
    fun `asignar sin avisos (back anterior) no falla`() {
        val json = """{"dia":{"dia_semana":2,"nombre_dia":"martes","rutina":{"id":4,"nombre":"Pierna"}},"message":"ok"}"""
        val r = gson.fromJson(json, AsignarDiaResponse::class.java).aDominio()
        assertTrue(r.avisos.isEmpty())
        assertEquals("Pierna", r.dia.rutina?.nombre)
    }

    // ---------- Agregar, editar y quitar ----------

    @Test
    fun `agregar con aviso`() {
        val json = """
            {"avisos":[$avisoJson],
             "item":{"descanso_seg":75,"ejercicio_id":6,"imagen_miniatura":null,"item_id":38,"musculo_principal":"pecho",
                     "nombre":"Aperturas con mancuernas","peso_kg":null,"repeticiones":"12","series":4,"unidad":"repeticiones"},
             "rutina_copiada":false}
        """.trimIndent()
        val r = gson.fromJson(json, AgregarEjercicioPlanResponse::class.java).aDominio()
        assertEquals(38, r.item.itemId)
        assertEquals("Aperturas con mancuernas", r.item.nombre)
        assertEquals(1, r.avisos.size)
        assertFalse(r.rutinaCopiada)
    }

    @Test
    fun `editar en rutina compartida trae la copia con otro item_id`() {
        val json = """
            {"item":{"descanso_seg":60,"ejercicio_id":15,"imagen_miniatura":null,"item_id":42,"musculo_principal":"espalda",
                     "nombre":"Remo con barra","peso_kg":50.0,"repeticiones":"10","series":3,"unidad":"repeticiones"},
             "rutina_copiada":true}
        """.trimIndent()
        val r = gson.fromJson(json, EditarEjercicioPlanResponse::class.java).aDominio()
        assertEquals(42, r.item.itemId)
        assertTrue(r.rutinaCopiada)
    }

    @Test
    fun `quitar el ultimo ejercicio deja el dia en descanso`() {
        val json = """{"dia":{"dia_semana":2,"ejercicios":[],"nombre_dia":"martes","rutina":null},"dia_en_descanso":true,"message":"Ejercicio quitado del día","rutina_copiada":false}"""
        val r = gson.fromJson(json, QuitarEjercicioPlanResponse::class.java).aDominio()
        assertTrue(r.diaEnDescanso)
        assertFalse(r.rutinaCopiada)
        assertNull(r.dia?.rutina)
    }

    @Test
    fun `cuerpo de la edicion envia peso_kg null para quitar el peso`() {
        val json = gson.fromJson(CambiosEjercicioPlan(3, "8-10", null, 90).cuerpoEdicion(), JsonObject::class.java)
        assertEquals(3, json["series"].asInt)
        assertEquals("8-10", json["repeticiones"].asString)
        assertTrue(json.has("peso_kg"))
        assertTrue(json["peso_kg"].isJsonNull)
        assertEquals(90, json["descanso_seg"].asInt)
        assertFalse(json.has("unidad"))
    }

    @Test
    fun `cuerpo de la edicion con peso`() {
        val json = gson.fromJson(CambiosEjercicioPlan(4, "9", 72.5, 150).cuerpoEdicion(), JsonObject::class.java)
        assertEquals(72.5, json["peso_kg"].asDouble, 0.0)
    }
}
