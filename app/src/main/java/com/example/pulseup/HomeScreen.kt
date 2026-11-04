package com.example.pulseup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.ui.theme.PuBgSurface
import com.example.pulseup.ui.theme.PuBorder
import com.example.pulseup.ui.theme.PuTextSecondary
import com.example.pulseup.data.Habito
import com.example.pulseup.data.HabitoRequest
import com.example.pulseup.data.RetrofitClient
import com.example.pulseup.data.Session
import kotlinx.coroutines.launch

// Los colores (PuBgBase, PuBgCard, PuAccent, etc.) salen de los que ya tienes
// declarados en el paquete com.example.pulseup. Aquí no se redefinen.
// Este archivo usa PuBgSurface y PuBorder además de los que usaba HabitosScreen:
// si alguno no existe en tu Color.kt, agrégalo allí.

private val CATEGORIAS = listOf("Ejercicio", "Nutrición", "Sueño")

private fun emojiDe(categoria: String): String {
    val c = categoria.lowercase()
    return when {
        c.contains("ejerc") -> "🏃"
        c.contains("nutri") || c.contains("alim") -> "🥗"
        c.contains("sue") -> "🌙"
        else -> "✨"
    }
}

private fun claveDe(h: Habito) = "${h.descripcion}|${h.fecha}"

@Composable
fun HomeScreen(
    nombre: String = "Usuario",
    onHabitoClick: (Habito) -> Unit = {},
    onAdminClick: () -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    onEditarPerfilClick: () -> Unit = {},
    onCambiarPasswordClick: () -> Unit = {},
    onNotificacionesClick: () -> Unit = {},
    onConfiguracionClick: () -> Unit = {},
    onProgresoClick: () -> Unit = {}
) {
    var listaHabitos by remember { mutableStateOf<List<Habito>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mensaje by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    var mostrarNuevo by remember { mutableStateOf(false) }
    var mostrarPerfil by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    var textoNuevo by remember { mutableStateOf("") }
    var categoriaNueva by remember { mutableStateOf(CATEGORIAS.first()) }

    // Marcas de "hecho hoy". Por ahora solo viven en memoria (el backend aún no guarda este estado)
    val completados = remember { mutableStateListOf<String>() }

    val scope = rememberCoroutineScope()

    suspend fun cargarHabitos() {
        val resp = RetrofitClient.api.getHabitos(Session.bearer())
        if (resp.isSuccessful) {
            listaHabitos = resp.body()?.habitos ?: emptyList()
        }
    }

    LaunchedEffect(Unit) {
        try {
            cargarHabitos()
        } catch (e: Exception) {
            mensaje = "No se pudieron cargar los hábitos"
        } finally {
            cargando = false
        }
    }

    fun guardarHabito() {
        if (textoNuevo.isBlank()) return
        mensaje = ""
        guardando = true
        scope.launch {
            try {
                val resp = RetrofitClient.api.crearHabito(
                    Session.bearer(),
                    HabitoRequest(descripcion = textoNuevo.trim(), categoria = categoriaNueva)
                )
                if (resp.isSuccessful) {
                    textoNuevo = ""
                    mostrarNuevo = false
                    cargarHabitos()
                } else {
                    mensaje = "Error al guardar (código ${resp.code()})"
                    mostrarNuevo = false
                }
            } catch (e: Exception) {
                mensaje = "No se pudo conectar: ${e.message}"
                mostrarNuevo = false
            } finally {
                guardando = false
            }
        }
    }

    // ---------- Diálogo: nuevo hábito ----------
    if (mostrarNuevo) {
        AlertDialog(
            onDismissRequest = { if (!guardando) mostrarNuevo = false },
            containerColor = PuBgCard,
            title = { Text("Nuevo hábito", color = PuTextPrimary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = textoNuevo,
                        onValueChange = { textoNuevo = it },
                        label = { Text("Ej: 30 min de caminata") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PuTextPrimary,
                            unfocusedTextColor = PuTextPrimary,
                            focusedBorderColor = PuAccent,
                            unfocusedBorderColor = PuBorder,
                            focusedLabelColor = PuAccent,
                            unfocusedLabelColor = PuTextMuted,
                            cursorColor = PuAccent
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CATEGORIAS.forEach { cat ->
                            val activa = cat == categoriaNueva
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (activa) PuAccentDark else PuBgBase)
                                    .border(
                                        0.5.dp,
                                        if (activa) PuAccent else PuBorder,
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable { categoriaNueva = cat }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    cat,
                                    fontSize = 12.sp,
                                    color = if (activa) PuAccentMid else PuTextSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { guardarHabito() },
                    enabled = !guardando && textoNuevo.isNotBlank()
                ) {
                    Text(if (guardando) "Guardando..." else "Agregar", color = PuAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarNuevo = false }, enabled = !guardando) {
                    Text("Cancelar", color = PuTextMuted)
                }
            }
        )
    }

    // ---------- Diálogo: menú de perfil ----------
    if (mostrarPerfil) {
        AlertDialog(
            onDismissRequest = { mostrarPerfil = false },
            containerColor = PuBgCard,
            title = { Text("Tu cuenta", color = PuTextPrimary) },
            text = {
                Column {
                    TextButton(onClick = { mostrarPerfil = false; onEditarPerfilClick() }) {
                        Text("Editar datos", color = PuAccent)
                    }
                    TextButton(onClick = { mostrarPerfil = false; onCambiarPasswordClick() }) {
                        Text("Cambiar contraseña", color = PuAccent)
                    }
                    if (Session.isAdmin) {
                        TextButton(onClick = { mostrarPerfil = false; onAdminClick() }) {
                            Text("Administrar usuarios", color = PuAccent)
                        }
                    }
                    TextButton(onClick = { mostrarPerfil = false; mostrarConfirmacion = true }) {
                        Text("Cerrar sesión", color = PuAccentMid)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarPerfil = false }) {
                    Text("Cerrar", color = PuTextMuted)
                }
            }
        )
    }

    // ---------- Diálogo: confirmar cierre de sesión ----------
    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            containerColor = PuBgCard,
            title = { Text("¿Seguro que quieres cerrar sesión?", color = PuTextPrimary) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmacion = false
                    Session.cerrarSesion()
                    onCerrarSesion()
                }) {
                    Text("Cerrar sesión", color = PuAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) {
                    Text("Cancelar", color = PuTextMuted)
                }
            }
        )
    }

    val hechos = listaHabitos.count { claveDe(it) in completados }

    Scaffold(
        containerColor = PuBgBase,
        bottomBar = {
            PuBottomBar(
                onProgresoClick = onProgresoClick,
                onAgregarClick = { mostrarNuevo = true },
                onNotificacionesClick = onNotificacionesClick,
                onPerfilClick = { mostrarPerfil = true }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Encabezado(nombre, onConfiguracionClick) }
            item {
                Spacer(Modifier.height(8.dp))
                TarjetaProgreso(hechos = hechos, total = listaHabitos.size)
                Spacer(Modifier.height(12.dp))
            }
            item {
                Text(
                    "Mis hábitos de hoy",
                    color = PuTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            if (mensaje.isNotEmpty()) {
                item { Text(mensaje, color = PuAccentMid, fontSize = 13.sp) }
            }
            if (cargando) {
                item { Text("Cargando...", color = PuTextMuted, fontSize = 13.sp) }
            } else if (listaHabitos.isEmpty() && mensaje.isEmpty()) {
                item {
                    Text(
                        "Aún no tienes hábitos. Toca + para crear el primero.",
                        color = PuTextMuted,
                        fontSize = 13.sp
                    )
                }
            }
            items(listaHabitos) { habito ->
                val hecho = claveDe(habito) in completados
                TarjetaHabito(
                    habito = habito,
                    hecho = hecho,
                    onClick = { onHabitoClick(habito) },
                    onToggle = {
                        val k = claveDe(habito)
                        if (hecho) completados.remove(k) else completados.add(k)
                    }
                )
            }
        }
    }
}

@Composable
private fun Encabezado(nombre: String, onConfiguracionClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Hola,", color = PuTextMuted, fontSize = 13.sp)
            Text(
                nombre,
                color = PuTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PuBgCard)
                .border(0.5.dp, PuBorder, RoundedCornerShape(10.dp))
                .clickable { onConfiguracionClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Configuración",
                tint = PuAccentMid,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TarjetaProgreso(hechos: Int, total: Int) {
    val fraccion = if (total == 0) 0f else hechos.toFloat() / total
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PuAccentDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("Progreso de hoy", color = PuAccentMid, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "$hechos de $total",
                        color = PuTextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text("🔥", fontSize = 28.sp)
            }
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(PuBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraccion)
                        .fillMaxHeight()
                        .background(PuAccent)
                )
            }
        }
    }
}

