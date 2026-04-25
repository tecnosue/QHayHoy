package com.tecnosue.qhayhoy.ui.navegacion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.tecnosue.qhayhoy.ui.auth.AuthViewModel
import com.tecnosue.qhayhoy.ui.auth.LoginScreen
import com.tecnosue.qhayhoy.ui.auth.RegistroScreen
import com.tecnosue.qhayhoy.ui.casa.CasaViewModel
import com.tecnosue.qhayhoy.ui.casa.GestionCasaScreen
import com.tecnosue.qhayhoy.ui.casa.PrincipalScreen
import com.tecnosue.qhayhoy.ui.receta.DetalleRecetaScreen
import com.tecnosue.qhayhoy.ui.receta.EditorRecetaScreen
import com.tecnosue.qhayhoy.ui.receta.ListaRecetasScreen
import com.tecnosue.qhayhoy.ui.receta.RecetaViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

/**
 * Grafo de navegación principal de la aplicación.
 *
 * Define todas las pantallas (destinos) y cómo se conectan entre sí.
 * La ruta inicial depende del estado de autenticación:
 *   - Si hay usuario autenticado → GestionCasa (o Principal si ya tiene casas)
 *   - Si no hay usuario → Login
 */
@OptIn(KoinExperimentalAPI::class) // Koin para ViewModels en KMP a veces requiere esto
@Composable
fun AppNavegacion() {
    val navController = rememberNavController()

    // 2. Reemplaza las líneas de 'remember' por 'koinViewModel()'
    // Esto arregla el error: No value passed for parameter 'usuarioRepository'
    val authViewModel: AuthViewModel = koinViewModel()
    val casaViewModel: CasaViewModel = koinViewModel()
    val recetaViewModel: RecetaViewModel = koinViewModel()


    val authState by authViewModel.uiState.collectAsState()

    // Determinamos la ruta inicial según si hay sesión iniciada
    val rutaInicial = if (authState.usuarioActual != null) {
        Rutas.GestionCasa
    } else {
        Rutas.Login
    }

    NavHost(
        navController = navController,
        startDestination = rutaInicial
    ) {
        composable<Rutas.Login> {
            LoginScreen(
                viewModel = authViewModel,
                onIrARegistro = {
                    navController.navigate(Rutas.Registro)
                },
                onLoginExitoso = {
                    navController.navigate(Rutas.GestionCasa) {
                        // Limpiamos Login del stack para que al pulsar atrás
                        // no vuelva al Login
                        popUpTo(Rutas.Login) { inclusive = true }
                    }
                }
            )
        }

        composable<Rutas.Registro> {
            RegistroScreen(
                viewModel = authViewModel,
                onIrALogin = {
                    navController.popBackStack()
                },
                onRegistroExitoso = {
                    navController.navigate(Rutas.GestionCasa) {
                        popUpTo(Rutas.Login) { inclusive = true }
                    }
                }
            )
        }

        composable<Rutas.GestionCasa> {
            GestionCasaScreen(
                authViewModel = authViewModel,
                casaViewModel = casaViewModel,
                onCasaSeleccionada = {
                    navController.navigate(Rutas.Principal)
                },
                onCerrarSesion = {
                    navController.navigate(Rutas.Login) {
                        popUpTo(Rutas.GestionCasa) { inclusive = true }
                    }
                }
            )
        }

        composable<Rutas.Principal> {
            PrincipalScreen(
                authViewModel = authViewModel,
                casaViewModel = casaViewModel,
                onCerrarSesion = {
                    navController.navigate(Rutas.Login) {
                        popUpTo(Rutas.Principal) { inclusive = true }
                    }
                },
                onIrARecetas = {
                    navController.navigate(Rutas.ListaRecetas)
                },

                onCambiarDeCasa = {
                    navController.navigate(Rutas.GestionCasa) {
                        popUpTo(Rutas.Principal) { inclusive = true }
                    }
                }
            )
        }
        // --- Pantallas de recetas (RF3) ---

        composable<Rutas.ListaRecetas> {
            ListaRecetasScreen(
                authViewModel = authViewModel,
                recetaViewModel = recetaViewModel,
                onVolver = { navController.popBackStack() },
                onNuevaReceta = {
                    navController.navigate(Rutas.EditorReceta())
                },
                onRecetaSeleccionada = { recetaId ->
                    navController.navigate(Rutas.DetalleReceta(recetaId))
                }
            )
        }

        composable<Rutas.EditorReceta> { backStackEntry ->
            val args = backStackEntry.toRoute<Rutas.EditorReceta>()
            EditorRecetaScreen(
                authViewModel = authViewModel,
                recetaViewModel = recetaViewModel,
                recetaId = args.recetaId,
                onVolver = { navController.popBackStack() },
                onGuardado = { navController.popBackStack() }
            )
        }

        composable<Rutas.DetalleReceta> { backStackEntry ->
            val args = backStackEntry.toRoute<Rutas.DetalleReceta>()
            DetalleRecetaScreen(
                authViewModel = authViewModel,
                recetaViewModel = recetaViewModel,
                recetaId = args.recetaId,
                onVolver = { navController.popBackStack() },
                onEditar = {
                    navController.navigate(Rutas.EditorReceta(args.recetaId))
                }
            )
        }
    }
}