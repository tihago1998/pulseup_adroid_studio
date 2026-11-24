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
import com.example.pulseup.presentation.ejercicios.CreditosScreen
import com.example.pulseup.presentation.ejercicios.CreditosViewModel
import com.example.pulseup.presentation.ejercicios.EjercicioDetalleScreen
import com.example.pulseup.presentation.ejercicios.EjercicioDetalleViewModel
import com.example.pulseup.presentation.ejercicios.EjerciciosScreen
import com.example.pulseup.presentation.ejercicios.EjerciciosViewModel
import com.example.pulseup.presentation.fabricaDe
import com.example.pulseup.presentation.home.HomeScreen
import com.example.pulseup.presentation.home.HomeViewModel
import com.example.pulseup.presentation.perfil.PerfilFisicoScreen
import com.example.pulseup.presentation.perfil.PerfilFisicoViewModel
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
            val homeViewModel: HomeViewModel = viewModel(
                factory = fabricaDe { HomeViewModel(Dependencias.planRepository) }
            )
            // Al volver al Home (desde el plan, el calendario...) se refresca "Hoy toca";
            // al terminar, el Home recalcula la tira de la semana y el progreso.
            DisposableEffect(entrada) {
                val observador = LifecycleEventObserver { _, evento ->
                    if (evento == Lifecycle.Event.ON_RESUME) planHoy.cargar()
                }
                entrada.lifecycle.addObserver(observador)
                onDispose { entrada.lifecycle.removeObserver(observador) }
            }
            HomeScreen(
                nombre = Session.primerNombre,
                frase = Session.frase,
                viewModel = homeViewModel,
                planHoy = planHoy,
                onPlanClick = { navController.navigate("plan") },
                onCalendarioClick = { navController.navigate("calendario") },
                onEjerciciosClick = { navController.navigate("ejercicios") },
                onRutinasClick = { navController.navigate("rutinas") },
                onPerfilFisicoClick = { navController.navigate("perfilFisico") },
                onEditarPerfilClick = { navController.navigate("editProfile") },
                onCambiarPasswordClick = { navController.navigate("changePassword") },
                onAdminClick = { navController.navigate("users") },
                onCerrarSesion = {
                    // Vamos a "login" y borramos "home" del historial,
                    // para que el botón "atrás" no regrese a una sesión ya cerrada
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
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
        // ---------- Perfil físico ----------
        composable("perfilFisico") {
            val viewModel: PerfilFisicoViewModel = viewModel(
                factory = fabricaDe { PerfilFisicoViewModel(Dependencias.perfilFisicoRepository) }
            )
            PerfilFisicoScreen(
                viewModel = viewModel,
                onVolver = { navController.popBackStack() },
                onGuardado = { navController.popBackStack() }
            )
        }
        // ---------- Ejercicios y rutinas ----------
        composable("ejercicios") {
            val viewModel: EjerciciosViewModel = viewModel(
                factory = fabricaDe {
                    EjerciciosViewModel(Dependencias.ejerciciosRepository, Dependencias.perfilFisicoRepository)
                }
            )
            EjerciciosScreen(
                viewModel = viewModel,
                onVolver = { navController.popBackStack() },
                onEjercicioClick = { id -> navController.navigate("ejercicio/$id") },
                onCreditosClick = { navController.navigate("creditosEjercicios") }
            )
        }
        composable("creditosEjercicios") {
            val viewModel: CreditosViewModel = viewModel(
                factory = fabricaDe { CreditosViewModel(Dependencias.ejerciciosRepository) }
            )
            CreditosScreen(viewModel = viewModel, onVolver = { navController.popBackStack() })
        }
        composable(
            route = "ejercicio/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            val id = entrada.arguments?.getInt("id") ?: 0
            val viewModel: EjercicioDetalleViewModel = viewModel(
                factory = fabricaDe {
                    EjercicioDetalleViewModel(Dependencias.ejerciciosRepository, Dependencias.perfilFisicoRepository, id)
                }
            )
            // Al volver de Perfil físico se pide otra vez la recomendación (si antes no había)
            DisposableEffect(entrada) {
                val observador = LifecycleEventObserver { _, evento ->
                    if (evento == Lifecycle.Event.ON_RESUME) viewModel.refrescarRecomendacion()
                }
                entrada.lifecycle.addObserver(observador)
                onDispose { entrada.lifecycle.removeObserver(observador) }
            }
            EjercicioDetalleScreen(
                viewModel = viewModel,
                onVolver = { navController.popBackStack() },
                onPerfilFisicoClick = { navController.navigate("perfilFisico") }
            )
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