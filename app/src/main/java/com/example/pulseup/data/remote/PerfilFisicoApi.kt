package com.example.pulseup.data.remote

import com.example.pulseup.data.remote.dto.PerfilFisicoRequest
import com.example.pulseup.data.remote.dto.PerfilFisicoResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT

/** Perfil físico del usuario: fecha de nacimiento, sexo, peso, estatura y nivel. */
interface PerfilFisicoApi {

    // 200 con {"perfil": null} si aún no lo ha llenado
    @GET("api/perfil-fisico/")
    suspend fun getPerfil(@Header("Authorization") auth: String): Response<PerfilFisicoResponse>

    // Crea o reemplaza. 400 con {"error": "..."} si algún dato no es válido
    @PUT("api/perfil-fisico/")
    suspend fun guardarPerfil(
        @Header("Authorization") auth: String,
        @Body body: PerfilFisicoRequest
    ): Response<PerfilFisicoResponse>

    // 200 con {"message": "Perfil eliminado"}, también si no existía
    @DELETE("api/perfil-fisico/")
    suspend fun eliminarPerfil(@Header("Authorization") auth: String): Response<ResponseBody>
}
