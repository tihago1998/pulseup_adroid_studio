package com.example.pulseup.presentation.ejercicios

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.domain.model.Catalogos
import com.example.pulseup.domain.model.Ejercicio
import com.example.pulseup.presentation.componentes.ChipPu
import com.example.pulseup.presentation.componentes.EstadoCarga
import com.example.pulseup.presentation.componentes.EstadoError
import com.example.pulseup.presentation.componentes.EstadoVacio
import com.example.pulseup.presentation.componentes.EtiquetaPu
import com.example.pulseup.presentation.componentes.ImagenRemota
import com.example.pulseup.presentation.componentes.PantallaPu
import com.example.pulseup.presentation.componentes.coloresCampoPu
import com.example.pulseup.presentation.componentes.textoBonito
import com.example.pulseup.ui.theme.PuAccent
import com.example.pulseup.ui.theme.PuBgCard
import com.example.pulseup.ui.theme.PuBorder
import com.example.pulseup.ui.theme.PuTextMuted
import com.example.pulseup.ui.theme.PuTextPrimary
import kotlinx.coroutines.flow.filter

/**
 * Catálogo de ejercicios: búsqueda, filtros, fotos y paginación al llegar al final.
 * También sirve para elegir un ejercicio para un día del plan: entonces cambia [titulo] y
 * [onEjercicioClick] devuelve el elegido en vez de abrir su detalle.
 */
@Composable
fun EjerciciosScreen(
    viewModel: EjerciciosViewModel,
    onVolver: () -> Unit,
    onEjercicioClick: (Int) -> Unit,
    onCreditosClick: () -> Unit,
    titulo: String = "Ejercicios"
) {
    val estado by viewModel.estado.collectAsState()
    val opciones = estado.opciones
    val lista = rememberLazyListState()

    // Al acercarse al final se pide la página siguiente. Se vuelve a vigilar cada vez que crece la lista.
    val cercaDelFinal by remember {
        derivedStateOf {
            val info = lista.layoutInfo
            val ultimo = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && ultimo >= info.totalItemsCount - 4
        }
    }
    LaunchedEffect(lista, estado.ejercicios.size) {
        snapshotFlow { cercaDelFinal }.filter { it }.collect { viewModel.cargarMas() }
    }

    PantallaPu(titulo = titulo, onVolver = onVolver) {
        LazyColumn(
            state = lista,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                OutlinedTextField(
                    value = estado.filtro.texto,
                    onValueChange = viewModel::cambiarTexto,
                    placeholder = { Text("Buscar ejercicio") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PuTextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = coloresCampoPu(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
            }
            item {
                FilaChips("Dónde entrenas", opciones?.ambientes?.ifEmpty { null } ?: Catalogos.AMBIENTES,
                    estado.filtro.ambiente, viewModel::elegirAmbiente)
            }
            if (opciones != null) {
                item { FilaChips("Músculo", opciones.musculos, estado.filtro.musculo, viewModel::elegirMusculo) }
                item { FilaChips("Equipo", opciones.equipos, estado.filtro.equipo, viewModel::elegirEquipo) }
            }
            item {
                FilaChips("Nivel", opciones?.niveles?.ifEmpty { null } ?: Catalogos.NIVELES,
                    estado.filtro.nivel, viewModel::elegirNivel)
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (estado.cargando) "Buscando…" else "${estado.total} ejercicios",
                        color = PuTextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onCreditosClick, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("Créditos de las fotos", color = PuAccent, fontSize = 13.sp)
                    }
                }
            }

            when {
                estado.cargando -> item { EstadoCarga() }
                estado.error != null -> item { EstadoError(estado.error!!, viewModel::reintentar) }
                estado.vacio -> item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        EstadoVacio("No hay ejercicios con estos filtros")
                        if (estado.conFiltros) {
                            OutlinedButton(
                                onClick = viewModel::quitarFiltros,
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) { Text("Quitar filtros", color = PuAccent) }
                        }
                    }
                }
                else -> {
                    items(estado.ejercicios, key = { it.id }) { ejercicio ->
                        TarjetaEjercicio(ejercicio, onClick = { onEjercicioClick(ejercicio.id) })
                    }
                    item { PieDeLista(estado, viewModel::reintentarMas) }
                }
            }
        }
    }
}

/** Fila con título y chips desplazables; tocar el elegido otra vez lo quita. Cada chip mide al menos 48 dp. */
@Composable
private fun FilaChips(
    titulo: String,
    opciones: List<String>,
    seleccionada: String?,
    onElegir: (String?) -> Unit
) {
    Column {
        Text(
            titulo,
            color = PuTextMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(opciones) { opcion ->
                ChipPu(
                    texto = textoBonito(opcion),
                    seleccionado = opcion == seleccionada,
                    modifier = Modifier.heightIn(min = 48.dp),
                    onClick = { onElegir(if (opcion == seleccionada) null else opcion) }
                )
            }
        }
    }
}

@Composable
private fun TarjetaEjercicio(ejercicio: Ejercicio, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PuBgCard),
        border = BorderStroke(0.5.dp, PuBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ImagenRemota(
                url = ejercicio.imagenMiniatura,
                descripcion = ejercicio.nombre,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(ejercicio.nombre, color = PuTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(
                    "${textoBonito(ejercicio.musculoPrincipal)} · ${textoBonito(ejercicio.equipo)}",
                    color = PuTextMuted,
                    fontSize = 13.sp
                )
                if (ejercicio.nivel.isNotBlank()) EtiquetaPu(textoBonito(ejercicio.nivel))
            }
        }
    }
}

/** Final de la lista: cargando más, error al paginar con Reintentar, o nada. */
@Composable
private fun PieDeLista(estado: CatalogoUiState, onReintentar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            estado.cargandoMas -> CircularProgressIndicator(color = PuAccent, modifier = Modifier.size(28.dp))
            estado.errorMas != null -> {
                Text(estado.errorMas, color = PuTextMuted, fontSize = 13.sp)
                TextButton(onClick = onReintentar, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Reintentar", color = PuAccent)
                }
            }
            !estado.hayMas && estado.ejercicios.isNotEmpty() ->
                Text("No hay más ejercicios", color = PuTextMuted, fontSize = 12.sp)
        }
    }
}