@Composable
private fun TarjetaHabito(
    habito: Habito,
    hecho: Boolean,
    onClick: () -> Unit,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PuBgCard),
        border = BorderStroke(0.5.dp, PuBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PuAccentDark),
                contentAlignment = Alignment.Center
            ) {
                Text(emojiDe(habito.categoria), fontSize = 18.sp)
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    habito.descripcion,
                    color = PuTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${habito.categoria} — ${habito.fecha}",
                    color = PuTextMuted,
                    fontSize = 12.sp
                )
            }
            if (hecho) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(PuAccent)
                        .clickable { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Hecho",
                        tint = PuAccentDark,
                        modifier = Modifier.size(14.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, PuBorder, CircleShape)
                        .clickable { onToggle() }
                )
            }
        }
    }
}

@Composable
private fun PuBottomBar(
    onProgresoClick: () -> Unit,
    onAgregarClick: () -> Unit,
    onNotificacionesClick: () -> Unit,
    onPerfilClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PuBgSurface)
            .navigationBarsPadding()
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(PuBorder)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ItemNav("Inicio", Icons.Default.Home, activo = true, onClick = {})
                ItemNav(
                    "Progreso",
                    Icons.Default.DateRange,
                    activo = false,
                    onClick = onProgresoClick
                )
            }

            Box(
                modifier = Modifier
                    .offset(y = (-14).dp)
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PuAccent)
                    .clickable { onAgregarClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Agregar hábito",
                    tint = PuAccentDark,
                    modifier = Modifier.size(24.dp)
                )
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ItemNav("Avisos", Icons.Default.Notifications, activo = false, onClick = onNotificacionesClick)
                ItemNav("Perfil", Icons.Default.Person, activo = false, onClick = onPerfilClick)
            }
        }
    }
}

@Composable
private fun ItemNav(
    etiqueta: String,
    icono: ImageVector,
    activo: Boolean,
    onClick: () -> Unit
) {
    val color = if (activo) PuAccent else PuTextMuted
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Icon(icono, contentDescription = etiqueta, tint = color, modifier = Modifier.size(22.dp))
        Text(etiqueta, color = color, fontSize = 11.sp)
    }
}