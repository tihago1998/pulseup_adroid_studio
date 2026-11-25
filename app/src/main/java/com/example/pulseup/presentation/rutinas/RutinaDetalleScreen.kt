package com.example.pulseup.presentation.rutinas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.ui.theme.PuAccent
import com.example.pulseup.ui.theme.PuBgCard
import com.example.pulseup.ui.theme.PuTextMuted
import com.example.pulseup.ui.theme.PuTextPrimary
import com.example.pulseup.domain.model.EjercicioDeRutina
import com.example.pulseup.presentation.componentes.EstadoCarga
import com.example.pulseup.presentation.componentes.EstadoError
import com.example.pulseup.presentation.componentes.EtiquetaPu
import com.example.pulseup.presentation.componentes.PantallaPu
import com.example.pulseup.presentation.componentes.textoBonito
import com.example.pulseup.ui.theme.PuBorder
import com.example.pulseup.ui.theme.PuTextSecondary

@Composable
fun RutinaDetalleScreen(
    viewModel: RutinaDetalleViewModel,
    onVolver: () -> Unit,
    onEjercicioClick: (Int) -> Unit,
    onEliminada: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    var confirmando by remember { mutableStateOf(false) }
    val rutina = estado.rutina

    // Cuando el backend confirma el borrado, se avisa para volver a la lista
    LaunchedEffect(estado.eliminada) {
        if (estado.eliminada) onEliminada()
    }

    PantallaPu(titulo = "Rutina", onVolver = onVolver) {
        when {
            estado.cargando -> EstadoCarga()
            estado.error != null -> EstadoError(estado.error!!, viewModel::cargar)
            rutina != null -> Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(rutina.nombre, color = PuTextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                if (rutina.descripcion.isNotBlank()) {
                    Text(rutina.descripcion, color = PuTextSecondary, fontSize = 14.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EtiquetaPu(textoBonito(rutina.objetivo))
                    EtiquetaPu(textoBonito(rutina.nivel))
                    EtiquetaPu("${rutina.duracionMin} min")
                }

                Text("Ejercicios", color = PuTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                rutina.ejercicios.forEach { item ->
                    FilaEjercicio(item, onClick = { onEjercicioClick(item.ejercicio.id) })
                }

                if (!rutina.esPlantilla) {
                    estado.errorAlEliminar?.let { Text(it, color = Color(0xFFE57373), fontSize = 13.sp) }
                    Button(
                        onClick = { confirmando = true },
                        enabled = !estado.eliminando,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5A2A2A),
                            contentColor = Color(0xFFFFB4AB)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (estado.eliminando) "Eliminando..." else "Eliminar rutina") }
                }
            }
        }
    }

    if (confirmando) {
        AlertDialog(
            onDismissRequest = { confirmando = false },
            title = { Text("¿Eliminar rutina?") },
            text = { Text("Se borrará \"${rutina?.nombre.orEmpty()}\". Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmando = false
                    viewModel.eliminar()
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { confirmando = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun FilaEjercicio(item: EjercicioDeRutina, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PuBgCard),
        border = BorderStroke(0.5.dp, PuBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("${item.orden}", color = PuAccent, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.ejercicio.nombre, color = PuTextPrimary, fontSize = 15.sp)
                Text(
                    "${item.series} series × ${item.repeticiones} · descanso ${item.descansoSeg} s",
                    color = PuTextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}
