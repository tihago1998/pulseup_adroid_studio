package com.example.pulseup.data.remote

import com.example.pulseup.data.remote.dto.AgregarEjercicioPlanRequest
import com.example.pulseup.data.remote.dto.AgregarEjercicioPlanResponse
import com.example.pulseup.data.remote.dto.AsignarDiaRequest
import com.example.pulseup.data.remote.dto.AsignarDiaResponse
import com.example.pulseup.data.remote.dto.CalendarioResponse
import com.example.pulseup.data.remote.dto.CompletarResponse
import com.example.pulseup.data.remote.dto.EditarEjercicioPlanResponse
import com.example.pulseup.data.remote.dto.PlanResponse
import com.example.pulseup.data.remote.dto.PrevisualizarResponse
import com.example.pulseup.data.remote.dto.QuitarEjercicioPlanResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Plan semanal (rutina y ejercicios de cada día), marcar la rutina del día y calendario. {dia}: lunes = 1 … domingo = 7. */
interface PlanApi {

    @GET("api/plan/")
    suspend fun getPlan(@Header("Authorization") auth: String): Response<PlanResponse>

    // La regla de 48 h ya no bloquea: guarda siempre y devuelve los choques en "avisos"
    @PUT("api/plan/{dia}")
    suspend fun asignarDia(
        @Header("Authorization") auth: String,
        @Path("dia") dia: Int,
        @Body body: AsignarDiaRequest
    ): Response<AsignarDiaResponse>

    @DELETE("api/plan/{dia}")
    suspend fun quitarDia(
        @Header("Authorization") auth: String,
        @Path("dia") dia: Int
    ): Response<ResponseBody>

    @POST("api/plan/completar")
    suspend fun completarHoy(@Header("Authorization") auth: String): Response<CompletarResponse>

    @GET("api/calendario/")
    suspend fun getCalendario(
        @Header("Authorization") auth: String,
        @Query("mes") mes: String? = null
    ): Response<CalendarioResponse>

    /** Solo uno de los dos: ejercicio_id o rutina_id (con ambos o ninguno responde 400). */
    @GET("api/plan/previsualizar")
    suspend fun previsualizar(
        @Header("Authorization") auth: String,
        @Query("ejercicio_id") ejercicioId: Int? = null,
        @Query("rutina_id") rutinaId: Int? = null
    ): Response<PrevisualizarResponse>

    /** 201 con el item; 409 si el ejercicio ya está ese día. */
    @POST("api/plan/{dia}/ejercicios")
    suspend fun agregarEjercicio(
        @Header("Authorization") auth: String,
        @Path("dia") dia: Int,
        @Body body: AgregarEjercicioPlanRequest
    ): Response<AgregarEjercicioPlanResponse>

    /** El cuerpo va ya serializado (con peso_kg null incluido). Si la rutina se copia, item_id cambia. */
    @PUT("api/plan/{dia}/ejercicios/{itemId}")
    suspend fun editarEjercicio(
        @Header("Authorization") auth: String,
        @Path("dia") dia: Int,
        @Path("itemId") itemId: Int,
        @Body body: RequestBody
    ): Response<EditarEjercicioPlanResponse>

    @DELETE("api/plan/{dia}/ejercicios/{itemId}")
    suspend fun quitarEjercicio(
        @Header("Authorization") auth: String,
        @Path("dia") dia: Int,
        @Path("itemId") itemId: Int
    ): Response<QuitarEjercicioPlanResponse>
}
