package com.example.pulseup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DetalleHabitoScreen(
    descripcion: String,
    categoria: String,
    onVolverClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PuBgBase)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TextButton(onClick = onVolverClick) {
                Text("← Volver", color = PuAccent)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Detalle del hábito",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = PuTextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Descripción: $descripcion",
                fontSize = 16.sp,
                color = PuTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Categoría: $categoria",
                fontSize = 16.sp,
                color = PuTextMuted
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF14110F)
@Composable
fun DetalleHabitoScreenPreview() {
    DetalleHabitoScreen(descripcion = "30 min de caminata", categoria = "Ejercicio")
}