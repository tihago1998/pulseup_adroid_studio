package com.example.pulseup.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.PuAccent
import com.example.pulseup.PuAccentDark
import com.example.pulseup.PuAccentMid
import com.example.pulseup.PuTextPrimary
import com.example.pulseup.domain.model.ProgresoSemana
import com.example.pulseup.ui.theme.PuBorder

/** "X de Y rutinas" de la semana con barra. Sin rutinas planificadas, invita a armar el plan. */
@Composable
fun TarjetaProgresoSemana(
    progreso: ProgresoSemana?,
    onPlanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PuAccentDark),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Progreso de la semana", color = PuAccentMid, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    when {
                        progreso == null -> Text("…", color = PuTextPrimary, fontSize = 28.sp)
                        progreso.sinPlan -> Text(
                            "Aún no tienes rutinas esta semana",
                            color = PuTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        else -> Text(
                            "${progreso.hechas} de ${progreso.planificadas} rutinas",
                            color = PuTextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = PuAccent,
                    modifier = Modifier.size(30.dp)
                )
            }

            if (progreso != null && progreso.sinPlan) {
                TextButton(onClick = onPlanClick) { Text("Armar mi plan semanal", color = PuAccent) }
            } else {
                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(PuBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progreso?.fraccion ?: 0f)
                            .fillMaxHeight()
                            .background(PuAccent)
                    )
                }
            }
        }
    }
}
