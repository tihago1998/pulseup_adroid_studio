package com.example.pulseup.presentation.rutinas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.ui.theme.PuAccent
import com.example.pulseup.ui.theme.PuAccentDark
import com.example.pulseup.ui.theme.PuBgCard
import com.example.pulseup.ui.theme.PuTextMuted
import com.example.pulseup.ui.theme.PuTextPrimary
import com.example.pulseup.domain.model.Catalogos
import com.example.pulseup.domain.model.Ejercicio
import com.example.pulseup.presentation.componentes.ChipPu
import com.example.pulseup.presentation.componentes.PantallaPu
import com.example.pulseup.presentation.componentes.coloresCampoPu
import com.example.pulseup.presentation.componentes.textoBonito
import com.example.pulseup.ui.theme.PuBorder

@Composable
fun CrearRutinaScreen(
    viewModel: CrearRutinaViewModel,
    onVolver: () -> Unit,
    onCreada: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    var eligiendo by remember { mutableStateOf(false) }

    LaunchedEffect(estado.creada) {
        if (estado.creada) onCreada()
    }

    PantallaPu(titulo = "Nueva rutina", onVolver = onVolver) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = estado.nombre,
                onValueChange = viewModel::cambiarNombre,
                label = { Text("Nombre") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = coloresCampoPu(),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.descripcion,
                onValueChange = viewModel::cambiarDescripcion,
                label = { Text("Descripción (opcional)") },
                shape = RoundedCornerShape(12.dp),
                colors = coloresCampoPu(),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Objetivo", color = PuTextMuted, fontSize = 12.sp)
            FlujoChips(Catalogos.OBJETIVOS, estado.objetivo, viewModel::cambiarObjetivo)

            Text("Nivel", color = PuTextMuted, fontSize = 12.sp)
            FlujoChips(Catalogos.NIVELES, estado.nivel, viewModel::cambiarNivel)

            OutlinedTextField(
                value = estado.duracion,
                onValueChange = viewModel::cambiarDuracion,
                label = { Text("Duración (minutos)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = coloresCampoPu(),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Ejercicios", color = PuTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            estado.items.forEachIndexed { indice, item ->
                TarjetaItem(
                    item = item,
                    onSeries = { viewModel.cambiarSeries(indice, it) },
                    onRepeticiones = { viewModel.cambiarRepeticiones(indice, it) },
                    onDescanso = { viewModel.cambiarDescanso(indice, it) },
                    onQuitar = { viewModel.quitar(indice) }
                )
            }

            OutlinedButton(
                onClick = { eligiendo = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Agregar ejercicio", color = PuAccent) }

            estado.error?.let { Text(it, color = Color(0xFFE57373), fontSize = 13.sp) }

            Button(
                onClick = viewModel::guardar,
                enabled = !estado.guardando,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PuAccent, contentColor = PuAccentDark),
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (estado.guardando) "Guardando..." else "Guardar rutina") }
        }
    }

    if (eligiendo) {
        SelectorEjercicio(
            catalogo = estado.catalogo,
            onElegir = {
                viewModel.agregar(it)
                eligiendo = false
            },
            onCerrar = { eligiendo = false }
        )
    }
}

// Los chips se reparten en varias filas si no caben (los objetivos son pocos)
@Composable
private fun FlujoChips(opciones: List<String>, seleccionada: String, onElegir: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        opciones.chunked(3).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                fila.forEach { opcion ->
                    ChipPu(textoBonito(opcion), opcion == seleccionada) { onElegir(opcion) }
                }
            }
        }
    }
}

@Composable
private fun TarjetaItem(
    item: ItemEnEdicion,
    onSeries: (String) -> Unit,
    onRepeticiones: (String) -> Unit,
    onDescanso: (String) -> Unit,
    onQuitar: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PuBgCard),
        border = BorderStroke(0.5.dp, PuBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.ejercicio.nombre,
                    color = PuTextPrimary,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onQuitar) {
                    Icon(Icons.Default.Delete, contentDescription = "Quitar ejercicio", tint = PuTextMuted)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoNumero("Series", item.series, onSeries, Modifier.weight(1f), numerico = true)
                CampoNumero("Reps", item.repeticiones, onRepeticiones, Modifier.weight(1f), numerico = false)
                CampoNumero("Descanso s", item.descanso, onDescanso, Modifier.weight(1f), numerico = true)
            }
        }
    }
}

@Composable
private fun CampoNumero(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    modifier: Modifier,
    numerico: Boolean
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta, maxLines = 1) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (numerico) KeyboardType.Number else KeyboardType.Text),
        shape = RoundedCornerShape(10.dp),
        colors = coloresCampoPu(),
        modifier = modifier
    )
}

@Composable
private fun SelectorEjercicio(
    catalogo: List<Ejercicio>,
    onElegir: (Ejercicio) -> Unit,
    onCerrar: () -> Unit
) {
    var texto by remember { mutableStateOf("") }
    val visibles = catalogo.filter { it.nombre.contains(texto.trim(), ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Elegir ejercicio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    placeholder = { Text("Buscar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    items(visibles, key = { it.id }) { ejercicio ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onElegir(ejercicio) }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(ejercicio.nombre, fontSize = 15.sp)
                            Text(
                                "${textoBonito(ejercicio.grupoMuscular)} · ${textoBonito(ejercicio.equipo)}",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onCerrar) { Text("Cerrar") } }
    )
}
