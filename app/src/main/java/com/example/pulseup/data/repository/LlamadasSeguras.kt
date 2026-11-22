package com.example.pulseup.data.repository

import com.example.pulseup.data.remote.dto.ErrorResponse
import com.example.pulseup.domain.Resultado
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException

/**
 * Ejecuta una llamada de Retrofit y la convierte en un [Resultado] con un mensaje claro,
 * así las pantallas nunca ven excepciones ni códigos HTTP.
 */
internal suspend fun <D, T> ejecutar(
    llamada: suspend () -> Response<D>,
    convertir: (D) -> T
): Resultado<T> = try {
    val respuesta = llamada()
    if (respuesta.isSuccessful) {
        val cuerpo = respuesta.body()
        if (cuerpo == null) Resultado.Fallo("El servidor respondió vacío")
        else Resultado.Exito(convertir(cuerpo))
    } else {
        Resultado.Fallo(mensajeDeError(respuesta))
    }
} catch (e: CancellationException) {
    throw e // no se debe tragar la cancelación de una corrutina
} catch (e: IOException) {
    Resultado.Fallo("No se pudo conectar con el servidor. Revisa tu conexión.")
} catch (e: Exception) {
    Resultado.Fallo("Ocurrió un error inesperado")
}

private fun mensajeDeError(respuesta: Response<*>): String {
    if (respuesta.code() == 401) return "Tu sesión expiró. Inicia sesión de nuevo."
    val texto = try {
        respuesta.errorBody()?.string()
    } catch (e: Exception) {
        null
    }
    val delBackend = try {
        Gson().fromJson(texto, ErrorResponse::class.java)?.error
    } catch (e: Exception) {
        null
    }
    return delBackend ?: "Error del servidor (${respuesta.code()})"
}
