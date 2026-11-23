package com.example.pulseup.presentation.ejercicios

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.pulseup.ui.theme.PuAccent
import com.example.pulseup.ui.theme.PuBgCard
import com.example.pulseup.ui.theme.PuTextMuted
import com.example.pulseup.ui.theme.PuTextPrimary
import com.example.pulseup.presentation.componentes.EstadoCarga
import com.example.pulseup.presentation.componentes.EstadoError
import com.example.pulseup.presentation.componentes.EtiquetaPu
import com.example.pulseup.presentation.componentes.PantallaPu
import com.example.pulseup.presentation.componentes.textoBonito
import com.example.pulseup.ui.theme.PuBorder
import com.example.pulseup.ui.theme.PuTextSecondary

@Composable
fun EjercicioDetalleScreen(viewModel: EjercicioDetalleViewModel, onVolver: () -> Unit) {
    val estado by viewModel.estado.collectAsState()
    val ejercicio = estado.ejercicio

    PantallaPu(titulo = "Ejercicio", onVolver = onVolver) {
        when {
            estado.cargando -> EstadoCarga()
            estado.error != null -> EstadoError(estado.error!!, viewModel::cargar)
            ejercicio != null -> Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(ejercicio.nombre, color = PuTextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Medium)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EtiquetaPu(textoBonito(ejercicio.musculoPrincipal))
                    EtiquetaPu(textoBonito(ejercicio.equipo))
                    EtiquetaPu(textoBonito(ejercicio.nivel))
                }

                if (ejercicio.musculosSecundarios.isNotEmpty()) {
                    Text(
                        "También trabaja: " + ejercicio.musculosSecundarios.joinToString(", ") { textoBonito(it) },
                        color = PuTextSecondary,
                        fontSize = 14.sp
                    )
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PuBgCard),
                    border = BorderStroke(0.5.dp, PuBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Cómo hacerlo", color = PuTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        ejercicio.pasos.forEachIndexed { indice, paso ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("${indice + 1}.", color = PuAccent, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(paso, color = PuTextSecondary, fontSize = 14.sp)
                            }
                        }
                        if (ejercicio.pasos.isEmpty()) {
                            Text("Sin instrucciones", color = PuTextMuted, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
