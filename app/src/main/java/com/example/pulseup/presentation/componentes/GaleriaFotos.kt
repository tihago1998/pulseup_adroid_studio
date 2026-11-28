package com.example.pulseup.presentation.componentes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.domain.model.ImagenEjercicio
import com.example.pulseup.ui.theme.PuAccent
import com.example.pulseup.ui.theme.PuBorder
import com.example.pulseup.ui.theme.PuTextMuted

/**
 * Fotos de inicio y final de un ejercicio: se pasan deslizando, con un indicador de puntos
 * y la etiqueta de la foto debajo. La usan el detalle del ejercicio y el plan del día.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GaleriaFotos(imagenes: List<ImagenEjercicio>, nombre: String, altura: Dp = 240.dp) {
    val forma = RoundedCornerShape(16.dp)
    if (imagenes.isEmpty()) {
        ImagenRemota(
            url = null,
            descripcion = nombre,
            conTexto = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(altura - 20.dp)
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
                .height(altura)
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

fun etiquetaFoto(orden: Int): String = when (orden) {
    0 -> "Posición inicial"
    1 -> "Posición final"
    else -> "Foto ${orden + 1}"
}
