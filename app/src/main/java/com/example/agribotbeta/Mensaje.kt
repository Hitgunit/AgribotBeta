

package com.example.agribotbeta

// Definición de la clase de datos "Mensaje"
data class Mensaje(
    // Propiedad "texto" para almacenar el texto del mensaje
    val texto: String,
    // Propiedad "esBot" para indicar si el mensaje fue enviado por el bot (true) o por el usuario (false)
    val esBot: Boolean,
    // Propiedad "imagen" opcional para almacenar la URL de una imagen asociada con el mensaje. Si no hay imagen, puede ser nula.
    val imagen: String? = null
)


