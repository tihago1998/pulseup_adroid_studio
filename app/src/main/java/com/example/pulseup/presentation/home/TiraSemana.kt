package com.example.pulseup.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.PuAccent
import com.example.pulseup.PuAccentDark
import com.example.pulseup.PuAccentMid
import com.example.pulseup.PuTextMuted
import com.example.pulseup.PuTextPrimary
import com.example.pulseup.domain.model.DiaDeSemana
import com.example.pulseup.domain.model.EstadoDia
import com.example.pulseup.presentation.plan.PuRojo
import com.example.pulseup.ui.theme.PuTextSecondary

private val LETRAS_DIA = listOf("L", "M", "M", "J", "V", "S", "D")

/** Lunes a domingo de la semana actual con el estado de cada día. Hoy va resaltado. */
@Composable
fun TiraSemana(
    dias: List<DiaDeSemana>,
    cargando: Boolean,
    error: String?,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (dias.isEmpty()) {
                // Mientras carga (o si falló la primera vez) se ven las letras sin estado
                LETRAS_DIA.forEach { letra ->
                    CeldaSemana(letra, numero = "", estado = null, esHoy = false, Modifier.weight(1f))
                }
            } else {
                dias.forEachIndexed { i, dia ->
                    CeldaSemana(
                        letra = LETRAS_DIA[i],
                        numero = dia.fecha.dayOfMonth.toString(),
                        estado = dia.estado,
                        esHoy = dia.esHoy,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        when {
            error != null && !cargando -> Row(verticalAlignment = Alignment.CenterVertically) {
                Text(error, color = PuTextMuted, fontSize = 12.sp, modifier = Modifier.weight(1f, fill = false))
                TextButton(onClick = onReintentar) { Text("Reintentar", color = PuAccent, fontSize = 12.sp) }
            }
            dias.isNotEmpty() -> Text(
                "🔥 cumplido   ● pendiente   ✖ incumplido   · descanso",
                color = PuTextMuted,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun CeldaSemana(
    letra: String,
    numero: String,
    estado: EstadoDia?,
    esHoy: Boolean,
    modifier: Modifier = Modifier
) {
    val forma = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .clip(forma)
            .background(if (esHoy) PuAccentDark else Color.Transparent)
            .then(if (esHoy) Modifier.border(0.5.dp, PuAccent, forma) else Modifier)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(letra, fontSize = 11.sp, color = if (esHoy) PuAccentMid else PuTextMuted)
        Text(
            numero,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (esHoy) PuTextPrimary else PuTextSecondary
        )
        Column(modifier = Modifier.height(18.dp), verticalArrangement = Arrangement.Center) {
            when (estado) {
                EstadoDia.CUMPLIDO -> Text("🔥", fontSize = 12.sp)
                EstadoDia.PENDIENTE -> Text("●", color = PuAccent, fontSize = 11.sp)
                EstadoDia.INCUMPLIDO -> Text("✖", color = PuRojo, fontSize = 12.sp)
                EstadoDia.DESCANSO -> Text("·", color = PuTextMuted, fontSize = 14.sp)
                null -> Unit
            }
        }
    }
}
