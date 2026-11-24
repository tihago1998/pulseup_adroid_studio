package com.example.pulseup.presentation.plan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.domain.model.EjercicioPlan
import com.example.pulseup.domain.model.MAX_EJERCICIOS_POR_DIA
import com.example.pulseup.presentation.componentes.EstadoCarga
import com.example.pulseup.presentation.componentes.EstadoError
import com.example.pulseup.presentation.componentes.EtiquetaPu
import com.example.pulseup.presentation.componentes.ImagenRemota
import com.example.pulseup.presentation.componentes.PantallaPu
import com.example.pulseup.presentation.componentes.coloresCampoPu
import com.example.pulseup.presentation.componentes.textoBonito
import com.example.pulseup.presentation.ejercicios.textoDescansoPlan
import com.example.pulseup.presentation.ejercicios.textoSeriesPlan
import com.example.pulseup.presentation.ejercicios.textosRecomendacion
import com.example.pulseup.ui.theme.PuAccent
import com.example.pulseup.ui.theme.PuAccentDark
import com.example.pulseup.ui.theme.PuAccentMid
import com.example.pulseup.ui.theme.PuBgCard
import com.example.pulseup.ui.theme.PuBorder
import com.example.pulseup.ui.theme.PuRojo
import com.example.pulseup.ui.theme.PuTextMuted
import com.example.pulseup.ui.theme.PuTextPrimary
import com.example.pulseup.ui.theme.PuTextSecondary

@Composable
fun PlanDiaScreen(
    viewModel: PlanDiaViewModel,
    onVolver: () -> Unit,
    onAgregarEjercicio: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()

    PantallaPu(titulo = "Plan del ${estado.nombreDia}", onVolver = onVolver) {
        when {
            estado.cargando && estado.dia == null -> EstadoCarga()
            estado.error != null -> EstadoError(estado.error!!, viewModel::cargar)
            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Encabezado(estado) }
                items(estado.notas) { nota -> TarjetaNota(nota, onCerrar = { viewModel.cerrarNota(nota) }) }
                items(estado.ejercicios, key = { it.itemId }) { e ->
                    TarjetaEjercicioPlan(
                        e = e,
                        habilitada = !estado.ocupado,
                        onEditar = { viewModel.abrirEdicion(e) },
                        onQuitar = { viewModel.pedirQuitar(e) }
                    )
                }
                item {
                    Button(
                        onClick = onAgregarEjercicio,
                        enabled = !estado.ocupado && !estado.lleno,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PuAccent, contentColor = PuAccentDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) { Text(if (estado.ocupado) "Guardando..." else "Agregar ejercicio") }
                    if (estado.lleno) {
                        Text(
                            "Máximo $MAX_EJERCICIOS_POR_DIA ejercicios por día.",
                            color = PuTextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }
    }

    estado.edicion?.let { edicion ->
        DialogoEdicion(
            e = edicion,
            onSeries = viewModel::cambiarSeries,
            onRepeticiones = viewModel::cambiarRepeticiones,
            onPeso = viewModel::cambiarPeso,
            onDescanso = viewModel::cambiarDescanso,
            onGuardar = viewModel::guardarEdicion,
            onCerrar = viewModel::cerrarEdicion
        )
    }

    estado.porQuitar?.let { item ->
        AlertDialog(
            onDismissRequest = viewModel::cancelarQuitar,
            containerColor = PuBgCard,
            title = { Text("¿Quitar ${item.nombre}?", color = PuTextPrimary, fontSize = 18.sp) },
            text = {
                Text(
                    "Se quita del plan del ${estado.nombreDia}. Si era el último ejercicio, el día queda en descanso.",
                    color = PuTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmarQuitar, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Quitar", color = PuRojo)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelarQuitar, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Cancelar", color = PuAccent)
                }
            }
        )
    }
}

@Composable
private fun Encabezado(estado: PlanDiaUiState) {
    val rutina = estado.dia?.rutina
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (rutina == null) {
            Text(
                "Día de descanso. Agrega un ejercicio para empezar a entrenar este día.",
                color = PuTextMuted,
                fontSize = 14.sp
            )
        } else {
            Text(rutina.nombre, color = PuTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rutina.gruposMusculares.forEach { EtiquetaPu(textoBonito(it)) }
            }
        }
    }
}

