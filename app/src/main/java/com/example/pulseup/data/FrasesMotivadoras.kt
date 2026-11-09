package com.example.pulseup.data


// Frases que se muestran en el Home. Se elige una nueva cada vez que el usuario inicia sesión.
object FrasesMotivadoras {

    private val frases = listOf(
        "Cada pequeño hábito de hoy es una versión más fuerte de ti mañana.",
        "No tienes que hacerlo perfecto, solo constante.",
        "Hoy es un buen día para cuidar de ti.",
        "Un paso a la vez también es avanzar.",
        "Tu cuerpo escucha todo lo que haces. Dale razones para agradecerte.",
        "Descansar también es parte del progreso.",
        "Empieza con lo que tienes a la mano; lo demás llega en el camino.",
        "Si hoy no tienes ganas, hazlo cinco minutos y luego decides.",
        "Beber agua, dormir bien y moverte: lo simple también cambia vidas.",
        "Tu racha empieza con el primer registro de hoy.",
        "Cuidarte no es un lujo, es la base para todo lo demás.",
        "Lo importante no es la velocidad, es no abandonar.",
        "Cada vez que vuelves, ya ganaste algo.",
        "Hoy sumas, mañana se nota.",
        "Los grandes cambios se arman con días normales bien aprovechados."
    )

    // Devuelve una frase al azar, evitando repetir la que se mostró la vez anterior
    fun aleatoria(excluir: String? = null): String {
        val opciones = frases.filter { it != excluir }
        return opciones.random()
    }
}