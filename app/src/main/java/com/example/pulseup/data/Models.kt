package com.example.pulseup.data

// ---------- Login ----------
// Lo que la app ENVÍA al hacer login
data class LoginRequest(
    val email: String,
    val password: String
)

// Lo que el backend DEVUELVE al hacer login (coincide con lo que armamos en auth.py)
data class LoginResponse(
    val token: String,
    val usuario: Usuario
)

// ---------- Registro ----------
// Lo que la app ENVÍA al registrarse
data class RegisterRequest(
    val nombre: String,
    val email: String,
    val password: String
)

// Lo que el backend DEVUELVE al registrarse
data class RegisterResponse(
    val message: String?,
    val id_usuario: Int?
)

// ---------- Usuario ----------
// Representa un usuario tal como lo devuelve el backend
data class Usuario(
    val id_usuario: Int,
    val nombre: String,
    val email: String,
    val roles: List<String>
)

// ---------- Perfil ----------
// Lo que enviamos al editar nombre y correo
data class UpdateProfileRequest(
    val nombre: String,
    val email: String
)

// El backend responde { "usuario": {...} } tanto al ver como al editar el perfil
data class ProfileResponse(
    val message: String? = null,
    val usuario: Usuario?
)

// ---------- Cambiar contraseña ----------
// Nombres EXACTOS que espera el backend: current_password y new_password
data class ChangePasswordRequest(
    val current_password: String,
    val new_password: String
)

// ---------- Listar usuarios (solo admin) ----------
// El backend responde { "usuarios": [ ... ] }
data class UsersListResponse(
    val usuarios: List<Usuario>?
)

// ---------- Asignar roles (solo admin) ----------
// role_ids = [1] para Administrador, [2] para Usuario
data class UpdateRolesRequest(
    val role_ids: List<Int>
)

// ---------- Respuesta genérica con solo un mensaje ----------
data class MessageResponse(
    val message: String?
)

// ---------- Hábitos ----------
// Lo que el backend devuelve por cada hábito guardado
data class Habito(
    val id: Int,
    val descripcion: String,
    val categoria: String,
    val fecha: String
)

// El backend responde { "habitos": [ ... ] }
data class HabitosListResponse(
    val habitos: List<Habito>?
)

// Lo que enviamos al registrar uno nuevo (el id_usuario sale del token, no se manda)
data class HabitoRequest(
    val descripcion: String,
    val categoria: String
)

// ---------- Estadísticas ----------
data class EstadisticasResponse(
    val total: Int,
    val por_rol: Map<String, Int>
)