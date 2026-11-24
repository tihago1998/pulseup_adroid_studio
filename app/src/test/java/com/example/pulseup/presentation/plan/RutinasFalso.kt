package com.example.pulseup.presentation.plan

import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.NuevaRutina
import com.example.pulseup.domain.model.Rutina
import com.example.pulseup.domain.repository.RutinasRepository

fun rutina(id: Int, nombre: String = "Rutina $id") =
    Rutina(id, nombre, "", "general", "intermedio", 45, 3, false, emptyList())

/** Rutinas falsas: listar devuelve [lista]; crear guarda la nueva y responde [idNueva] (o falla). */
class RutinasFalso(
    var lista: List<Rutina> = listOf(rutina(10, "Torso"), rutina(11, "Pierna")),
    var idNueva: Int = 50
) : RutinasRepository {
    val creadas = mutableListOf<NuevaRutina>()
    var fallarCrear: String? = null

    override suspend fun listar(objetivo: String?, nivel: String?) = Resultado.Exito(lista)
    override suspend fun detalle(id: Int): Resultado<Rutina> = Resultado.Fallo("no se usa")

    override suspend fun crear(nueva: NuevaRutina): Resultado<Int> {
        creadas += nueva
        return fallarCrear?.let { Resultado.Fallo(it) } ?: Resultado.Exito(idNueva)
    }

    override suspend fun eliminar(id: Int): Resultado<Unit> = Resultado.Fallo("no se usa")
}
