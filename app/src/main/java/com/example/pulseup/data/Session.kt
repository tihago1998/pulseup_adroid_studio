package com.example.pulseup.data

// Guarda en memoria los datos de la sesión actual, mientras la app está abierta.
object Session {

    var token: String? = null
    var usuario: Usuario? = null

    // true para Administrador y para SuperAdministrador: ambos deben ver
    // la opción de "Administrar usuarios" en el menú
    val isAdmin: Boolean
        get() = usuario?.roles?.let {
            it.contains("Administrador") || it.contains("SuperAdministrador")
        } == true

    // Solo el nivel más alto: quién puede tocar cuentas de otros administradores
    // y eliminar usuarios
    val isSuperAdmin: Boolean
        get() = usuario?.roles?.contains("SuperAdministrador") == true

    // Primer nombre del usuario para saludos ("Santiago Varela Peña" -> "Santiago").
    // Si no hay sesión o el nombre viene vacío, devuelve "Usuario".
    val primerNombre: String
        get() = usuario?.nombre
            ?.substringBefore(" ")
            ?.takeIf { it.isNotBlank() }
            ?: "Usuario"

    // Devuelve el token ya formateado como lo espera el encabezado Authorization
    fun bearer(): String = "Bearer $token"

    // Se llama justo después de un login exitoso
    fun iniciarSesion(loginResponse: LoginResponse) {
        token = loginResponse.token
        usuario = loginResponse.usuario
    }

    // Se llama al cerrar sesión
    fun cerrarSesion() {
        token = null
        usuario = null
    }
}