@Composable
fun TarjetaEjercicioPlan(
    e: EjercicioPlan,
    habilitada: Boolean,
    onEditar: () -> Unit,
    onQuitar: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PuBgCard),
        border = BorderStroke(0.5.dp, PuBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, top = 10.dp, bottom = 10.dp, end = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ImagenRemota(
                url = e.imagenMiniatura,
                descripcion = e.nombre,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(e.nombre, color = PuTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(textoSeriesPlan(e.series, e.repeticiones, e.unidad, e.pesoKg), color = PuAccentMid, fontSize = 14.sp)
                textoDescansoPlan(e.descansoSeg)?.let { Text(it, color = PuTextMuted, fontSize = 12.sp) }
            }
            IconButton(onClick = onEditar, enabled = habilitada) {
                Icon(Icons.Default.Edit, contentDescription = "Editar ${e.nombre}", tint = PuTextSecondary)
            }
            IconButton(onClick = onQuitar, enabled = habilitada) {
                Icon(Icons.Default.Delete, contentDescription = "Quitar ${e.nombre}", tint = PuTextMuted)
            }
        }
    }
}

@Composable
private fun DialogoEdicion(
    e: EdicionEjercicio,
    onSeries: (String) -> Unit,
    onRepeticiones: (String) -> Unit,
    onPeso: (String) -> Unit,
    onDescanso: (String) -> Unit,
    onGuardar: () -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = PuBgCard,
        title = { Text(e.item.nombre, color = PuTextPrimary, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GuiaRecomendacion(e)
                if (!e.esCardio) Campo("Series", e.series, onSeries, numerico = true)
                Campo(etiquetaRepeticiones(e.item.unidad), e.repeticiones, onRepeticiones, numerico = e.item.unidad != "repeticiones")
                if (!e.esCardio) {
                    Campo("Peso (kg, opcional)", e.peso, onPeso, numerico = true, decimal = true)
                    Campo("Descanso (segundos)", e.descanso, onDescanso, numerico = true)
                }
                e.error?.let { Text(it, color = PuRojo, fontSize = 13.sp) }
            }
        },
        confirmButton = {
            TextButton(onClick = onGuardar, enabled = !e.guardando, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(if (e.guardando) "Guardando..." else "Guardar", color = PuAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar, enabled = !e.guardando, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Cancelar", color = PuTextMuted)
            }
        }
    )
}

/** La recomendación es solo una guía: el usuario puede poner otros valores. */
@Composable
private fun GuiaRecomendacion(e: EdicionEjercicio) {
    val r = e.recomendacion
    when {
        e.cargandoRecomendacion -> Text("Buscando tu recomendación...", color = PuTextMuted, fontSize = 13.sp)
        r == null -> Text(
            "Este ejercicio no tiene recomendación para ti; elige los valores que prefieras.",
            color = PuTextMuted,
            fontSize = 13.sp
        )
        else -> {
            val t = textosRecomendacion(r)
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = PuAccentDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Guía · ${t.titulo}", color = PuAccentMid, fontSize = 12.sp)
                    Text(t.principal, color = PuTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    t.peso?.let { Text("Peso sugerido: $it", color = PuTextSecondary, fontSize = 13.sp) }
                    t.descanso?.let { Text(it, color = PuTextSecondary, fontSize = 13.sp) }
                    Text("Es una guía: puedes cambiar los valores.", color = PuAccentMid, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun Campo(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    numerico: Boolean,
    decimal: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = when {
                decimal -> KeyboardType.Decimal
                numerico -> KeyboardType.Number
                else -> KeyboardType.Text
            }
        ),
        shape = RoundedCornerShape(10.dp),
        colors = coloresCampoPu(),
        modifier = Modifier.fillMaxWidth()
    )
}
