package com.example.pulseup.presentation.rutinas

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.PuAccent
import com.example.pulseup.PuAccentDark
import com.example.pulseup.PuBgCard
import com.example.pulseup.PuTextMuted
import com.example.pulseup.PuTextPrimary
import com.example.pulseup.domain.model.Catalogos
import com.example.pulseup.domain.model.Rutina
import com.example.pulseup.presentation.componentes.EstadoCarga
import com.example.pulseup.presentation.componentes.EstadoError
import com.example.pulseup.presentation.componentes.EstadoVacio
import com.example.pulseup.presentation.componentes.EtiquetaPu
import com.example.pulseup.presentation.componentes.PantallaPu
import com.example.pulseup.presentation.componentes.SeccionChips
import com.example.pulseup.presentation.componentes.textoBonito
import com.example.pulseup.ui.theme.PuBorder

@Composable
fun RutinasScreen(
    viewModel: RutinasViewModel,
    onVolver: () -> Unit,
    onRutinaClick: (Int) -> Unit,
    onCrearClick: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()

    PantallaPu(titulo = "Rutinas", onVolver = onVolver) {
        Button(
            onClick = onCrearClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PuAccent, contentColor = PuAccentDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) { Text("Crear mi rutina") }

        SeccionChips("Objetivo", Catalogos.OBJETIVOS, estado.objetivo, viewModel::elegirObjetivo)

        when {
            estado.cargando && estado.rutinas.isEmpty() -> EstadoCarga()
            estado.error != null -> EstadoError(estado.error!!, viewModel::cargar)
            estado.rutinas.isEmpty() -> EstadoVacio("No hay rutinas con ese objetivo")
            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(estado.rutinas, key = { it.id }) { rutina ->
                    TarjetaRutina(rutina, onClick = { onRutinaClick(rutina.id) })
                }
            }
        }
    }
}

@Composable
private fun TarjetaRutina(rutina: Rutina, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PuBgCard),
        border = BorderStroke(0.5.dp, if (rutina.esPlantilla) PuBorder else PuAccent),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(rutina.nombre, color = PuTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            if (rutina.descripcion.isNotBlank()) {
                Text(rutina.descripcion, color = PuTextMuted, fontSize = 13.sp)
            }
            Text(
                "${rutina.totalEjercicios} ejercicios · ${rutina.duracionMin} min",
                color = PuTextMuted,
                fontSize = 13.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                EtiquetaPu(textoBonito(rutina.objetivo))
                EtiquetaPu(textoBonito(rutina.nivel))
                if (!rutina.esPlantilla) EtiquetaPu("Mía")
            }
        }
    }
}
