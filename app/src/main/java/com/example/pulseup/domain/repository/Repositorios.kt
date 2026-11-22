package com.example.pulseup.domain.repository

import com.example.pulseup.domain.Resultado
import com.example.pulseup.domain.model.Ejercicio
import com.example.pulseup.domain.model.FiltrosEjercicio
import com.example.pulseup.domain.model.NuevaRutina
import com.example.pulseup.domain.model.Rutina

interface EjerciciosRepository {
    suspend fun listar(
        musculo: String? = null,
        equipo: String? = null,
        nivel: String? = null,
        texto: String? = null
    ): Resultado<List<Ejercicio>>

    suspend fun filtros(): Resultado<FiltrosEjercicio>
    suspend fun detalle(id: Int): Resultado<Ejercicio>
}

interface RutinasRepository {
    suspend fun listar(objetivo: String? = null, nivel: String? = null): Resultado<List<Rutina>>
    suspend fun detalle(id: Int): Resultado<Rutina>
    suspend fun crear(nueva: NuevaRutina): Resultado<Int>
    suspend fun eliminar(id: Int): Resultado<Unit>
}
