package com.example.pulseup.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.PuBgCard
import com.example.pulseup.PuTextMuted
import com.example.pulseup.PuTextPrimary
import com.example.pulseup.ui.theme.PuBorder

/** Dos tarjetas en el Home que llevan al catálogo de ejercicios y a las rutinas. */
@Composable
fun AccesoEntrenamiento(
    onEjerciciosClick: () -> Unit,
    onRutinasClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        TarjetaAcceso("🏋️", "Ejercicios", "Catálogo y filtros", onEjerciciosClick, Modifier.weight(1f))
        TarjetaAcceso("📋", "Rutinas", "Plantillas y las tuyas", onRutinasClick, Modifier.weight(1f))
    }
}

@Composable
private fun TarjetaAcceso(
    emoji: String,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PuBgCard),
        border = BorderStroke(0.5.dp, PuBorder),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(emoji, fontSize = 22.sp)
            Column {
                Text(titulo, color = PuTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(subtitulo, color = PuTextMuted, fontSize = 11.sp)
            }
        }
    }
}
