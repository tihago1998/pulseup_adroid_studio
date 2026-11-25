package com.example.pulseup.di

import com.example.pulseup.data.RetrofitClient
import com.example.pulseup.data.Session
import com.example.pulseup.data.repository.EjerciciosRepositoryImpl
import com.example.pulseup.data.repository.PerfilFisicoRepositoryImpl
import com.example.pulseup.data.repository.PlanRepositoryImpl
import com.example.pulseup.data.repository.RutinasRepositoryImpl
import com.example.pulseup.domain.repository.EjerciciosRepository
import com.example.pulseup.domain.repository.PerfilFisicoRepository
import com.example.pulseup.domain.repository.PlanRepository
import com.example.pulseup.domain.repository.RutinasRepository

/** Único lugar donde se arma cada pieza. Las pantallas piden aquí sus repositorios. */
object Dependencias {
    val ejerciciosRepository: EjerciciosRepository by lazy {
        EjerciciosRepositoryImpl(RetrofitClient.entrenamientoApi, RetrofitClient.baseUrl) { Session.bearer() }
    }
    val rutinasRepository: RutinasRepository by lazy {
        RutinasRepositoryImpl(RetrofitClient.entrenamientoApi) { Session.bearer() }
    }
    val planRepository: PlanRepository by lazy {
        PlanRepositoryImpl(RetrofitClient.planApi, RetrofitClient.baseUrl) { Session.bearer() }
    }
    val perfilFisicoRepository: PerfilFisicoRepository by lazy {
        PerfilFisicoRepositoryImpl(RetrofitClient.perfilFisicoApi) { Session.bearer() }
    }
}
