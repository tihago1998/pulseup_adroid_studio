package com.example.pulseup.data.mapper

import com.example.pulseup.data.remote.dto.EjercicioDeRutinaDto
import com.example.pulseup.data.remote.dto.EjercicioDto
import com.example.pulseup.data.remote.dto.FiltrosResponse
import com.example.pulseup.data.remote.dto.ItemRutinaRequest
import com.example.pulseup.data.remote.dto.NuevaRutinaRequest
import com.example.pulseup.data.remote.dto.RutinaDto
import com.example.pulseup.domain.model.Ejercicio
import com.example.pulseup.domain.model.EjercicioDeRutina
import com.example.pulseup.domain.model.FiltrosCatalogo
import com.example.pulseup.domain.model.NuevaRutina
import com.example.pulseup.domain.model.Rutina

// Convierte lo que llega del backend (DTO) en los modelos que usa el resto de la app.

/** [baseUrl] completa la URL relativa de la miniatura (/static/ejercicios/...). */
fun EjercicioDto.aDominio(baseUrl: String = "") = Ejercicio(
    id = id,
    nombre = nombre.orEmpty(),
    grupoMuscular = grupoMuscular.orEmpty(),
    musculosSecundarios = musculosSecundarios.orEmpty(),
    equipo = equipo.orEmpty(),
    nivel = nivel.orEmpty(),
    instrucciones = instrucciones.orEmpty(),
    musculoPrincipal = musculoPrincipal?.takeIf { it.isNotBlank() } ?: grupoMuscular.orEmpty(),
    ambientes = ambientes.orEmpty(),
    imagenMiniatura = urlAbsoluta(baseUrl, imagenMiniatura)
)

fun FiltrosResponse.aDominio() = FiltrosCatalogo(
    ambientes = ambientes.orEmpty(),
    musculos = musculos.orEmpty(),
    equipos = equipos.orEmpty(),
    niveles = niveles.orEmpty()
)

fun EjercicioDeRutinaDto.aDominio(): EjercicioDeRutina? {
    val detalle = ejercicio ?: return null
    return EjercicioDeRutina(
        orden = orden ?: 0,
        series = series ?: 0,
        repeticiones = repeticiones.orEmpty(),
        descansoSeg = descansoSeg ?: 0,
        ejercicio = detalle.aDominio()
    )
}

fun RutinaDto.aDominio() = Rutina(
    id = id,
    nombre = nombre.orEmpty(),
    descripcion = descripcion.orEmpty(),
    objetivo = objetivo.orEmpty(),
    nivel = nivel.orEmpty(),
    duracionMin = duracionMin ?: 0,
    totalEjercicios = totalEjercicios ?: ejercicios?.size ?: 0,
    esPlantilla = esPlantilla ?: false,
    ejercicios = ejercicios.orEmpty().mapNotNull { it.aDominio() },
    origen = origen
)

fun NuevaRutina.aRequest() = NuevaRutinaRequest(
    nombre = nombre,
    descripcion = descripcion,
    objetivo = objetivo,
    nivel = nivel,
    duracionMin = duracionMin,
    ejercicios = items.map {
        ItemRutinaRequest(
            idEjercicio = it.idEjercicio,
            series = it.series,
            repeticiones = it.repeticiones,
            descansoSeg = it.descansoSeg
        )
    }
)
