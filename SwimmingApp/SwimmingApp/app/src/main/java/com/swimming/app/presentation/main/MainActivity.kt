package com.swimming.app.presentation.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.swimming.app.R
import com.swimming.app.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity principal de la aplicación.
 * Contiene la barra de navegación inferior y la barra superior con avatar y flecha de atrás.
 * Aloja un NavHostFragment que gestiona la navegación entre todos los fragments de la app.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /** Identificadores de las pantallas raíz donde NO se muestra la flecha de atrás. */
    private val pestañasRaiz = setOf(
        R.id.homeFragment,
        R.id.calendarioFragment,
        R.id.tiemposFragment,
        R.id.equipoFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarNavegacion()
        configurarBarraSuperior()
    }

    /**
     * Configura la BottomNavigationView con un listener personalizado que limpia
     * el backstack al pulsar una pestaña principal.
     *
     * Sin esta personalización, si el usuario navega Home → Perfil → Equipo → Home,
     * Android lo lleva de vuelta al Perfil en lugar de a Home, porque Perfil
     * sigue en la pila de navegación. Con esta solución, cada vez que se pulsa
     * una pestaña principal se limpia todo lo anterior hasta la raíz.
     */
    private fun configurarNavegacion() {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController

        // Listener personalizado de la BottomNav: limpia el backstack al pulsar una pestaña.
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            // Si ya estamos en esa pestaña, no hacemos nada para evitar recargas innecesarias.
            if (navController.currentDestination?.id == item.itemId) {
                return@setOnItemSelectedListener true
            }

            // Opciones de navegación: limpiamos el backstack hasta el destino raíz (Home)
            // y evitamos crear varias instancias de la misma pestaña.
            val opciones = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setPopUpTo(navController.graph.startDestinationId, false)
                .build()

            try {
                navController.navigate(item.itemId, null, opciones)
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }

        // Listener que mantiene iluminado el icono correcto de la BottomNav
        // cuando el usuario navega por programa (por ejemplo desde botones internos).
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val itemRaiz = pestañasRaiz.firstOrNull { it == destination.id }
            if (itemRaiz != null) {
                binding.bottomNavigation.menu.findItem(itemRaiz)?.isChecked = true
            }
        }
    }

    /**
     * Configura los botones de la barra superior:
     *   - Avatar: lleva al perfil del usuario.
     *   - Flecha atrás: vuelve a la pantalla anterior.
     * La flecha se oculta automáticamente en las pestañas raíz.
     */
    private fun configurarBarraSuperior() {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController

        // Avatar → ir al perfil
        binding.btnTopPerfil.setOnClickListener {
            navController.navigate(R.id.perfilFragment)
        }

        // Flecha atrás → volver a la pantalla anterior
        binding.btnAtras.setOnClickListener {
            navController.popBackStack()
        }

        // Listener que oculta o muestra la flecha según la pantalla actual.
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.btnAtras.visibility =
                if (destination.id in pestañasRaiz) View.GONE else View.VISIBLE
        }
    }
}