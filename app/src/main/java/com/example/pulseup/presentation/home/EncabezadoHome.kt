package com.example.pulseup.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
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
import com.example.pulseup.ui.theme.PuAccent
import com.example.pulseup.ui.theme.PuAdvertencia
import com.example.pulseup.ui.theme.PuTextMuted
import com.example.pulseup.ui.theme.PuTextPrimary
import com.example.pulseup.ui.theme.PuTextSecondary
import java.time.LocalTime

enum class IconoSaludo { SOL, LUNA }

data class Saludo(val texto: String, val icono: IconoSaludo)

/**
 * Saludo según la hora del teléfono:
 * 05:00–11:59 "Buenos días" (sol), 12:00–18:59 "Buenas tardes" (sol), 19:00–04:59 "Buenas noches" (luna).
 */
fun saludoPara(hora: LocalTime): Saludo = when (hora.hour) {
    in 5..11 -> Saludo("Buenos días", IconoSaludo.SOL)
    in 12..18 -> Saludo("Buenas tardes", IconoSaludo.SOL)
    else -> Saludo("Buenas noches", IconoSaludo.LUNA)
}

@Composable
fun EncabezadoHome(nombre: String, modifier: Modifier = Modifier) {
    val saludo = remember { saludoPara(LocalTime.now()) }
    Column(modifier = modifier.fillMaxWidth()) {
        Text("${saludo.texto},", color = PuTextMuted, fontSize = 13.sp)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                nombre,
                color = PuTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f, fill = false)
            )
            when (saludo.icono) {
                IconoSaludo.SOL -> Icon(
                    Icons.Default.WbSunny,
                    contentDescription = "Sol",
                    tint = PuAdvertencia,
                    modifier = Modifier.size(22.dp)
                )
                IconoSaludo.LUNA -> Icon(
                    Icons.Default.DarkMode,
                    contentDescription = "Luna",
                    tint = PuAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
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
