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
import com.example.pulseup.data.LoginRequest
import com.example.pulseup.data.RetrofitClient
import com.example.pulseup.data.Session
import com.example.pulseup.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit = {},
    onRegistrarseClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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
                text = "PulseUp",
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                color = PuTextPrimary
            )
            Text(
                text = "Inicia sesión para continuar",
                fontSize = 14.sp,
                color = PuTextMuted
            )
            Spacer(modifier = Modifier.height(32.dp))

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
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    mensajeError = ""
                    cargando = true
                    // Lanzamos una corrutina: la petición de red es "suspend",
                    // no puede llamarse directo desde onClick (que no es suspend)
                    scope.launch {
                        try {
                            val respuesta = RetrofitClient.api.login(
                                LoginRequest(email = email, password = password)
                            )
                            if (respuesta.isSuccessful) {
                                val body = respuesta.body()
                                if (body != null) {
                                    // Guardamos el token y el usuario en la sesión global
                                    Session.iniciarSesion(body)
                                    onLoginExitoso()
                                }
                            } else {
                                mensajeError = when (respuesta.code()) {
                                    401 -> "Correo o contraseña incorrectos"
                                    else -> "Error al iniciar sesión (código ${respuesta.code()})"
                                }
                            }
                        } catch (e: Exception) {
                            mensajeError = "No se pudo conectar al servidor: ${e.message}"
                        } finally {
                            cargando = false
                        }
                    }
                },
                enabled = !cargando && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PuAccent,
                    contentColor = PuAccentDark
                )
            ) {
                Text(if (cargando) "Ingresando..." else "Ingresar", fontWeight = FontWeight.Medium)
            }

            if (mensajeError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = mensajeError, color = PuAccentMid, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onRegistrarseClick) {
                Text("¿No tienes cuenta? Regístrate", color = PuAccent, fontSize = 13.sp)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF14110F)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}