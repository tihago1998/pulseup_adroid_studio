package com.example.pulseup.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.ui.theme.PuAccent
import com.example.pulseup.ui.theme.PuBgCard
import com.example.pulseup.ui.theme.PuTextMuted
import com.example.pulseup.ui.theme.PuTextPrimary
import com.example.pulseup.domain.model.EstadoDia
import com.example.pulseup.presentation.plan.BotonMarcarHoy
import com.example.pulseup.presentation.plan.CalendarioViewModel
import com.example.pulseup.presentation.plan.PuRojo
import com.example.pulseup.ui.theme.PuBorder

/** Tarjeta del Home: qué toca hoy según el plan semanal, la racha y el botón para marcarlo. */
@Composable
fun TarjetaPlanHoy(
    viewModel: CalendarioViewModel,
    onPlanClick: () -> Unit,
    onCalendarioClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsState()
    val hoy = estado.hoy
    val rutinaHoy = hoy?.rutina
    val racha = estado.calendario?.racha?.actual ?: 0

    val titulo = when {
        estado.cargando && estado.calendario == null -> "Cargando tu plan…"
        estado.error != null && estado.calendario == null -> "No se pudo cargar tu plan"
        hoy == null || rutinaHoy == null -> "Hoy es tu día de descanso"
        hoy.estado == EstadoDia.CUMPLIDO -> "¡Rutina de hoy cumplida! 🔥"
        else -> "Hoy toca: ${rutinaHoy.nombre}"
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PuBgCard),
        border = BorderStroke(0.5.dp, PuBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()) {
                Text(titulo, color = PuTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f))
                Text("🔥 $racha", color = PuTextPrimary, fontSize = 14.sp)
            }
            estado.aviso?.let {
                Text(it, color = if (estado.avisoEsError) PuRojo else PuTextMuted, fontSize = 12.sp)
            }
            if (estado.puedeMarcarHoy || estado.completando) {
                BotonMarcarHoy(estado.puedeMarcarHoy, estado.completando, viewModel::marcarHoy)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onPlanClick) { Text("Mi plan", color = PuAccent, fontSize = 13.sp) }
                TextButton(onClick = onCalendarioClick) { Text("Calendario", color = PuAccent, fontSize = 13.sp) }
            }
        }
    }
}
