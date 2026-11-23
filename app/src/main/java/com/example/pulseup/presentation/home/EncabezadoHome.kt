package com.example.pulseup.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.PuTextMuted
import com.example.pulseup.PuTextPrimary
import com.example.pulseup.ui.theme.PuTextSecondary
import java.time.LocalTime

/** Saludo según la hora del día (0 a 23). */
fun saludoPorHora(hora: Int): String = when (hora) {
    in 5..11 -> "Buenos días"
    in 12..17 -> "Buenas tardes"
    else -> "Buenas noches"
}

@Composable
fun EncabezadoHome(nombre: String, modifier: Modifier = Modifier) {
    val saludo = remember { saludoPorHora(LocalTime.now().hour) }
    Column(modifier = modifier.fillMaxWidth()) {
        Text("$saludo,", color = PuTextMuted, fontSize = 13.sp)
        Text(nombre, color = PuTextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun FraseMotivadora(frase: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = frase,
            color = PuTextSecondary,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center
        )
    }
}
