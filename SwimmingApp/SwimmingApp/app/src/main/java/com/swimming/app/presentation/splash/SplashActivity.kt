package com.swimming.app.presentation.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.swimming.app.databinding.ActivitySplashBinding
import com.swimming.app.presentation.auth.LoginActivity
import com.swimming.app.presentation.main.MainActivity
import com.swimming.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Pantalla de carga inicial (splash) que se muestra al abrir la app.
 * Tras un breve retardo, redirige al usuario:
 *   - A MainActivity si ya hay una sesión activa.
 *   - A LoginActivity en caso contrario.
 */
@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    // Inyección directa de SessionManager para consultar si hay sesión iniciada.
    @Inject lateinit var sessionManager: SessionManager
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navegarDespuesDeRetraso()
    }

    /**
     * Espera 2 segundos y navega a la siguiente pantalla.
     * Permite que el usuario vea la marca de la aplicación durante un instante.
     */
    private fun navegarDespuesDeRetraso() {
        Handler(Looper.getMainLooper()).postDelayed({
            val destino = if (sessionManager.haySession()) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
            startActivity(destino)
            finish()
        }, 2000)
    }
}