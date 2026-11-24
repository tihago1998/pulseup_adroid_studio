package com.example.pulseup.presentation.plan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.ui.theme.PuAccent
import com.example.pulseup.ui.theme.PuAccentDark
import com.example.pulseup.ui.theme.PuBgCard
import com.example.pulseup.ui.theme.PuTextMuted
import com.example.pulseup.ui.theme.PuTextPrimary
import com.example.pulseup.domain.model.DiaPlan
import com.example.pulseup.domain.model.EjercicioPlan
import com.example.pulseup.domain.model.Rutina
import com.example.pulseup.presentation.componentes.EstadoCarga
import com.example.pulseup.presentation.componentes.EstadoError
import com.example.pulseup.presentation.componentes.EtiquetaPu
import com.example.pulseup.presentation.componentes.ImagenRemota
import com.example.pulseup.presentation.componentes.PantallaPu
import com.example.pulseup.presentation.componentes.textoBonito
import com.example.pulseup.presentation.ejercicios.textoSeriesPlan
import com.example.pulseup.ui.theme.PuTextSecondary
import com.example.pulseup.ui.theme.PuBorder
import com.example.pulseup.ui.theme.PuRojo

@Composable
fun PlanSemanalScreen(
    viewModel: PlanSemanalViewModel,
    onVolver: () -> Unit,
    onDiaClick: (Int) -> Unit
) {
    val estado by viewModel.estado.collectAsState()

    PantallaPu(titulo = "Mi plan semanal", onVolver = onVolver) {
        when {
            estado.cargando && estado.dias.isEmpty() -> EstadoCarga()
            estado.error != null -> EstadoError(estado.error!!, viewModel::cargar)
            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "Toca un día para ver, editar o agregar sus ejercicios, o elige una rutina completa. " +
                            "Un músculo necesita 48 h de descanso antes de volver a entrenarse: si no se cumple, te avisamos.",
                        color = PuTextMuted,
                        fontSize = 13.sp
                    )
                }
                if (estado.guardando) {
                    item { Text("Guardando...", color = PuTextMuted, fontSize = 14.sp) }
                }
                items(estado.notas) { nota -> TarjetaNota(nota, onCerrar = { viewModel.cerrarNota(nota) }) }
                items(estado.dias, key = { it.diaSemana }) { dia ->
                    TarjetaDia(
                        dia = dia,
                        onClick = { onDiaClick(dia.diaSemana) },
                        onElegirRutina = { viewModel.abrirSelector(dia.diaSemana) }
                    )
                }
            }
        }
    }

    estado.diaEditando?.let { diaElegido ->
        SelectorRutina(
            nombreDia = estado.dias.firstOrNull { it.diaSemana == diaElegido }?.nombreDia.orEmpty(),
            rutinas = estado.rutinas,
            tieneRutina = estado.dias.firstOrNull { it.diaSemana == diaElegido }?.rutina != null,
            onElegir = viewModel::elegirRutina,
            onDescanso = viewModel::dejarEnDescanso,
            onCerrar = viewModel::cerrarSelector
        )
    }

    estado.porConfirmar?.let { p ->
        DialogoAvisos(
            titulo = if (p.nombreRutina.isBlank()) "¿Asignar al ${p.nombreDia}?" else "¿Asignar ${p.nombreRutina} al ${p.nombreDia}?",
            avisos = p.avisos,
            confirmar = "Asignar igual",
            onConfirmar = viewModel::confirmarAsignacion,
            onCancelar = viewModel::cancelarAsignacion
        )
    }
}

@Composable
private fun TarjetaDia(dia: DiaPlan, onClick: () -> Unit, onElegirRutina: () -> Unit) {
    val rutina = dia.rutina
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PuBgCard),
        border = BorderStroke(0.5.dp, if (rutina != null) PuAccent else PuBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(textoBonito(dia.nombreDia), color = PuTextMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = onElegirRutina, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(if (rutina == null) "Elegir rutina" else "Cambiar rutina", color = PuAccent, fontSize = 13.sp)
                }
            }
            if (rutina == null) {
                Text("Descanso · toca para agregar ejercicios", color = PuTextMuted, fontSize = 15.sp)
            } else {
                Text(rutina.nombre, color = PuTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    rutina.gruposMusculares.forEach { EtiquetaPu(textoBonito(it)) }
                }
            }
            dia.ejercicios.forEach { e -> FilaEjercicio(e) }
        }
    }
}

/** Miniatura, nombre y "3 × 10 · 40 kg". */
@Composable
private fun FilaEjercicio(e: EjercicioPlan) {
    Row(
        modifier = Modifier.padding(end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ImagenRemota(
            url = e.imagenMiniatura,
            descripcion = e.nombre,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(e.nombre, color = PuTextPrimary, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(textoSeriesPlan(e.series, e.repeticiones, e.unidad, e.pesoKg), color = PuTextSecondary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun SelectorRutina(
    nombreDia: String,
    rutinas: List<Rutina>,
    tieneRutina: Boolean,
    onElegir: (Int) -> Unit,
    onDescanso: () -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = PuBgCard,
        title = { Text("Rutina del ${nombreDia}", color = PuTextPrimary, fontSize = 18.sp) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(rutinas, key = { it.id }) { rutina ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onElegir(rutina.id) }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(rutina.nombre, color = PuTextPrimary, fontSize = 15.sp)
                        Text(
                            "${rutina.totalEjercicios} ejercicios · ${rutina.duracionMin} min",
                            color = PuTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (tieneRutina) {
                TextButton(onClick = onDescanso) { Text("Dejar en descanso", color = PuRojo) }
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cancelar", color = PuAccent) }
        }
    )
}
