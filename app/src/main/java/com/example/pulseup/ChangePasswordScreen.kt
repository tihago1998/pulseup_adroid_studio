package com.example.pulseup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.data.ChangePasswordRequest
import com.example.pulseup.data.RetrofitClient
import com.example.pulseup.data.Session
import kotlinx.coroutines.launch

@Composable
fun ChangePasswordScreen(onVolverClick: () -> Unit = {}) {
    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PuBgBase)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Cambiar contraseña", fontSize = 22.sp, fontWeight = FontWeight.Medium, color = PuTextPrimary)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = actual,
                onValueChange = { actual = it },
                label = { Text("Contraseña actual") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = nueva,
                onValueChange = { nueva = it },
                label = { Text("Nueva contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (actual.isBlank() || nueva.isBlank()) {
                        mensaje = "Completa los dos campos"
                        return@Button
                    }
                    cargando = true
                    mensaje = ""
                    scope.launch {
                        try {
                            val resp = RetrofitClient.api.changePassword(
                                Session.bearer(), ChangePasswordRequest(current_password = actual, new_password = nueva)
                            )
                            mensaje = when {
                                resp.isSuccessful -> "✅ Contraseña cambiada"
                                resp.code() == 400 -> "❌ La contraseña actual es incorrecta"
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
                Text(if (cargando) "Actualizando..." else "Actualizar", fontWeight = FontWeight.Medium)
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