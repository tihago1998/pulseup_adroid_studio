package com.example.pulseup.presentation.perfil

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.PuAccent
import com.example.pulseup.PuAccentDark
import com.example.pulseup.PuBgCard
import com.example.pulseup.PuTextMuted
import com.example.pulseup.PuTextPrimary
import com.example.pulseup.domain.model.Catalogos
import com.example.pulseup.domain.model.ValoresPerfilFisico
import com.example.pulseup.presentation.componentes.ChipPu
import com.example.pulseup.presentation.componentes.EstadoCarga
import com.example.pulseup.presentation.componentes.EstadoError
import com.example.pulseup.presentation.componentes.PantallaPu
import com.example.pulseup.presentation.componentes.coloresCampoPu
import com.example.pulseup.presentation.componentes.textoBonito
import com.example.pulseup.presentation.plan.PuRojo
import com.example.pulseup.ui.theme.PuBorder
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private val TEXTO_SEXO = mapOf(
    "hombre" to "Hombre",
    "mujer" to "Mujer",
    "no_indica" to "Prefiero no decirlo"
)

private val AYUDA_NIVEL = mapOf(
    "principiante" to "menos de 1 año entrenando",
    "intermedio" to "de 1 a 3 años",
    "avanzado" to "más de 3 años"
)

@Composable
fun PerfilFisicoScreen(
    viewModel: PerfilFisicoViewModel,
    onVolver: () -> Unit,
    onGuardado: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val contexto = LocalContext.current
    var eligiendoFecha by remember { mutableStateOf(false) }
    var confirmandoBorrado by remember { mutableStateOf(false) }

    LaunchedEffect(estado.guardado) {
        if (estado.guardado) {
            Toast.makeText(contexto, "Perfil guardado", Toast.LENGTH_SHORT).show()
            onGuardado()
        }
    }

    PantallaPu(titulo = "Mi perfil físico", onVolver = onVolver) {
        when {
            estado.cargando -> EstadoCarga()
            estado.errorCarga != null -> EstadoError(estado.errorCarga!!, viewModel::cargar)
            else -> Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Usamos estos datos solo para recomendarte repeticiones y pesos según tu nivel. " +
                        "Puedes editarlos o borrarlos cuando quieras.",
                    color = PuTextMuted,
                    fontSize = 13.sp
                )

                estado.aviso?.let { aviso ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PuBgCard),
                        border = BorderStroke(1.dp, PuAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = viewModel::cerrarAviso)
                    ) {
                        Text(aviso, color = PuTextPrimary, fontSize = 14.sp, modifier = Modifier.padding(14.dp))
                    }
                }

                // ---------- Fecha de nacimiento ----------
                CampoFecha(
                    fecha = estado.fechaNacimiento,
                    error = estado.errores.fecha,
                    onClick = { eligiendoFecha = true }
                )

                // ---------- Sexo (opcional) ----------
                Text("Sexo (opcional)", color = PuTextMuted, fontSize = 12.sp)
                ChipsSexo(estado.sexo, viewModel::cambiarSexo)

                // ---------- Peso y estatura ----------
                CampoDecimal(
                    etiqueta = "Peso (kg)",
                    valor = estado.peso,
                    error = estado.errores.peso,
                    onCambio = viewModel::cambiarPeso
                )
                CampoDecimal(
                    etiqueta = "Estatura (cm)",
                    valor = estado.estatura,
                    error = estado.errores.estatura,
                    onCambio = viewModel::cambiarEstatura
                )

                // ---------- Nivel ----------
                Text("Nivel", color = PuTextMuted, fontSize = 12.sp)
                ChipsNivel(estado.nivel, viewModel::cambiarNivel)
                estado.errores.nivel?.let { MensajeError(it) }

                estado.errorServidor?.let { MensajeError(it) }

                Button(
                    onClick = viewModel::guardar,
                    enabled = !estado.guardando && !estado.borrando,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PuAccent, contentColor = PuAccentDark),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (estado.guardando) "Guardando..." else "Guardar") }

                if (estado.tienePerfil) {
                    OutlinedButton(
                        onClick = { confirmandoBorrado = true },
                        enabled = !estado.guardando && !estado.borrando,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, PuRojo),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (estado.borrando) "Borrando..." else "Borrar mis datos", color = PuRojo) }
                }
            }
        }
    }

    if (eligiendoFecha) {
        SelectorFecha(
            inicial = estado.fechaNacimiento,
            onElegir = viewModel::cambiarFecha,
            onCerrar = { eligiendoFecha = false }
        )
    }

    if (confirmandoBorrado) {
        AlertDialog(
            onDismissRequest = { confirmandoBorrado = false },
            containerColor = PuBgCard,
            title = { Text("¿Borrar tus datos físicos?", color = PuTextPrimary, fontSize = 18.sp) },
            text = {
                Text(
                    "Se eliminarán tu fecha de nacimiento, sexo, peso, estatura y nivel. " +
                        "Puedes volver a llenarlos cuando quieras.",
                    color = PuTextMuted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmandoBorrado = false
                    viewModel.borrar()
                }) { Text("Borrar", color = PuRojo) }
            },
            dismissButton = {
                TextButton(onClick = { confirmandoBorrado = false }) { Text("Cancelar", color = PuAccent) }
            }
        )
    }
}

