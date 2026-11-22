package com.example.pulseup.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.pulseup.BuildConfig
import com.example.pulseup.data.remote.EntrenamientoApi

object RetrofitClient {

    // La URL del backend viene de local.properties (pulseup.baseUrl).
    private const val BASE_URL = BuildConfig.BASE_URL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS) // 30 segundos
        .readTimeout(30, TimeUnit.SECONDS)    // 30 segundos
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Usuarios, perfil y hábitos
    val api: ApiService by lazy { retrofit.create(ApiService::class.java) }

    // Ejercicios y rutinas
    val entrenamientoApi: EntrenamientoApi by lazy { retrofit.create(EntrenamientoApi::class.java) }
}
