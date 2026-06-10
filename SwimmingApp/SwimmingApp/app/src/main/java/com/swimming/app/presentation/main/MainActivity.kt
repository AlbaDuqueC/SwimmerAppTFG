package com.swimming.app.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.swimming.app.presentation.calendario.CalendarioScreen
import com.swimming.app.presentation.equipo.*
import com.swimming.app.presentation.home.HomeScreen
import com.swimming.app.presentation.perfil.EditarPerfilScreen
import com.swimming.app.presentation.perfil.PerfilScreen
import com.swimming.app.presentation.tiempos.CrearTiempoScreen
import com.swimming.app.presentation.tiempos.CronometroScreen
import com.swimming.app.presentation.tiempos.TiemposScreen
import com.swimming.app.ui.theme.SwimmingAppTheme
import dagger.hilt.android.AndroidEntryPoint

// ────────────────────────────────────────────────────────────────────────────
// Rutas de navegación de la aplicación.
// Se usan string constantes en lugar de IDs de recurso como en Navigation XML.
// ────────────────────────────────────────────────────────────────────────────
object Rutas {
    // Pestañas principales de la BottomNavigationBar.
    const val HOME = "home"
    const val CALENDARIO = "calendario"
    const val TIEMPOS = "tiempos"
    const val EQUIPO = "equipo"

    // Pantallas secundarias accesibles desde las pestañas.
    const val PERFIL = "perfil"
    const val EDITAR_PERFIL = "editarPerfil"
    const val CREAR_TIEMPO = "crearTiempo"
    const val CRONOMETRO = "cronometro"
    const val CREAR_EQUIPO = "crearEquipo"
    const val CREAR_NADADOR = "crearNadador"
    const val INGRESAR_EQUIPO = "ingresarEquipo"
    const val IMPRIMIR = "imprimir"

    // Ruta con argumento para crear tiempo desde la vista de equipo.
    const val CREAR_TIEMPO_CON_ARG = "crearTiempo/{idNadadorEquipo}"
    const val CRONOMETRO_CON_ARG = "cronometro/{prueba}/{idNadadorEquipo}"
}

/**
 * Modelo de datos de cada elemento de la barra de navegación inferior.
 */
data class ItemBottomNav(
    val ruta: String,
    val icono: ImageVector,
    val etiqueta: String
)

/**
 * Activity principal de la aplicación.
 * Aloja el NavHost de Compose que gestiona la navegación entre todas las pantallas.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SwimmingAppTheme {
                MainScreen()
            }
        }
    }
}

/**
 * Composable raíz que construye el Scaffold con la barra superior,
 * la barra de navegación inferior y el NavHost con todas las rutas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val rutaActual by navController.currentBackStackEntryAsState()
    val destinoActual = rutaActual?.destination

    // Pestañas que forman la barra de navegación inferior.
    val pestañas = listOf(
        ItemBottomNav(Rutas.HOME,       Icons.Default.Home,       "Inicio"),
        ItemBottomNav(Rutas.CALENDARIO, Icons.Default.CalendarMonth, "Calendario"),
        ItemBottomNav(Rutas.TIEMPOS,    Icons.Default.Timer,      "Tiempos"),
        ItemBottomNav(Rutas.EQUIPO,     Icons.Default.Group,      "Equipo")
    )

    // Pantallas raíz donde NO se muestra la flecha de atrás.
    val rutasRaiz = setOf(Rutas.HOME, Rutas.CALENDARIO, Rutas.TIEMPOS, Rutas.EQUIPO)
    val rutaActualString = destinoActual?.route ?: ""
    val esRaiz = rutasRaiz.contains(rutaActualString)

    Scaffold(
        // Barra superior con avatar y flecha de atrás.
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    // La flecha de atrás solo se muestra en pantallas secundarias.
                    if (!esRaiz) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                        }
                    }
                },
                actions = {
                    // Avatar que navega al perfil del usuario.
                    IconButton(onClick = { navController.navigate(Rutas.PERFIL) }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
                    }
                }
            )
        },
        // Barra de navegación inferior con las 4 pestañas principales.
        bottomBar = {
            NavigationBar {
                pestañas.forEach { item ->
                    val seleccionado = destinoActual?.hierarchy?.any { it.route == item.ruta } == true
                    NavigationBarItem(
                        selected = seleccionado,
                        onClick = {
                            navController.navigate(item.ruta) {
                                // Limpia el backstack al navegar entre pestañas principales,
                                // igual que hacía el NavOptions del XML anterior.
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icono, contentDescription = item.etiqueta) },
                        label = { Text(item.etiqueta) }
                    )
                }
            }
        }
    ) { innerPadding ->
        // NavHost que contiene todas las pantallas de la aplicación.
        NavHost(
            navController = navController,
            startDestination = Rutas.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Pantallas raíz (pestañas principales).
            composable(Rutas.HOME) {
                HomeScreen(navController = navController)
            }
            composable(Rutas.CALENDARIO) {
                CalendarioScreen()
            }
            composable(Rutas.TIEMPOS) {
                TiemposScreen(navController = navController)
            }
            composable(Rutas.EQUIPO) {
                EquipoScreen(navController = navController)
            }

            // Pantallas secundarias.
            composable(Rutas.PERFIL) {
                PerfilScreen(navController = navController)
            }
            composable(Rutas.EDITAR_PERFIL) {
                EditarPerfilScreen(navController = navController)
            }

            // Crear tiempo sin argumento (flujo del nadador).
            composable(Rutas.CREAR_TIEMPO) {
                CrearTiempoScreen(navController = navController, idNadadorEquipoOverride = -1)
            }
            // Crear tiempo con argumento (flujo del entrenador asignando a un nadador).
            composable(Rutas.CREAR_TIEMPO_CON_ARG) { backStackEntry ->
                val idNadadorEquipo = backStackEntry.arguments?.getString("idNadadorEquipo")?.toIntOrNull() ?: -1
                CrearTiempoScreen(navController = navController, idNadadorEquipoOverride = idNadadorEquipo)
            }
            // Cronómetro con los datos de la prueba y el nadador.
            composable(Rutas.CRONOMETRO_CON_ARG) { backStackEntry ->
                val prueba = backStackEntry.arguments?.getString("prueba") ?: ""
                val idNadadorEquipo = backStackEntry.arguments?.getString("idNadadorEquipo")?.toIntOrNull() ?: -1
                CronometroScreen(navController = navController, prueba = prueba, idNadadorEquipoOverride = idNadadorEquipo)
            }

            // Pantallas de equipo.
            composable(Rutas.CREAR_EQUIPO) {
                CrearEquipoScreen(navController = navController)
            }
            composable(Rutas.CREAR_NADADOR) {
                CrearNadadorScreen(navController = navController)
            }
            composable(Rutas.INGRESAR_EQUIPO) {
                IngresarEquipoScreen(navController = navController)
            }
            composable(Rutas.IMPRIMIR) {
                ImprimirScreen(navController = navController)
            }
        }
    }
}
