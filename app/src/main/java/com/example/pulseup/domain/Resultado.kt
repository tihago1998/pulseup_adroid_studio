package com.example.pulseup.domain

/** Resultado de una operación: o trae los datos, o trae un mensaje listo para mostrar al usuario. */
sealed interface Resultado<out T> {
    data class Exito<T>(val datos: T) : Resultado<T>
    /** [codigo] = código HTTP cuando el servidor respondió con error (por ejemplo 409); null si no hubo respuesta. */
    data class Fallo(val mensaje: String, val codigo: Int? = null) : Resultado<Nothing>
}
