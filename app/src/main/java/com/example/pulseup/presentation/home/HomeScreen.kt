package com.example.pulseup.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pulseup.PuBgBase
import com.example.pulseup.presentation.plan.CalendarioViewModel
import kotlinx.coroutines.launch

/**
 * Home alimentado por el plan semanal y el calendario:
 * saludo, frase, tira de la semana, "Hoy toca", progreso de la semana y accesos.
 */
@Composable
fun HomeScreen(
    nombre: String,
    frase: String,
    viewModel: HomeViewModel,
    planHoy: CalendarioViewModel,
    onPlanClick: () -> Unit,
    onCalendarioClick: () -> Unit,
    onEjerciciosClick: () -> Unit,
    onRutinasClick: () -> Unit,
    onPerfilFisicoClick: () -> Unit,
    onEditarPerfilClick: () -> Unit,
    onCambiarPasswordClick: () -> Unit,
    onAdminClick: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val estadoPlan by planHoy.estado.collectAsState()
    var mostrarMenu by remember { mutableStateOf(false) }
    val lista = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Cada vez que "Hoy toca" termina de cargar (al abrir, al volver al Home o al marcar la rutina),
    // se recalculan la tira y el progreso reutilizando el mes que ya trajo.
    LaunchedEffect(estadoPlan.cargando) {
        if (!estadoPlan.cargando) viewModel.cargar(estadoPlan.calendario)
    }

    MenuPerfil(
        visible = mostrarMenu,
        onCerrar = { mostrarMenu = false },
        onEditarPerfilClick = onEditarPerfilClick,
        onCambiarPasswordClick = onCambiarPasswordClick,
        onAdminClick = onAdminClick,
        onCerrarSesion = onCerrarSesion
    )

    Scaffold(
        containerColor = PuBgBase,
        bottomBar = {
            BarraInferior(
                onInicioClick = { scope.launch { lista.animateScrollToItem(0) } },
                onCalendarioClick = onCalendarioClick,
                onEjerciciosClick = onEjerciciosClick,
                onPerfilClick = { mostrarMenu = true }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = lista,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { EncabezadoHome(nombre) }
            item { FraseMotivadora(frase) }
            item {
                TiraSemana(
                    dias = estado.tira,
                    cargando = estado.cargando,
                    error = estado.error,
                    onReintentar = { viewModel.cargar(estadoPlan.calendario) }
                )
            }
            item {
                TarjetaPlanHoy(
                    viewModel = planHoy,
                    onPlanClick = onPlanClick,
                    onCalendarioClick = onCalendarioClick
                )
            }
            item { TarjetaProgresoSemana(estado.progreso, onPlanClick) }
            item {
                AccesoEntrenamiento(
                    onEjerciciosClick = onEjerciciosClick,
                    onRutinasClick = onRutinasClick,
                    onPerfilFisicoClick = onPerfilFisicoClick
                )
            }
        }
    }
}
