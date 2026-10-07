package com.example.pulseup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.data.RetrofitClient
import com.example.pulseup.data.Session
import com.example.pulseup.data.UpdateProfileRequest
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(onVolverClick: () -> Unit = {}) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Al abrir la pantalla, traemos los datos actuales del servidor para precargar el formulario
    LaunchedEffect(Unit) {
        try {
            val resp = RetrofitClient.api.getProfile(Session.bearer())
            if (resp.isSuccessful) {
                nombre = resp.body()?.usuario?.nombre ?: ""
                email = resp.body()?.usuario?.email ?: ""
            }
        } catch (_: Exception) { /* si falla, dejamos los campos vacíos */ }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PuBgBase)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Editar mis datos", fontSize = 22.sp, fontWeight = FontWeight.Medium, color = PuTextPrimary)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (nombre.isBlank() || email.isBlank()) {
                        mensaje = "Completa los dos campos"
                        return@Button
                    }
                    cargando = true
                    mensaje = ""
                    scope.launch {
                        try {
                            val resp = RetrofitClient.api.updateProfile(
                                Session.bearer(), UpdateProfileRequest(nombre = nombre, email = email)
                            )
                            mensaje = when {
                                resp.isSuccessful -> {
                                    // Actualizamos también el usuario guardado en la sesión
                                    resp.body()?.usuario?.let { Session.usuario = it }
                                    "✅ Perfil actualizado"
                                }
                                resp.code() == 409 -> "❌ Ese correo ya está en uso"
                                else -> "❌ Error (código ${resp.code()})"
                            }
                        } catch (e: Exception) {
                            mensaje = "⚠️ ${e.message}"
                        } finally {
                            cargando = false
                        }
                    }
                },
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PuAccent, contentColor = PuAccentDark)
            ) {
                Text(if (cargando) "Guardando..." else "Guardar", fontWeight = FontWeight.Medium)
            }

            if (mensaje.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(mensaje, color = PuAccentMid, fontSize = 13.sp)
            }

            TextButton(onClick = onVolverClick) {
                Text("Volver", color = PuAccent)
            }
        }
    }
}