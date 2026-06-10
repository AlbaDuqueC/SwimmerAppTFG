package com.swimming.app.presentation.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.swimming.app.R
import com.swimming.app.presentation.auth.LoginActivity
import com.swimming.app.presentation.main.MainActivity
import com.swimming.app.ui.theme.SwimmingAppTheme
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
class SplashActivity : ComponentActivity() {

    // Inyección directa de SessionManager para consultar si hay sesión iniciada.
    @Inject lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SwimmingAppTheme {
                SplashScreen()
            }
        }
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

/**
 * Composable de la pantalla splash.
 * Muestra el logo de la aplicación centrado y una barra de progreso en la parte inferior.
 */
@Composable
fun SplashScreen() {
    // Fondo azul marino de la marca de la aplicación.
    val azulMarca = Color(0xFF0A2A3D)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(azulMarca),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo de la aplicación.
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "Logo DolphinSwimmer",
                modifier = Modifier.size(160.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Nombre de la aplicación.
            Text(
                text = "DolphinSwimmer",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Barra de progreso en la parte inferior de la pantalla.
        LinearProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 48.dp),
            color = Color(0xFF29B6F6),
            trackColor = Color.White.copy(alpha = 0.2f)
        )
    }
}
