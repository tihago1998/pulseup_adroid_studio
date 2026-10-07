package com.example.pulseup

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginExitoso = {
                    // Al loguearse, navegamos a "lista" y quitamos "login" del historial
                    // (popUpTo + inclusive = true) para que el botón "atrás" no regrese al login
                    navController.navigate("lista") {
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
        composable("lista") {
            HabitosScreen(
                onHabitoClick = { habito ->
                    val descEncoded = URLEncoder.encode(habito.descripcion, "UTF-8")
                    navController.navigate("detalle/$descEncoded/${habito.categoria}")
                },
                onAdminClick = {
                    navController.navigate("users")
                },
                onCerrarSesion = {
                    // Simétrico al login: vamos a "login" y borramos "lista" del historial,
                    // para que el botón "atrás" no regrese a una sesión ya cerrada
                    navController.navigate("login") {
                        popUpTo("lista") { inclusive = true }
                    }
                },
                onEditarPerfilClick = {
                    navController.navigate("editProfile")
                },
                onCambiarPasswordClick = {
                    navController.navigate("changePassword")
                }
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