@Composable
private fun MensajeError(texto: String) {
    Text(texto, color = PuRojo, fontSize = 12.sp)
}

/** Se ve como un campo de texto, pero al tocarlo abre el calendario. */
@Composable
private fun CampoFecha(fecha: LocalDate?, error: String?, onClick: () -> Unit) {
    val forma = RoundedCornerShape(12.dp)
    val hoy = remember { LocalDate.now() }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (error != null) PuRojo else PuBorder, forma)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text("Fecha de nacimiento", color = PuTextMuted, fontSize = 12.sp)
            Text(
                fecha?.format(FORMATO_FECHA) ?: "Toca para elegir",
                color = if (fecha != null) PuTextPrimary else PuTextMuted,
                fontSize = 16.sp
            )
        }
        when {
            error != null -> MensajeError(error)
            fecha != null && !fecha.isAfter(hoy) ->
                Text("${calcularEdad(fecha, hoy)} años", color = PuTextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun CampoDecimal(etiqueta: String, valor: String, error: String?, onCambio: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it, color = PuRojo) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(12.dp),
        colors = coloresCampoPu(),
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipsSexo(seleccionado: String, onElegir: (String) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ValoresPerfilFisico.SEXOS.forEach { sexo ->
            ChipPu(TEXTO_SEXO[sexo] ?: textoBonito(sexo), sexo == seleccionado) { onElegir(sexo) }
        }
    }
}

@Composable
private fun ChipsNivel(seleccionado: String?, onElegir: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Catalogos.NIVELES.forEach { nivel ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ChipPu(textoBonito(nivel), nivel == seleccionado) { onElegir(nivel) }
                Text(
                    AYUDA_NIVEL[nivel].orEmpty(),
                    color = PuTextMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = if (nivel == seleccionado) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}

// El DatePicker trabaja con milisegundos en UTC
private fun LocalDate.aMillisUtc(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
private fun Long.aFechaUtc(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorFecha(
    inicial: LocalDate?,
    onElegir: (LocalDate) -> Unit,
    onCerrar: () -> Unit
) {
    val hoy = remember { LocalDate.now() }
    val hoyMillis = remember(hoy) { hoy.aMillisUtc() }
    val estadoFecha = rememberDatePickerState(
        initialSelectedDateMillis = inicial?.aMillisUtc(),
        // Sin fecha, el calendario abre en una edad razonable para no recorrer años
        initialDisplayedMonthMillis = (inicial ?: hoy.minusYears(25)).aMillisUtc(),
        yearRange = (hoy.year - ValoresPerfilFisico.EDAD_MAXIMA - 1)..hoy.year,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= hoyMillis
            override fun isSelectableYear(year: Int) = year <= hoy.year
        }
    )
    DatePickerDialog(
        onDismissRequest = onCerrar,
        confirmButton = {
            TextButton(
                onClick = {
                    estadoFecha.selectedDateMillis?.let { onElegir(it.aFechaUtc()) }
                    onCerrar()
                },
                enabled = estadoFecha.selectedDateMillis != null
            ) { Text("Aceptar", color = PuAccent) }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cancelar", color = PuAccent) }
        }
    ) {
        DatePicker(state = estadoFecha)
    }
}
