package com.example.pulseup.data.remote

import com.example.pulseup.data.MessageResponse
import com.example.pulseup.data.remote.dto.CrearRutinaResponse
import com.example.pulseup.data.remote.dto.CreditosResponse
import com.example.pulseup.data.remote.dto.EjercicioDetalleResponse
import com.example.pulseup.data.remote.dto.EjerciciosListaResponse
import com.example.pulseup.data.remote.dto.FiltrosResponse
import com.example.pulseup.data.remote.dto.NuevaRutinaRequest
import com.example.pulseup.data.remote.dto.RecomendacionResponse
import com.example.pulseup.data.remote.dto.RutinaResponse
import com.example.pulseup.data.remote.dto.RutinasListaResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoints de ejercicios y rutinas. Todos requieren el token (como el resto de la API). */
interface EntrenamientoApi {

    // Catálogo con filtros opcionales: los parámetros en null no se envían.
    // Sin limit/offset devuelve TODOS (lo usa el selector de crear rutina).
    @GET("api/ejercicios/")
    suspend fun getEjercicios(
        @Header("Authorization") auth: String,
        @Query("musculo") musculo: String? = null,
        @Query("equipo") equipo: String? = null,
        @Query("nivel") nivel: String? = null,
        @Query("q") texto: String? = null
    ): Response<EjerciciosListaResponse>

    // Catálogo paginado: limit por defecto 20 (máx. 100) y offset desde 0
    @GET("api/ejercicios/")
    suspend fun buscarEjercicios(
        @Header("Authorization") auth: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("q") texto: String? = null,
        @Query("ambiente") ambiente: String? = null,
        @Query("equipo") equipo: String? = null,
        @Query("musculo") musculo: String? = null,
        @Query("nivel") nivel: String? = null
    ): Response<EjerciciosListaResponse>

    @GET("api/ejercicios/filtros")
    suspend fun getFiltros(@Header("Authorization") auth: String): Response<FiltrosResponse>

    @GET("api/ejercicios/creditos")
    suspend fun getCreditos(@Header("Authorization") auth: String): Response<CreditosResponse>

    @GET("api/ejercicios/{id}")
    suspend fun getEjercicio(
        @Header("Authorization") auth: String,
        @Path("id") id: Int
    ): Response<EjercicioDetalleResponse>

    @GET("api/ejercicios/{id}/recomendacion")
    suspend fun getRecomendacion(
        @Header("Authorization") auth: String,
        @Path("id") id: Int
    ): Response<RecomendacionResponse>

    // Plantillas del sistema + rutinas propias del usuario
    @GET("api/rutinas/")
    suspend fun getRutinas(
        @Header("Authorization") auth: String,
        @Query("objetivo") objetivo: String? = null,
        @Query("nivel") nivel: String? = null
    ): Response<RutinasListaResponse>

    @GET("api/rutinas/{id}")
    suspend fun getRutina(
        @Header("Authorization") auth: String,
        @Path("id") id: Int
    ): Response<RutinaResponse>

    @POST("api/rutinas/")
    suspend fun crearRutina(
        @Header("Authorization") auth: String,
        @Body body: NuevaRutinaRequest
    ): Response<CrearRutinaResponse>

    @DELETE("api/rutinas/{id}")
    suspend fun eliminarRutina(
        @Header("Authorization") auth: String,
        @Path("id") id: Int
    ): Response<MessageResponse>
}
