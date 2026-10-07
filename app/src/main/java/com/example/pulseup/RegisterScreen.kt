package com.example.pulseup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.data.RegisterRequest
import com.example.pulseup.data.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegistroExitoso: () -> Unit = {},
    onVolverAlLoginClick: () -> Unit = {}
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PuBgBase)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Text(
                text = "Crear cuenta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                color = PuTextPrimary
            )
            Text(
                text = "Únete a PulseUp gratis",
                fontSize = 14.sp,
                color = PuTextMuted
            )
            Spacer(modifier = Modifier.height(32.dp))

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
                label = { Text("Correo electrónico") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmarPassword,
                onValueChange = { confirmarPassword = it },
                label = { Text("Confirmar contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    mensajeError = ""

                    // Validaciones básicas ANTES de llamar al backend
                    if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
                        mensajeError = "Completa todos los campos"
                        return@Button
                    }
                    if (password.length < 8) {
                        mensajeError = "La contraseña debe tener al menos 8 caracteres"
                        return@Button
                    }
                    if (password != confirmarPassword) {
                        mensajeError = "Las contraseñas no coinciden"
                        return@Button
                    }

                    cargando = true
                    scope.launch {
                        try {
                            val respuesta = RetrofitClient.api.register(
                                RegisterRequest(nombre = nombre, email = email, password = password)
                            )
                            if (respuesta.isSuccessful) {
                                onRegistroExitoso()
                            } else {
                                mensajeError = when (respuesta.code()) {
                                    409 -> "Ese correo ya está registrado"
                                    400 -> "Faltan datos requeridos"
                                    else -> "Error al registrarse (código ${respuesta.code()})"
                                }
                            }
                        } catch (e: Exception) {
                            mensajeError = "No se pudo conectar al servidor: ${e.message}"
                        } finally {
                            cargando = false
                        }
                    }
                },
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PuAccent,
                    contentColor = PuAccentDark
                )
            ) {
                Text(if (cargando) "Creando cuenta..." else "Crear cuenta", fontWeight = FontWeight.Medium)
            }

            if (mensajeError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = mensajeError, color = PuAccentMid, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onVolverAlLoginClick) {
                Text("¿Ya tienes cuenta? Inicia sesión", color = PuAccent, fontSize = 13.sp)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF14110F)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen()
}