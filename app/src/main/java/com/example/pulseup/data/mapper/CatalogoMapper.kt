package com.example.pulseup.data.mapper

import com.example.pulseup.data.remote.dto.CreditosResponse
import com.example.pulseup.data.remote.dto.EjercicioDetalleDto
import com.example.pulseup.data.remote.dto.EjerciciosListaResponse
import com.example.pulseup.data.remote.dto.RecomendacionDto
import com.example.pulseup.domain.model.CreditoImagenes
import com.example.pulseup.domain.model.CreditosCatalogo
import com.example.pulseup.domain.model.EjercicioDetalle
import com.example.pulseup.domain.model.ImagenEjercicio
import com.example.pulseup.domain.model.PaginaEjercicios
import com.example.pulseup.domain.model.RecomendacionEjercicio
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/** Texto que el backend pone en "aviso"; se usa si por algún motivo no llega. */
const val AVISO_RECOMENDACION = "Es una guía general y no reemplaza a un entrenador ni a un médico."

/**
 * Las fotos llegan como rutas relativas (/static/ejercicios/...): se les antepone la URL del backend.
 * La URL base se interpreta con OkHttp, igual que la interpreta Retrofit para la API: así una base
 * mal escrita en local.properties (espacios, barras de más) sirve para las fotos si sirve para la API.
 * Una URL que ya es absoluta se deja igual; ruta vacía o null, o base inválida, da null.
 */
fun urlAbsoluta(baseUrl: String, ruta: String?): String? {
    if (ruta.isNullOrBlank()) return null
    val base = baseUrl.trim().toHttpUrlOrNull() ?: return null
    return base.resolve(ruta.trim())?.toString()
}

/** Lista paginada: se lee "items" y, si no viene, "ejercicios" (mismo contenido). */
fun EjerciciosListaResponse.aPagina(baseUrl: String): PaginaEjercicios {
    val lista = (items ?: ejercicios).orEmpty().map { it.aDominio(baseUrl) }
    return PaginaEjercicios(ejercicios = lista, total = total ?: lista.size)
}

fun RecomendacionDto.aDominio() = RecomendacionEjercicio(
    nivel = nivel.orEmpty(),
    unidad = unidad ?: "repeticiones",
    repsMin = repsMin ?: 0,
    repsMax = repsMax ?: repsMin ?: 0,
    seriesMin = seriesMin ?: 0,
    seriesMax = seriesMax ?: seriesMin ?: 0,
    descansoSeg = descansoSeg,
    rir = rir?.takeIf { it.isNotBlank() },
    diasSemana = diasSemana?.takeIf { it.isNotBlank() },
    rpe = rpe?.takeIf { it.isNotBlank() },
    pesoKg = pesoKg,
    nota = nota?.takeIf { it.isNotBlank() },
    aviso = aviso?.takeIf { it.isNotBlank() } ?: AVISO_RECOMENDACION
)

fun EjercicioDetalleDto.aDominio(baseUrl: String) = EjercicioDetalle(
    id = id,
    nombre = nombre.orEmpty(),
    musculoPrincipal = musculoPrincipal?.takeIf { it.isNotBlank() } ?: grupoMuscular.orEmpty(),
    musculosSecundarios = musculosSecundarios.orEmpty(),
    equipo = equipo.orEmpty(),
    nivel = nivel.orEmpty(),
    ambientes = ambientes.orEmpty(),
    imagenes = imagenes.orEmpty()
        .mapNotNull { img -> urlAbsoluta(baseUrl, img.url)?.let { ImagenEjercicio(img.orden ?: 0, it) } }
        .sortedBy { it.orden },
    // Si el ejercicio todavía no tiene "pasos", se muestran las instrucciones de siempre
    pasos = pasos?.takeIf { it.isNotEmpty() } ?: instrucciones.orEmpty(),
    respiracion = respiracion?.takeIf { it.isNotBlank() },
    erroresComunes = erroresComunes.orEmpty(),
    varianteFacil = varianteFacil?.takeIf { it.isNotBlank() },
    varianteDificil = varianteDificil?.takeIf { it.isNotBlank() },
    descansoSeg = descansoSeg,
    recomendacion = recomendacion?.aDominio(),
    recomendacionMotivo = recomendacionMotivo?.takeIf { it.isNotBlank() }
)

fun CreditosResponse.aDominio() = CreditosCatalogo(
    creditos = creditos.orEmpty().map {
        CreditoImagenes(
            fuente = it.fuente.orEmpty(),
            licencia = it.licencia.orEmpty(),
            autor = it.autor?.takeIf { a -> a.isNotBlank() },
            atribucion = it.atribucion?.takeIf { a -> a.isNotBlank() },
            imagenes = it.imagenes ?: 0
        )
    },
    textos = textos?.takeIf { it.isNotBlank() }
)
