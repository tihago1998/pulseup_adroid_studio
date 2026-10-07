package com.example.pulseup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.data.EstadisticasResponse
import com.example.pulseup.data.RetrofitClient
import com.example.pulseup.data.Session
import com.example.pulseup.data.UpdateRolesRequest
import com.example.pulseup.data.Usuario
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip

@Composable
fun UsersScreen(onVolverClick: () -> Unit = {}) {
    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var estadisticas by remember { mutableStateOf<EstadisticasResponse?>(null) }
    var mensaje by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val miId = Session.usuario?.id_usuario
    val soySuperAdmin = Session.isSuperAdmin

    suspend fun cargarUsuarios() {
        val resp = RetrofitClient.api.getUsers(Session.bearer())
        if (resp.isSuccessful) {
            usuarios = resp.body()?.usuarios ?: emptyList()
        }
    }

    suspend fun cargarEstadisticas() {
        val resp = RetrofitClient.api.getEstadisticas(Session.bearer())
        if (resp.isSuccessful) {
            estadisticas = resp.body()
        }
    }

    LaunchedEffect(Unit) {
        try {
            cargarUsuarios()
            cargarEstadisticas()
        } catch (_: Exception) { }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PuBgBase)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Usuarios del sistema", fontSize = 22.sp, fontWeight = FontWeight.Medium, color = PuTextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Los usuarios se registran ellos mismos desde la pantalla de inicio.",
                fontSize = 12.sp,
                color = PuTextMuted
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Tarjeta de estadísticas: total y cuántos hay de cada rol
            estadisticas?.let { stats ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(PuAccentDark)
                        .padding(12.dp)
                ) {
                    Text("Total de usuarios: ${stats.total}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = PuTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        stats.por_rol.forEach { (rol, cantidad) ->
                            Text("$rol: $cantidad", fontSize = 12.sp, color = PuAccentMid)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (mensaje.isNotEmpty()) {
                Text(mensaje, color = PuAccentMid, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(usuarios, key = { it.id_usuario }) { u ->
                    val esAdmin = u.roles.contains("Administrador")
                    val esSuperAdmin = u.roles.contains("SuperAdministrador")
                    val esYoMismo = u.id_usuario == miId

                    // Un Administrador normal solo puede tocar cuentas que hoy son Usuario común.
                    // El SuperAdministrador puede tocar cualquiera (salvo eliminarse a sí mismo).
                    val puedoModificarRol = soySuperAdmin || (!esAdmin && !esSuperAdmin)
                    val puedoEliminar = soySuperAdmin && !esYoMismo

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(PuBgCard)
                            .padding(12.dp)
                    ) {
                        Text(u.nombre, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = PuTextPrimary)
                        Text(u.email, fontSize = 12.sp, color = PuTextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            when {
                                esSuperAdmin -> "SuperAdministrador"
                                esAdmin -> "Administrador"
                                else -> "Usuario"
                            },
                            fontSize = 11.sp,
                            color = if (esAdmin || esSuperAdmin) PuAccent else PuTextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Cambiar rol: visible solo si tengo permiso sobre esta cuenta
                            if (puedoModificarRol && !esSuperAdmin) {
                                if (esAdmin) {
                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    val resp = RetrofitClient.api.updateRoles(
                                                        Session.bearer(), u.id_usuario, UpdateRolesRequest(role_ids = listOf(2))
                                                    )
                                                    mensaje = if (resp.isSuccessful) "✅ ${u.nombre} ahora es usuario"
                                                    else "❌ No se pudo quitar el rol (${resp.code()})"
                                                    cargarUsuarios()
                                                    cargarEstadisticas()
                                                } catch (e: Exception) {
                                                    mensaje = "⚠️ ${e.message}"
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PuAccent)
                                    ) {
                                        Text("Quitar admin", fontSize = 12.sp)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    val resp = RetrofitClient.api.updateRoles(
                                                        Session.bearer(), u.id_usuario, UpdateRolesRequest(role_ids = listOf(1))
                                                    )
                                                    mensaje = if (resp.isSuccessful) "✅ ${u.nombre} ahora es administrador"
                                                    else "❌ No se pudo asignar el rol (${resp.code()})"
                                                    cargarUsuarios()
                                                    cargarEstadisticas()
                                                } catch (e: Exception) {
                                                    mensaje = "⚠️ ${e.message}"
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PuAccent, contentColor = PuAccentDark)
                                    ) {
                                        Text("Hacer admin", fontSize = 12.sp)
                                    }
                                }
                            }

                            // Eliminar: exclusivo del SuperAdministrador, nunca sobre su propia cuenta
                            if (puedoEliminar) {
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                val resp = RetrofitClient.api.eliminarUsuario(Session.bearer(), u.id_usuario)
                                                mensaje = if (resp.isSuccessful) "🗑️ ${u.nombre} eliminado"
                                                else "❌ No se pudo eliminar (${resp.code()})"
                                                cargarUsuarios()
                                                cargarEstadisticas()
                                            } catch (e: Exception) {
                                                mensaje = "⚠️ ${e.message}"
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PuAccentMid)
                                ) {
                                    Text("Eliminar", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onVolverClick) {
                Text("Volver", color = PuAccent)
            }
        }
    }
}