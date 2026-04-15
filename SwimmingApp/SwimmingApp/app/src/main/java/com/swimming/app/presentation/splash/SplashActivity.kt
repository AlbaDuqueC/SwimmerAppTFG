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

//Pantalla de carga
@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject lateinit var sessionManager: SessionManager
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navegarDespuesDeRetraso()
    }

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
