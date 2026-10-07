package com.example.pulseup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.data.Habito
import com.example.pulseup.data.HabitoRequest
import com.example.pulseup.data.RetrofitClient
import com.example.pulseup.data.Session
import kotlinx.coroutines.launch

@Composable
fun HabitosScreen(
    onHabitoClick: (Habito) -> Unit = {},
    onAdminClick: () -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    onEditarPerfilClick: () -> Unit = {},
    onCambiarPasswordClick: () -> Unit = {}
) {
    var textoNuevoHabito by remember { mutableStateOf("") }
    var listaHabitos by remember { mutableStateOf<List<Habito>>(emptyList()) }
    var mensaje by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Carga los hábitos del usuario desde el servidor
    suspend fun cargarHabitos() {
        val resp = RetrofitClient.api.getHabitos(Session.bearer())
        if (resp.isSuccessful) {
            listaHabitos = resp.body()?.habitos ?: emptyList()
        }
    }

    // Al abrir la pantalla, traemos lo que ya está guardado
    LaunchedEffect(Unit) {
        try { cargarHabitos() } catch (e: Exception) {
            mensaje = "No se pudieron cargar los hábitos"
        }
    }

    // Diálogo de confirmación: solo se dibuja cuando mostrarConfirmacion es true
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PuBgBase)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mis hábitos de hoy",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = PuTextPrimary
                )
                TextButton(onClick = { mostrarConfirmacion = true }) {
                    Text("Cerrar sesión", color = PuAccentMid, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = textoNuevoHabito,
                onValueChange = { textoNuevoHabito = it },
                label = { Text("Ej: 30 min de caminata") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (textoNuevoHabito.isBlank()) return@Button
                    mensaje = ""
                    guardando = true
                    scope.launch {
                        try {
                            val resp = RetrofitClient.api.crearHabito(
                                Session.bearer(),
                                HabitoRequest(descripcion = textoNuevoHabito, categoria = "Ejercicio")
                            )
                            if (resp.isSuccessful) {
                                textoNuevoHabito = ""
                                cargarHabitos()   // recargamos para ver el nuevo en la lista
                            } else {
                                mensaje = "Error al guardar (código ${resp.code()})"
                            }
                        } catch (e: Exception) {
                            mensaje = "No se pudo conectar: ${e.message}"
                        } finally {
                            guardando = false
                        }
                    }
                },
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PuAccent,
                    contentColor = PuAccentDark
                )
            ) {
                Text(if (guardando) "Guardando..." else "Agregar hábito", fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Registrados: ${listaHabitos.size}",
                fontSize = 14.sp,
                color = PuTextMuted
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEditarPerfilClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PuAccent)
                ) {
                    Text("Editar datos", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onCambiarPasswordClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PuAccent)
                ) {
                    Text("Cambiar contraseña", fontSize = 12.sp)
                }
            }

            if (Session.isAdmin) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onAdminClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PuBgCard,
                        contentColor = PuAccent
                    )
                ) {
                    Text("Administrar usuarios", fontWeight = FontWeight.Medium)
                }
            }

            if (mensaje.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(mensaje, color = PuAccentMid, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(listaHabitos) { habito ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onHabitoClick(habito) },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = PuBgCard)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(habito.descripcion, fontSize = 15.sp, color = PuTextPrimary)
                            Text(
                                "${habito.categoria} — ${habito.fecha}",
                                fontSize = 12.sp,
                                color = PuTextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}