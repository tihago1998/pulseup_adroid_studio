package com.example.pulseup.presentation.ejercicios

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.PuBgCard
import com.example.pulseup.PuTextMuted
import com.example.pulseup.PuTextPrimary
import com.example.pulseup.domain.model.Ejercicio
import com.example.pulseup.presentation.componentes.EstadoCarga
import com.example.pulseup.presentation.componentes.EstadoError
import com.example.pulseup.presentation.componentes.EstadoVacio
import com.example.pulseup.presentation.componentes.EtiquetaPu
import com.example.pulseup.presentation.componentes.PantallaPu
import com.example.pulseup.presentation.componentes.SeccionChips
import com.example.pulseup.presentation.componentes.coloresCampoPu
import com.example.pulseup.presentation.componentes.textoBonito
import com.example.pulseup.ui.theme.PuBorder

@Composable
fun EjerciciosScreen(
    viewModel: EjerciciosViewModel,
    onVolver: () -> Unit,
    onEjercicioClick: (Int) -> Unit
) {
    val estado by viewModel.estado.collectAsState()

    PantallaPu(titulo = "Ejercicios", onVolver = onVolver) {
        OutlinedTextField(
            value = estado.texto,
            onValueChange = viewModel::cambiarTexto,
            placeholder = { Text("Buscar ejercicio") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = coloresCampoPu(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        estado.filtros?.let { filtros ->
            SeccionChips("Músculo", filtros.musculos, estado.musculo, viewModel::elegirMusculo)
            SeccionChips("Equipo", filtros.equipos, estado.equipo, viewModel::elegirEquipo)
        }

        when {
            estado.cargando && estado.ejercicios.isEmpty() -> EstadoCarga()
            estado.error != null -> EstadoError(estado.error!!, viewModel::reintentar)
            estado.ejercicios.isEmpty() -> EstadoVacio("No hay ejercicios con esos filtros")
            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(estado.ejercicios, key = { it.id }) { ejercicio ->
                    TarjetaEjercicio(ejercicio, onClick = { onEjercicioClick(ejercicio.id) })
                }
            }
        }
    }
}

@Composable
fun TarjetaEjercicio(ejercicio: Ejercicio, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PuBgCard),
        border = BorderStroke(0.5.dp, PuBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(ejercicio.nombre, color = PuTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(
                "${textoBonito(ejercicio.grupoMuscular)} · ${textoBonito(ejercicio.equipo)}",
                color = PuTextMuted,
                fontSize = 13.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                EtiquetaPu(textoBonito(ejercicio.nivel))
            }
        }
    }
}
