package com.example.pulseup.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.pulseup.ui.theme.PuAccent
import com.example.pulseup.ui.theme.PuAccentMid
import com.example.pulseup.ui.theme.PuBgCard
import com.example.pulseup.ui.theme.PuTextMuted
import com.example.pulseup.ui.theme.PuTextPrimary
import com.example.pulseup.data.Session

/**
 * Menú "Tu cuenta" que abre el botón Perfil de la barra inferior, con la confirmación de cierre de sesión.
 * Misma lógica que tenía el Home anterior.
 */
@Composable
fun MenuPerfil(
    visible: Boolean,
    onCerrar: () -> Unit,
    onEditarPerfilClick: () -> Unit,
    onCambiarPasswordClick: () -> Unit,
    onAdminClick: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    var confirmandoSalida by remember { mutableStateOf(false) }

    if (visible) {
        AlertDialog(
            onDismissRequest = onCerrar,
            containerColor = PuBgCard,
            title = { Text("Tu cuenta", color = PuTextPrimary) },
            text = {
                Column {
                    TextButton(onClick = { onCerrar(); onEditarPerfilClick() }) {
                        Text("Editar datos", color = PuAccent)
                    }
                    TextButton(onClick = { onCerrar(); onCambiarPasswordClick() }) {
                        Text("Cambiar contraseña", color = PuAccent)
                    }
                    if (Session.isAdmin) {
                        TextButton(onClick = { onCerrar(); onAdminClick() }) {
                            Text("Administrar usuarios", color = PuAccent)
                        }
                    }
                    TextButton(onClick = { onCerrar(); confirmandoSalida = true }) {
                        Text("Cerrar sesión", color = PuAccentMid)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onCerrar) { Text("Cerrar", color = PuTextMuted) }
            }
        )
    }

    if (confirmandoSalida) {
        AlertDialog(
            onDismissRequest = { confirmandoSalida = false },
            containerColor = PuBgCard,
            title = { Text("¿Seguro que quieres cerrar sesión?", color = PuTextPrimary) },
            confirmButton = {
                TextButton(onClick = {
                    confirmandoSalida = false
                    Session.cerrarSesion()
                    onCerrarSesion()
                }) { Text("Cerrar sesión", color = PuAccent) }
            },
            dismissButton = {
                TextButton(onClick = { confirmandoSalida = false }) { Text("Cancelar", color = PuTextMuted) }
            }
        )
    }
}
