package com.example.pulseup.presentation.plan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.domain.model.AvisoDescanso
import com.example.pulseup.domain.model.DiaPrevisualizado
import com.example.pulseup.presentation.componentes.EstadoCarga
import com.example.pulseup.presentation.componentes.EstadoError
import com.example.pulseup.presentation.componentes.textoBonito
import com.example.pulseup.ui.theme.PuAccent
import com.example.pulseup.ui.theme.PuAccentDark
import com.example.pulseup.ui.theme.PuAccentMid
import com.example.pulseup.ui.theme.PuAdvertencia
import com.example.pulseup.ui.theme.PuBgCard
import com.example.pulseup.ui.theme.PuBgSurface
import com.example.pulseup.ui.theme.PuBorder
import com.example.pulseup.ui.theme.PuRojo
import com.example.pulseup.ui.theme.PuTextMuted
import com.example.pulseup.ui.theme.PuTextPrimary
import com.example.pulseup.ui.theme.PuTextSecondary

/**
 * Hoja inferior para elegir días del plan, con los avisos de 48 h de cada día.
 * Con [multiple] = false, tocar un día lo elige; con true, cada día se marca o desmarca
 * y la acción va en [acciones]. La usan "Agregar a mi plan" (un día) y "Crear rutina" (varios).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaDias(
    titulo: String,
    subtitulo: String?,
    cargando: Boolean,
    error: String?,
    dias: List<DiaPrevisualizado>,
    multiple: Boolean,
    seleccionados: Set<Int>,
    /** Texto bajo el nombre del día cuando ya tiene rutina (por ejemplo, "se agrega a su rutina"). */
    textoConRutina: String,
    onDia: (DiaPrevisualizado) -> Unit,
    onReintentar: () -> Unit,
    onCerrar: () -> Unit,
    /** Mensaje bajo la lista (por ejemplo, el 409); en rojo. */
    mensaje: String? = null,
    deshabilitada: Boolean = false,
    acciones: @Composable ColumnScope.() -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = PuBgSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(titulo, color = PuTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Medium)
            subtitulo?.let { Text(it, color = PuTextMuted, fontSize = 13.sp) }
            when {
                cargando -> EstadoCarga()
                error != null -> EstadoError(error, onReintentar)
                else -> dias.forEach { dia ->
                    FilaDia(
                        dia = dia,
                        seleccionado = dia.dia in seleccionados,
                        multiple = multiple,
                        textoConRutina = textoConRutina,
                        habilitada = !deshabilitada,
                        onClick = { onDia(dia) }
                    )
                }
            }
            mensaje?.let { Text(it, color = PuRojo, fontSize = 14.sp) }
            acciones()
        }
    }
}

@Composable
private fun FilaDia(
    dia: DiaPrevisualizado,
    seleccionado: Boolean,
    multiple: Boolean,
    textoConRutina: String,
    habilitada: Boolean,
    onClick: () -> Unit
) {
    val conAviso = dia.avisos.isNotEmpty()
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (seleccionado) PuAccentDark else PuBgCard),
        border = BorderStroke(
            if (seleccionado) 1.dp else 0.5.dp,
            when {
                seleccionado -> PuAccent
                conAviso -> PuAdvertencia
                else -> PuBorder
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(enabled = habilitada, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(textoBonito(dia.nombreDia), color = PuTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(
                    if (dia.tieneRutina) textoConRutina else "Descanso",
                    color = if (dia.tieneRutina) PuAccentMid else PuTextMuted,
                    fontSize = 13.sp
                )
                dia.avisos.map { it.mensaje }.distinct().forEach { Text("⚠ $it", color = PuAdvertencia, fontSize = 13.sp) }
            }
            if (multiple) {
                Checkbox(
                    checked = seleccionado,
                    onCheckedChange = { onClick() },
                    enabled = habilitada,
                    colors = CheckboxDefaults.colors(checkedColor = PuAccent, uncheckedColor = PuTextMuted)
                )
            }
        }
    }
}

/** Confirmación cuando hay avisos de 48 h: el usuario siempre puede seguir con [confirmar]. */
@Composable
fun DialogoAvisos(
    titulo: String,
    avisos: List<AvisoDescanso>,
    confirmar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = PuBgCard,
        title = { Text(titulo, color = PuTextPrimary, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                avisos.map { it.mensaje }.distinct().forEach { Text("⚠ $it", color = PuAdvertencia, fontSize = 14.sp) }
                Text("Es solo un aviso: puedes continuar si lo prefieres.", color = PuTextSecondary, fontSize = 13.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirmar, modifier = Modifier.heightIn(min = 48.dp)) { Text(confirmar, color = PuAccent) }
        },
        dismissButton = {
            TextButton(onClick = onCancelar, modifier = Modifier.heightIn(min = 48.dp)) { Text("Cancelar", color = PuTextMuted) }
        }
    )
}

/** Asignar una rutina reemplaza lo que ya tiene el día: se confirma con "Reemplazar". */
@Composable
fun DialogoReemplazo(
    texto: String,
    onReemplazar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = PuBgCard,
        title = { Text("¿Cambiar la rutina?", color = PuTextPrimary, fontSize = 18.sp) },
        text = { Text(texto, color = PuTextSecondary, fontSize = 15.sp) },
        confirmButton = {
            TextButton(onClick = onReemplazar, modifier = Modifier.heightIn(min = 48.dp)) { Text("Reemplazar", color = PuRojo) }
        },
        dismissButton = {
            TextButton(onClick = onCancelar, modifier = Modifier.heightIn(min = 48.dp)) { Text("Cancelar", color = PuAccent) }
        }
    )
}

/** Tarjeta de un mensaje del plan (guardado, aviso de 48 h, copia de rutina o error). Tocarla la cierra. */
@Composable
fun TarjetaNota(nota: Nota, onCerrar: (() -> Unit)? = null) {
    val color = when (nota.tipo) {
        TipoNota.EXITO -> PuAccent
        TipoNota.INFO -> PuAccentMid
        TipoNota.ADVERTENCIA -> PuAdvertencia
        TipoNota.ERROR -> PuRojo
    }
    val prefijo = if (nota.tipo == TipoNota.ADVERTENCIA) "⚠ " else ""
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PuBgCard),
        border = BorderStroke(1.dp, color),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onCerrar != null) Modifier.clickable(onClick = onCerrar) else Modifier)
    ) {
        Text(
            prefijo + nota.texto,
            color = if (nota.tipo == TipoNota.ADVERTENCIA) PuAdvertencia else PuTextPrimary,
            fontSize = 14.sp,
            modifier = Modifier.padding(14.dp)
        )
    }
}
