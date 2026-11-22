package com.example.pulseup.domain

/** Resultado de una operación: o trae los datos, o trae un mensaje listo para mostrar al usuario. */
sealed interface Resultado<out T> {
    data class Exito<T>(val datos: T) : Resultado<T>
    data class Fallo(val mensaje: String) : Resultado<Nothing>
}
