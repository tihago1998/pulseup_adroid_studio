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
import com.example.pulseup.ui.theme.PuBgCard
import com.example.pulseup.ui.theme.PuTextMuted
import com.example.pulseup.ui.theme.PuTextPrimary
import com.example.pulseup.ui.theme.PuBorder

/**
 * Tarjetas del Home que llevan al catálogo de ejercicios, a las rutinas y,
 * si se pasa [onPerfilFisicoClick], al perfil físico (debajo, a lo ancho).
 */
@Composable
fun AccesoEntrenamiento(
    onEjerciciosClick: () -> Unit,
    onRutinasClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPerfilFisicoClick: (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TarjetaAcceso("🏋️", "Ejercicios", "Catálogo y filtros", onEjerciciosClick, Modifier.weight(1f))
            TarjetaAcceso("📋", "Rutinas", "Plantillas y las tuyas", onRutinasClick, Modifier.weight(1f))
        }
        if (onPerfilFisicoClick != null) {
            TarjetaAcceso(
                "📏",
                "Mi perfil físico",
                "Edad, peso, estatura y nivel",
                onPerfilFisicoClick,
                Modifier.fillMaxWidth()
            )
        }
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
