package com.example.pulseup

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pulseup.data.Session
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginExitoso = {
                    // Al loguearse, navegamos a "home" y quitamos "login" del historial
                    // (popUpTo + inclusive = true) para que el botón "atrás" no regrese al login
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegistrarseClick = {
                    navController.navigate("register")
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegistroExitoso = {
                    // Tras registrarse, lo mandamos de vuelta al login para que inicie sesión
                    navController.popBackStack()
                },
                onVolverAlLoginClick = {
                    navController.popBackStack()
                }
            )
        }
        composable("home") {
            HomeScreen(
                nombre = Session.primerNombre,
                onHabitoClick = { habito ->
                    val descEncoded = URLEncoder.encode(habito.descripcion, "UTF-8")
                    navController.navigate("detalle/$descEncoded/${habito.categoria}")
                },
                onAdminClick = {
                    navController.navigate("users")
                },
                onCerrarSesion = {
                    // Vamos a "login" y borramos "home" del historial,
                    // para que el botón "atrás" no regrese a una sesión ya cerrada
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onEditarPerfilClick = {
                    navController.navigate("editProfile")
                },
                onCambiarPasswordClick = {
                    navController.navigate("changePassword")
                },
                onProgresoClick = { /* pendiente */ },
                onNotificacionesClick = { /* pendiente */ },
                onConfiguracionClick = { /* pendiente */ }
            )
        }
        composable(
            route = "detalle/{descripcion}/{categoria}",
            arguments = listOf(
                navArgument("descripcion") { type = NavType.StringType },
                navArgument("categoria") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val descEncoded = backStackEntry.arguments?.getString("descripcion") ?: ""
            val descripcion = URLDecoder.decode(descEncoded, "UTF-8")
            val categoria = backStackEntry.arguments?.getString("categoria") ?: ""

            DetalleHabitoScreen(
                descripcion = descripcion,
                categoria = categoria,
                onVolverClick = { navController.popBackStack() }
            )
        }
        composable("users") {
            UsersScreen(
                onVolverClick = { navController.popBackStack() }
            )
        }
        composable("editProfile") {
            EditProfileScreen(
                onVolverClick = { navController.popBackStack() }
            )
        }
        composable("changePassword") {
            ChangePasswordScreen(
                onVolverClick = { navController.popBackStack() }
            )
        }
    }
}