package com.example.pulseup.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.ui.theme.PuAccent
import com.example.pulseup.ui.theme.PuTextMuted
import com.example.pulseup.ui.theme.PuBgSurface
import com.example.pulseup.ui.theme.PuBorder

/** Barra inferior del Home: Inicio, Calendario, Ejercicios y Perfil. */
@Composable
fun BarraInferior(
    onInicioClick: () -> Unit,
    onCalendarioClick: () -> Unit,
    onEjerciciosClick: () -> Unit,
    onPerfilClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PuBgSurface)
            .navigationBarsPadding()
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(PuBorder)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemBarra("Inicio", Icons.Default.Home, activo = true, onClick = onInicioClick)
            ItemBarra("Calendario", Icons.Default.DateRange, activo = false, onClick = onCalendarioClick)
            ItemBarra("Ejercicios", Icons.Default.FitnessCenter, activo = false, onClick = onEjerciciosClick)
            ItemBarra("Perfil", Icons.Default.Person, activo = false, onClick = onPerfilClick)
        }
    }
}

@Composable
private fun ItemBarra(
    etiqueta: String,
    icono: ImageVector,
    activo: Boolean,
    onClick: () -> Unit
) {
    val color = if (activo) PuAccent else PuTextMuted
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Icon(icono, contentDescription = etiqueta, tint = color, modifier = Modifier.size(22.dp))
        Text(etiqueta, color = color, fontSize = 11.sp)
    }
}
