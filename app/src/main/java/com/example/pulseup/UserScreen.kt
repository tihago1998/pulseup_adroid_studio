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
import com.example.pulseup.data.repository.ejecutar
import com.example.pulseup.domain.Resultado
import com.example.pulseup.presentation.componentes.EstadoCarga
import com.example.pulseup.presentation.componentes.EstadoError
import kotlinx.coroutines.launch
import retrofit2.Response
import androidx.compose.ui.draw.clip
import com.example.pulseup.ui.theme.PuAccent
import com.example.pulseup.ui.theme.PuAccentDark
import com.example.pulseup.ui.theme.PuAccentMid
import com.example.pulseup.ui.theme.PuBgBase
import com.example.pulseup.ui.theme.PuBgCard
import com.example.pulseup.ui.theme.PuTextMuted
import com.example.pulseup.ui.theme.PuTextPrimary

@Composable
fun UsersScreen(onVolverClick: () -> Unit = {}) {
    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var estadisticas by remember { mutableStateOf<EstadisticasResponse?>(null) }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(true) }
    var errorCarga by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val miId = Session.usuario?.id_usuario
    val soySuperAdmin = Session.isSuperAdmin

    // Trae usuarios y estadísticas con el mismo manejo de errores del resto de la app.
    // Devuelve el mensaje del primer error, o null si todo salió bien.
    suspend fun recargar(): String? {
        val rUsuarios = ejecutar({ RetrofitClient.api.getUsers(Session.bearer()) }) { it.usuarios.orEmpty() }
        val rEstadisticas = ejecutar({ RetrofitClient.api.getEstadisticas(Session.bearer()) }) { it }
        if (rUsuarios is Resultado.Exito) usuarios = rUsuarios.datos
        if (rEstadisticas is Resultado.Exito) estadisticas = rEstadisticas.datos
        return (rUsuarios as? Resultado.Fallo)?.mensaje ?: (rEstadisticas as? Resultado.Fallo)?.mensaje
    }

    fun cargar() {
        scope.launch {
            cargando = true
            errorCarga = recargar()
            cargando = false
            // Si la lista sí cargó (falló solo una parte), el error se avisa arriba de la lista
            if (errorCarga != null && usuarios.isNotEmpty()) mensaje = "⚠️ $errorCarga"
        }
    }

    // Cambiar rol o eliminar: muestra el resultado y, si salió bien, recarga la lista
    fun <D> accion(textoExito: String, textoFallo: String, llamada: suspend () -> Response<D>) {
        scope.launch {
            when (val r = ejecutar(llamada) { }) {
                is Resultado.Exito -> {
                    mensaje = textoExito
                    recargar()?.let { mensaje = "$textoExito. ⚠️ No se pudo actualizar la lista: $it" }
                }
                is Resultado.Fallo -> mensaje = "❌ $textoFallo: ${r.mensaje}"
            }
        }
    }

    LaunchedEffect(Unit) { cargar() }

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

            // Sin datos todavía: carga, o error con Reintentar. Con datos: la lista.
            if (usuarios.isEmpty() && (cargando || errorCarga != null)) {
                Box(modifier = Modifier.weight(1f)) {
                    if (cargando) EstadoCarga()
                    else EstadoError(errorCarga.orEmpty(), onReintentar = { cargar() })
                }
            } else LazyColumn(
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
                                            accion("✅ ${u.nombre} ahora es usuario", "No se pudo quitar el rol") {
                                                RetrofitClient.api.updateRoles(
                                                    Session.bearer(), u.id_usuario, UpdateRolesRequest(role_ids = listOf(2))
                                                )
                                            }
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PuAccent)
                                    ) {
                                        Text("Quitar admin", fontSize = 12.sp)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            accion("✅ ${u.nombre} ahora es administrador", "No se pudo asignar el rol") {
                                                RetrofitClient.api.updateRoles(
                                                    Session.bearer(), u.id_usuario, UpdateRolesRequest(role_ids = listOf(1))
                                                )
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
                                        accion("🗑️ ${u.nombre} eliminado", "No se pudo eliminar") {
                                            RetrofitClient.api.eliminarUsuario(Session.bearer(), u.id_usuario)
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