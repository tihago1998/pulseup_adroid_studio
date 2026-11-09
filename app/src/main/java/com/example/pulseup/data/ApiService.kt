package com.example.pulseup.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    //------------------USUARIOS-----------------------
    // Registro: no requiere token, cualquiera puede llamarlo
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    // Login: no requiere token, devuelve el token que usaremos después
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    // Listar usuarios: SOLO funciona si el token pertenece a un Administrador (si no, 403)
    @GET("api/users/")
    suspend fun getUsers(@Header("Authorization") auth: String): Response<UsersListResponse>

    // Estadísticas de usuarios: total y distribución por rol
    @GET("api/users/stats")
    suspend fun getEstadisticas(@Header("Authorization") auth: String): Response<EstadisticasResponse>

    // Asignar roles a un usuario: {id} se reemplaza con @Path. Requiere ser Administrador.
    @PUT("api/users/{id}/roles")
    suspend fun updateRoles(
        @Header("Authorization") auth: String,
        @Path("id") id: Int,
        @Body body: UpdateRolesRequest
    ): Response<MessageResponse>

    // Ver mi propio perfil (el id sale del token, no se manda)
    @GET("api/users/profile")
    suspend fun getProfile(@Header("Authorization") auth: String): Response<ProfileResponse>

    // Editar mi nombre y correo
    @PUT("api/users/profile")
    suspend fun updateProfile(
        @Header("Authorization") auth: String,
        @Body body: UpdateProfileRequest
    ): Response<ProfileResponse>

    // Cambiar mi contraseña
    @PUT("api/auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") auth: String,
        @Body body: ChangePasswordRequest
    ): Response<MessageResponse>

    // Elimina un usuario. Requiere ser Administrador, y el backend impide auto-eliminarse (400)
    @DELETE("api/users/{id}")
    suspend fun eliminarUsuario(
        @Header("Authorization") auth: String,
        @Path("id") id: Int
    ): Response<MessageResponse>

    //------------------------------HABITOS--------------------------
    // Lista los hábitos del usuario dueño del token.
    // Si se manda fecha (YYYY-MM-DD) solo devuelve los de ese día; si es null, devuelve todos.
    @GET("api/habitos/")
    suspend fun getHabitos(
        @Header("Authorization") auth: String,
        @Query("fecha") fecha: String? = null
    ): Response<HabitosListResponse>

    // Registra un hábito para el usuario dueño del token (la fecha va dentro del body, opcional)
    @POST("api/habitos/")
    suspend fun crearHabito(
        @Header("Authorization") auth: String,
        @Body body: HabitoRequest
    ): Response<MessageResponse>

    // Marca o desmarca un hábito como cumplido. {id} es el id del hábito.
    @PATCH("api/habitos/{id}/completado")
    suspend fun marcarCompletado(
        @Header("Authorization") auth: String,
        @Path("id") id: Int,
        @Body body: CompletadoRequest
    ): Response<MessageResponse>

    // Resumen por día entre dos fechas (YYYY-MM-DD): lo usa el calendario para las llamas
    @GET("api/habitos/resumen")
    suspend fun getResumen(
        @Header("Authorization") auth: String,
        @Query("desde") desde: String,
        @Query("hasta") hasta: String
    ): Response<ResumenResponse>
}