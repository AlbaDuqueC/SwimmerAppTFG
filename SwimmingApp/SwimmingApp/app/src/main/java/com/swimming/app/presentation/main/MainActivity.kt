package com.swimming.app.presentation.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.swimming.app.R
import com.swimming.app.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /** Pantallas top-level: aquí ocultamos la flecha de atrás. */
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

    private fun configurarNavegacion() {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController
        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun configurarBarraSuperior() {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController

        // Avatar → ir al perfil
        binding.btnTopPerfil.setOnClickListener {
            navController.navigate(R.id.perfilFragment)
        }

        // Flecha atrás → popBackStack
        binding.btnAtras.setOnClickListener {
            navController.popBackStack()
        }

        // Mostrar la flecha solo en pantallas hijas
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.btnAtras.visibility =
                if (destination.id in pestañasRaiz) View.GONE else View.VISIBLE
        }
    }
}