package com.example.pulseup.presentation.ejercicios

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.domain.model.EjercicioDetalle
import com.example.pulseup.domain.model.ImagenEjercicio
import com.example.pulseup.domain.model.RecomendacionEjercicio
import com.example.pulseup.presentation.componentes.EstadoCarga
import com.example.pulseup.presentation.componentes.EstadoError
import com.example.pulseup.presentation.componentes.EtiquetaPu
import com.example.pulseup.presentation.componentes.ImagenRemota
import com.example.pulseup.presentation.componentes.PantallaPu
import com.example.pulseup.presentation.componentes.textoBonito
import com.example.pulseup.ui.theme.PuAccent
import com.example.pulseup.ui.theme.PuAccentDark
import com.example.pulseup.ui.theme.PuAccentMid
import com.example.pulseup.ui.theme.PuBgCard
import com.example.pulseup.ui.theme.PuBorder
import com.example.pulseup.ui.theme.PuTextMuted
import com.example.pulseup.ui.theme.PuTextPrimary
import com.example.pulseup.ui.theme.PuTextSecondary

@Composable
fun EjercicioDetalleScreen(
    viewModel: EjercicioDetalleViewModel,
    onVolver: () -> Unit,
    onPerfilFisicoClick: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val ejercicio = estado.ejercicio

    PantallaPu(titulo = "Ejercicio", onVolver = onVolver) {
        when {
            estado.cargando && ejercicio == null -> EstadoCarga()
            estado.error != null -> EstadoError(estado.error!!, viewModel::cargar)
            ejercicio != null -> ContenidoDetalle(ejercicio, onPerfilFisicoClick, Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContenidoDetalle(e: EjercicioDetalle, onPerfilFisicoClick: () -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GaleriaFotos(e.imagenes, e.nombre)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(e.nombre, color = PuTextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Medium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (e.musculoPrincipal.isNotBlank()) EtiquetaPu(textoBonito(e.musculoPrincipal))
                e.musculosSecundarios.forEach { EtiquetaPu(textoBonito(it)) }
                if (e.equipo.isNotBlank()) EtiquetaPu(textoBonito(e.equipo))
                if (e.nivel.isNotBlank()) EtiquetaPu(textoBonito(e.nivel))
            }
        }

        val recomendacion = e.recomendacion
        if (recomendacion != null) {
            TarjetaRecomendacion(recomendacion)
        } else {
            SinRecomendacion(e.recomendacionMotivo, e.descansoSeg, onPerfilFisicoClick)
        }

        ComoSeHace(e)

        // Fase 2: no existe todavía un flujo para agregar un ejercicio a una rutina desde aquí
        Button(
            onClick = {},
            enabled = false,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) { Text("Agregar a una rutina") }
        Text(
            "Muy pronto podrás agregar este ejercicio a tus rutinas desde aquí.",
            color = PuTextMuted,
            fontSize = 12.sp
        )
    }
}

/** Fotos de inicio y final: se pasan deslizando, con un indicador debajo. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GaleriaFotos(imagenes: List<ImagenEjercicio>, nombre: String) {
    val forma = RoundedCornerShape(16.dp)
    if (imagenes.isEmpty()) {
        ImagenRemota(
            url = null,
            descripcion = nombre,
            conTexto = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(forma)
        )
        return
    }
    val pager = rememberPagerState(pageCount = { imagenes.size })
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalPager(
            state = pager,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(forma)
        ) { pagina ->
            val imagen = imagenes[pagina]
            ImagenRemota(
                url = imagen.url,
                descripcion = "$nombre, ${etiquetaFoto(imagen.orden)}",
                conTexto = true,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (imagenes.size > 1) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                imagenes.indices.forEach { i ->
                    Box(
                        Modifier
                            .size(if (i == pager.currentPage) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (i == pager.currentPage) PuAccent else PuBorder)
                    )
                }
            }
        }
        Text(etiquetaFoto(imagenes[pager.currentPage].orden), color = PuTextMuted, fontSize = 12.sp)
    }
}

private fun etiquetaFoto(orden: Int): String = when (orden) {
    0 -> "Posición inicial"
    1 -> "Posición final"
    else -> "Foto ${orden + 1}"
}

@Composable
private fun TarjetaRecomendacion(r: RecomendacionEjercicio) {
    val t = textosRecomendacion(r)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PuAccentDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(t.titulo, color = PuAccentMid, fontSize = 13.sp)
            Text(t.principal, color = PuTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Medium)
            t.peso?.let { Text("Peso sugerido: $it", color = PuTextPrimary, fontSize = 15.sp) }
            t.descanso?.let { Text(it, color = PuTextSecondary, fontSize = 14.sp) }
            t.reserva?.let { Text(it, color = PuTextSecondary, fontSize = 14.sp) }
            t.nota?.let { Text(it, color = PuTextSecondary, fontSize = 13.sp) }
            Text(t.aviso, color = PuAccentMid, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SinRecomendacion(motivo: String?, descansoSeg: Int?, onPerfilFisicoClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PuBgCard),
        border = BorderStroke(0.5.dp, PuBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Recomendado para ti", color = PuAccentMid, fontSize = 13.sp)
            Text(
                motivo ?: "Completa tu perfil físico para ver la recomendación",
                color = PuTextPrimary,
                fontSize = 15.sp
            )
            descansoSeg?.let {
                Text("Descanso entre series: ${duracionDescanso(it)}", color = PuTextSecondary, fontSize = 14.sp)
            }
            Button(
                onClick = onPerfilFisicoClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PuAccent, contentColor = PuAccentDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) { Text("Ir a mi perfil físico") }
        }
    }
}

@Composable
private fun ComoSeHace(e: EjercicioDetalle) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Cómo se hace", color = PuTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        if (e.pasos.isEmpty()) {
            Text("Este ejercicio todavía no tiene pasos.", color = PuTextMuted, fontSize = 14.sp)
        }
        e.pasos.forEachIndexed { i, paso ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(PuAccentDark)
                ) { Text("${i + 1}", color = PuAccentMid, fontSize = 13.sp) }
                Text(paso, color = PuTextSecondary, fontSize = 15.sp, modifier = Modifier.weight(1f))
            }
        }
        e.respiracion?.let { Seccion("Respiración") { Text(it, color = PuTextSecondary, fontSize = 15.sp) } }
        if (e.erroresComunes.isNotEmpty()) {
            Seccion("Errores comunes") {
                e.erroresComunes.forEach { Text("• $it", color = PuTextSecondary, fontSize = 15.sp) }
            }
        }
        e.varianteFacil?.let { Seccion("Variante fácil") { Text(it, color = PuTextSecondary, fontSize = 15.sp) } }
        e.varianteDificil?.let { Seccion("Variante difícil") { Text(it, color = PuTextSecondary, fontSize = 15.sp) } }
    }
}

@Composable
private fun Seccion(titulo: String, contenido: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
        Text(titulo, color = PuTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        contenido()
    }
}
