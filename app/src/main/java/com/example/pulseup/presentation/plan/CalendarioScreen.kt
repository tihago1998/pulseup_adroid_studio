package com.example.pulseup.presentation.plan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.ui.theme.PuAccent
import com.example.pulseup.ui.theme.PuAccentDark
import com.example.pulseup.ui.theme.PuBgCard
import com.example.pulseup.ui.theme.PuTextMuted
import com.example.pulseup.ui.theme.PuTextPrimary
import com.example.pulseup.domain.model.DiaCalendario
import com.example.pulseup.domain.model.EstadoDia
import com.example.pulseup.presentation.componentes.EstadoCarga
import com.example.pulseup.presentation.componentes.EstadoError
import com.example.pulseup.presentation.componentes.PantallaPu
import com.example.pulseup.presentation.componentes.textoBonito
import com.example.pulseup.ui.theme.PuBorder
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val NOMBRES_DIA = listOf("L", "M", "X", "J", "V", "S", "D")

@Composable
fun CalendarioScreen(
    viewModel: CalendarioViewModel,
    onVolver: () -> Unit,
    onPlanClick: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val calendario = estado.calendario

    PantallaPu(titulo = "Calendario", onVolver = onVolver) {
        when {
            estado.cargando && calendario == null -> EstadoCarga()
            estado.error != null && calendario == null -> EstadoError(estado.error!!, viewModel::cargar)
            calendario != null -> Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TarjetaRacha(calendario.racha.actual, calendario.racha.mejor)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = viewModel::mesAnterior) { Text("‹", color = PuAccent, fontSize = 22.sp) }
                    Text(
                        textoBonito(
                            estado.mes.month.getDisplayName(TextStyle.FULL, Locale("es", "CO"))
                        ) + " ${estado.mes.year}",
                        color = PuTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    TextButton(onClick = viewModel::mesSiguiente) { Text("›", color = PuAccent, fontSize = 22.sp) }
                }

                Cuadricula(
                    dias = calendario.dias,
                    seleccionado = estado.seleccionado?.fecha,
                    onDiaClick = viewModel::seleccionar
                )

                estado.seleccionado?.let { DetalleDia(it) }

                estado.aviso?.let { aviso ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PuBgCard),
                        border = BorderStroke(1.dp, if (estado.avisoEsError) PuRojo else PuAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = viewModel::cerrarAviso)
                    ) {
                        Text(aviso, color = PuTextPrimary, fontSize = 14.sp, modifier = Modifier.padding(14.dp))
                    }
                }

                BotonMarcarHoy(estado.puedeMarcarHoy, estado.completando, viewModel::marcarHoy)

                TextButton(onClick = onPlanClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Editar mi plan semanal", color = PuAccent)
                }
                Leyenda()
            }
            else -> Unit
        }
    }
}

@Composable
fun TarjetaRacha(actual: Int, mejor: Int) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PuBgCard),
        border = BorderStroke(0.5.dp, PuBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("🔥", fontSize = 28.sp)
            Column {
                Text("Racha: $actual", color = PuTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text("Mejor racha: $mejor", color = PuTextMuted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun Cuadricula(dias: List<DiaCalendario>, seleccionado: String?, onDiaClick: (DiaCalendario) -> Unit) {
    // Relleno inicial para que el día 1 caiga en su columna (lunes = primera columna)
    val relleno = ((dias.firstOrNull()?.diaSemana ?: 1) - 1).coerceIn(0, 6)
    val celdas: List<DiaCalendario?> = List(relleno) { null } + dias
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            NOMBRES_DIA.forEach {
                Text(
                    it,
                    color = PuTextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        celdas.chunked(7).forEach { semana ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                semana.forEach { dia ->
                    Box(modifier = Modifier.weight(1f)) {
                        if (dia != null) CeldaDia(dia, dia.fecha == seleccionado) { onDiaClick(dia) }
                    }
                }
                repeat(7 - semana.size) { Box(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun CeldaDia(dia: DiaCalendario, seleccionada: Boolean, onClick: () -> Unit) {
    val hoy = dia.fecha == LocalDate.now().toString()
    val forma = RoundedCornerShape(10.dp)
    val borde = when {
        seleccionada -> PuAccent
        hoy -> PuAccent.copy(alpha = 0.6f)
        else -> PuBorder
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clip(forma)
            .background(if (dia.estado == EstadoDia.CUMPLIDO) PuAccentDark else PuBgCard)
            .border(if (seleccionada || hoy) 1.5.dp else 0.5.dp, borde, forma)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(dia.fecha.takeLast(2).trimStart('0'), color = PuTextPrimary, fontSize = 13.sp)
        when (dia.estado) {
            EstadoDia.CUMPLIDO -> Text("🔥", fontSize = 16.sp)
            EstadoDia.INCUMPLIDO -> Text("✖", color = PuRojo, fontSize = 14.sp)
            EstadoDia.PENDIENTE -> Text("●", color = PuAccent, fontSize = 12.sp)
            EstadoDia.DESCANSO -> Text("·", color = PuTextMuted, fontSize = 14.sp)
        }
    }
}

@Composable
private fun DetalleDia(dia: DiaCalendario) {
    val texto = when (dia.estado) {
        EstadoDia.CUMPLIDO -> "Cumplido"
        EstadoDia.PENDIENTE -> "Pendiente"
        EstadoDia.INCUMPLIDO -> "No cumpliste esta rutina"
        EstadoDia.DESCANSO -> "Día de descanso"
    }
    Text(
        buildString {
            append(dia.fecha)
            dia.rutina?.let { append(" · ${it.nombre}") }
            append(" · $texto")
        },
        color = PuTextMuted,
        fontSize = 13.sp
    )
}

@Composable
fun BotonMarcarHoy(habilitado: Boolean, cargando: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = habilitado && !cargando,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PuAccent,
            contentColor = PuAccentDark,
            disabledContainerColor = PuBgCard,
            disabledContentColor = PuTextMuted
        ),
        modifier = Modifier.fillMaxWidth()
    ) { Text(if (cargando) "Marcando…" else "Marcar rutina de hoy 🔥") }
}

@Composable
private fun Leyenda() {
    Text(
        "🔥 cumplido   ● pendiente   ✖ incumplido   · descanso",
        color = PuTextMuted,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}
