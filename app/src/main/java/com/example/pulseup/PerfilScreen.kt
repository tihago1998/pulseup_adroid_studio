package com.example.pulseup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Paleta real PulseUp (tomada de styles.css)
val PuBgBase = Color(0xFF14110F)
val PuBgCard = Color(0xFF232020)
val PuBorder = Color(0xFF3A3633)
val PuAccent = Color(0xFF7F77DD)
val PuAccentDark = Color(0xFF26215C)
val PuTextPrimary = Color(0xFFFFFFFF)
val PuTextMuted = Color(0xFF837F79)
val PuAccentMid = Color(0xFFAFA9EC)


@Composable
fun TarjetaPerfil() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PuBgBase)
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = PuBgCard),
            border = BorderStroke(0.5.dp, PuBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Santiago Varela Peña",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = PuTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ficha 3223873 — ADSO",
                    fontSize = 14.sp,
                    color = PuTextMuted
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { /* acción editar */ },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PuAccent,
                            contentColor = PuAccentDark
                        )
                    ) {
                        Text("Editar", fontWeight = FontWeight.Medium)
                    }
                    OutlinedButton(
                        onClick = { /* acción cerrar sesión */ },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.5.dp, PuBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PuTextPrimary)
                    ) {
                        Text("Cerrar sesión", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF14110F)
@Composable
fun TarjetaPerfilPreview() {
    TarjetaPerfil()
}