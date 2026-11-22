package com.example.pulseup.data.remote

import com.example.pulseup.data.remote.dto.AsignarDiaRequest
import com.example.pulseup.data.remote.dto.AsignarDiaResponse
import com.example.pulseup.data.remote.dto.CalendarioResponse
import com.example.pulseup.data.remote.dto.CompletarResponse
import com.example.pulseup.data.remote.dto.PlanResponse
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

/** Plan semanal, marcar la rutina del día y calendario. */
interface PlanApi {

    @GET("api/plan/")
    suspend fun getPlan(@Header("Authorization") auth: String): Response<PlanResponse>

    // 409 si rompe la regla de descanso de 48 h (el mensaje viene en "error")
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
}
