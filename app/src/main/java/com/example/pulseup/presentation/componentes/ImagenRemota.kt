package com.example.pulseup.presentation.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.pulseup.ui.theme.PuAccent
import com.example.pulseup.ui.theme.PuBgSurface
import com.example.pulseup.ui.theme.PuTextMuted

/**
 * Foto de un ejercicio cargada con Coil. Mientras carga muestra un indicador; si no hay foto
 * o no se pudo descargar, un icono (y un texto si [conTexto]), nunca un hueco en blanco.
 */
@Composable
fun ImagenRemota(
    url: String?,
    descripcion: String?,
    modifier: Modifier = Modifier,
    conTexto: Boolean = false
) {
    Box(modifier = modifier.background(PuBgSurface), contentAlignment = Alignment.Center) {
        if (url == null) {
            AvisoImagen(Icons.Default.FitnessCenter, "Sin foto", conTexto)
        } else {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = descripcion,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PuAccent, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                    }
                },
                error = { AvisoImagen(Icons.Default.BrokenImage, "No se pudo cargar la foto", conTexto) }
            )
        }
    }
}

@Composable
private fun AvisoImagen(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String, conTexto: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icono, contentDescription = if (conTexto) null else texto, tint = PuTextMuted, modifier = Modifier.size(28.dp))
        if (conTexto) Text(texto, color = PuTextMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}
