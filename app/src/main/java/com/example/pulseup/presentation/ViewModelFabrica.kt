package com.example.pulseup.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Permite crear un ViewModel que recibe parámetros: viewModel(factory = fabricaDe { MiVm(repo) }) */
fun <VM : ViewModel> fabricaDe(crear: () -> VM): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = crear() as T
    }
