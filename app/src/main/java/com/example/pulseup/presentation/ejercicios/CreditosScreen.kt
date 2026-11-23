package com.example.pulseup.presentation.ejercicios

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.domain.model.CreditoImagenes
import com.example.pulseup.presentation.componentes.EstadoCarga
import com.example.pulseup.presentation.componentes.EstadoError
import com.example.pulseup.presentation.componentes.EstadoVacio
import com.example.pulseup.presentation.componentes.PantallaPu
import com.example.pulseup.ui.theme.PuBgCard
import com.example.pulseup.ui.theme.PuBorder
import com.example.pulseup.ui.theme.PuTextMuted
import com.example.pulseup.ui.theme.PuTextPrimary
import com.example.pulseup.ui.theme.PuTextSecondary

/** Fuentes, licencias y atribuciones de las fotos del catálogo. */
@Composable
fun CreditosScreen(viewModel: CreditosViewModel, onVolver: () -> Unit) {
    val estado by viewModel.estado.collectAsState()
    val creditos = estado.creditos

    PantallaPu(titulo = "Créditos de las fotos", onVolver = onVolver) {
        when {
            estado.cargando -> EstadoCarga()
            estado.error != null -> EstadoError(estado.error!!, viewModel::cargar)
            creditos == null || (creditos.creditos.isEmpty() && creditos.textos == null) ->
                EstadoVacio("Todavía no hay créditos registrados")
            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(creditos.creditos) { TarjetaCredito(it) }
                creditos.textos?.let { textos ->
                    item { Text(textos, color = PuTextMuted, fontSize = 13.sp) }
                }
            }
        }
    }
}

@Composable
private fun TarjetaCredito(credito: CreditoImagenes) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PuBgCard),
        border = BorderStroke(0.5.dp, PuBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(credito.fuente, color = PuTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text("Licencia: ${credito.licencia}", color = PuTextSecondary, fontSize = 13.sp)
            credito.autor?.let { Text("Autor: $it", color = PuTextSecondary, fontSize = 13.sp) }
            credito.atribucion?.let { Text(it, color = PuTextSecondary, fontSize = 13.sp) }
            Text(
                if (credito.imagenes == 1) "1 foto" else "${credito.imagenes} fotos",
                color = PuTextMuted,
                fontSize = 12.sp
            )
        }
    }
}
