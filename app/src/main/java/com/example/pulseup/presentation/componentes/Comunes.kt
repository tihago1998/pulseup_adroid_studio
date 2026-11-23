package com.example.pulseup.presentation.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.ui.theme.PuAccent
import com.example.pulseup.ui.theme.PuAccentDark
import com.example.pulseup.ui.theme.PuBgBase
import com.example.pulseup.ui.theme.PuBgCard
import com.example.pulseup.ui.theme.PuTextMuted
import com.example.pulseup.ui.theme.PuTextPrimary
import com.example.pulseup.ui.theme.PuBorder
import com.example.pulseup.ui.theme.PuTextSecondary

/** "pecho" -> "Pecho". Los textos del backend vienen en minúscula. */
fun textoBonito(texto: String): String = texto.replaceFirstChar { it.uppercase() }

/** Marco común de las pantallas nuevas: fondo, flecha de volver y título. */
@Composable
fun PantallaPu(
    titulo: String,
    onVolver: () -> Unit,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PuBgBase)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = PuTextPrimary
                )
            }
            Text(titulo, color = PuTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        }
        contenido()
    }
}

/** Botón redondeado tipo "chip" para filtros. */
@Composable
fun ChipPu(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    val forma = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .clip(forma)
            .background(if (seleccionado) PuAccent else PuBgCard)
            .border(0.5.dp, if (seleccionado) PuAccent else PuBorder, forma)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            texto,
            color = if (seleccionado) PuAccentDark else PuTextSecondary,
            fontSize = 13.sp
        )
    }
}

/** Etiqueta pequeña que no se toca (nivel, equipo, objetivo...). */
@Composable
fun EtiquetaPu(texto: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(PuAccentDark)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(texto, color = PuAccent, fontSize = 11.sp)
    }
}

/** Fila horizontal de chips con un título; tocar el elegido otra vez lo quita. */
@Composable
fun SeccionChips(
    titulo: String,
    opciones: List<String>,
    seleccionada: String?,
    onElegir: (String?) -> Unit
) {
    Column(modifier = Modifier.padding(top = 10.dp)) {
        Text(
            titulo,
            color = PuTextMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(opciones) { opcion ->
                ChipPu(
                    texto = textoBonito(opcion),
                    seleccionado = opcion == seleccionada,
                    onClick = { onElegir(if (opcion == seleccionada) null else opcion) }
                )
            }
        }
    }
}

@Composable
fun EstadoCarga(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = PuAccent)
    }
}

@Composable
fun EstadoError(mensaje: String, onReintentar: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(mensaje, color = PuTextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
        Button(
            onClick = onReintentar,
            colors = ButtonDefaults.buttonColors(containerColor = PuAccent, contentColor = PuAccentDark)
        ) { Text("Reintentar") }
    }
}

@Composable
fun EstadoVacio(texto: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        Text(texto, color = PuTextMuted, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

/** Colores de los campos de texto con el estilo oscuro de PulseUp. */
@Composable
fun coloresCampoPu(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = PuTextPrimary,
    unfocusedTextColor = PuTextPrimary,
    focusedBorderColor = PuAccent,
    unfocusedBorderColor = PuBorder,
    cursorColor = PuAccent,
    focusedLabelColor = PuAccent,
    unfocusedLabelColor = PuTextMuted,
    focusedPlaceholderColor = PuTextMuted,
    unfocusedPlaceholderColor = PuTextMuted
)
