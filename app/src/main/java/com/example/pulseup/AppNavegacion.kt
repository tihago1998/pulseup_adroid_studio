package com.example.pulseup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pulseup.data.Session
import com.example.pulseup.di.Dependencias
import com.example.pulseup.presentation.ejercicios.EjercicioDetalleScreen
import com.example.pulseup.presentation.ejercicios.EjercicioDetalleViewModel
import com.example.pulseup.presentation.ejercicios.EjerciciosScreen
import com.example.pulseup.presentation.ejercicios.EjerciciosViewModel
import com.example.pulseup.presentation.fabricaDe
import com.example.pulseup.presentation.plan.CalendarioScreen
import com.example.pulseup.presentation.plan.CalendarioViewModel
import com.example.pulseup.presentation.plan.PlanSemanalScreen
import com.example.pulseup.presentation.plan.PlanSemanalViewModel
import com.example.pulseup.presentation.rutinas.CrearRutinaScreen
import com.example.pulseup.presentation.rutinas.CrearRutinaViewModel
import com.example.pulseup.presentation.rutinas.RutinaDetalleScreen
import com.example.pulseup.presentation.rutinas.RutinaDetalleViewModel
import com.example.pulseup.presentation.rutinas.RutinasScreen
import com.example.pulseup.presentation.rutinas.RutinasViewModel
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
        composable("home") { entrada ->
            val planHoy: CalendarioViewModel = viewModel(
                factory = fabricaDe { CalendarioViewModel(Dependencias.planRepository) }
            )
            // Al volver al Home desde el plan o el calendario, se refresca "Hoy toca"
            DisposableEffect(entrada) {
                val observador = LifecycleEventObserver { _, evento ->
                    if (evento == Lifecycle.Event.ON_RESUME) planHoy.cargar()
                }
                entrada.lifecycle.addObserver(observador)
                onDispose { entrada.lifecycle.removeObserver(observador) }
            }
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
                onEjerciciosClick = { navController.navigate("ejercicios") },
                onRutinasClick = { navController.navigate("rutinas") },
                planHoy = planHoy,
                onPlanClick = { navController.navigate("plan") },
                onCalendarioClick = { navController.navigate("calendario") },
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
        // ---------- Plan semanal y calendario ----------
        composable("plan") {
            val viewModel: PlanSemanalViewModel = viewModel(
                factory = fabricaDe {
                    PlanSemanalViewModel(Dependencias.planRepository, Dependencias.rutinasRepository)
                }
            )
            PlanSemanalScreen(viewModel = viewModel, onVolver = { navController.popBackStack() })
        }
        composable("calendario") {
            val viewModel: CalendarioViewModel = viewModel(
                factory = fabricaDe { CalendarioViewModel(Dependencias.planRepository) }
            )
            CalendarioScreen(
                viewModel = viewModel,
                onVolver = { navController.popBackStack() },
                onPlanClick = { navController.navigate("plan") }
            )
        }
        // ---------- Ejercicios y rutinas ----------
        composable("ejercicios") {
            val viewModel: EjerciciosViewModel = viewModel(
                factory = fabricaDe { EjerciciosViewModel(Dependencias.ejerciciosRepository) }
            )
            EjerciciosScreen(
                viewModel = viewModel,
                onVolver = { navController.popBackStack() },
                onEjercicioClick = { id -> navController.navigate("ejercicio/$id") }
            )
        }
        composable(
            route = "ejercicio/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            val id = entrada.arguments?.getInt("id") ?: 0
            val viewModel: EjercicioDetalleViewModel = viewModel(
                factory = fabricaDe { EjercicioDetalleViewModel(Dependencias.ejerciciosRepository, id) }
            )
            EjercicioDetalleScreen(viewModel = viewModel, onVolver = { navController.popBackStack() })
        }
        composable("rutinas") { entrada ->
            val viewModel: RutinasViewModel = viewModel(
                factory = fabricaDe { RutinasViewModel(Dependencias.rutinasRepository) }
            )
            // Al crear o eliminar una rutina, la pantalla que se cierra deja esta marca para recargar la lista
            val recargar by entrada.savedStateHandle.getStateFlow("recargar", false).collectAsState()
            LaunchedEffect(recargar) {
                if (recargar) {
                    viewModel.cargar()
                    entrada.savedStateHandle["recargar"] = false
                }
            }
            RutinasScreen(
                viewModel = viewModel,
                onVolver = { navController.popBackStack() },
                onRutinaClick = { id -> navController.navigate("rutina/$id") },
                onCrearClick = { navController.navigate("rutinaNueva") }
            )
        }
        composable(
            route = "rutina/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            val id = entrada.arguments?.getInt("id") ?: 0
            val viewModel: RutinaDetalleViewModel = viewModel(
                factory = fabricaDe { RutinaDetalleViewModel(Dependencias.rutinasRepository, id) }
            )
            RutinaDetalleScreen(
                viewModel = viewModel,
                onVolver = { navController.popBackStack() },
                onEjercicioClick = { idEjercicio -> navController.navigate("ejercicio/$idEjercicio") },
                onEliminada = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("recargar", true)
                    navController.popBackStack()
                }
            )
        }
        composable("rutinaNueva") {
            val viewModel: CrearRutinaViewModel = viewModel(
                factory = fabricaDe {
                    CrearRutinaViewModel(Dependencias.rutinasRepository, Dependencias.ejerciciosRepository)
                }
            )
            CrearRutinaScreen(
                viewModel = viewModel,
                onVolver = { navController.popBackStack() },
                onCreada = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("recargar", true)
                    navController.popBackStack()
                }
            )
        }
    }
}