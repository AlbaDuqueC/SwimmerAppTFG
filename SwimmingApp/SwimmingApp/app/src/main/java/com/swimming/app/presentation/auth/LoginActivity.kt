package com.swimming.app.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swimming.app.R
import com.swimming.app.presentation.main.MainActivity
import com.swimming.app.ui.theme.SwimmingAppTheme
import com.swimming.app.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint

/**
 * Pantalla de inicio de sesión.
 * Permite al usuario introducir email y contraseña, o navegar al registro
 * para crear una cuenta nueva. Delega toda la lógica al LoginViewModel.
 */
@AndroidEntryPoint
class LoginActivity : ComponentActivity() {

    // ViewModel inyectado automáticamente por Hilt.
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SwimmingAppTheme {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginExitoso = {
                        // Navega a MainActivity y cierra esta Activity.
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    },
                    onIrARegistro = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    }
                )
            }
        }
    }
}

/**
 * Composable de la pantalla de login.
 * Observa el resultado del ViewModel y reacciona mostrando errores
 * o navegando al inicio según el resultado.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginExitoso: () -> Unit,
    onIrARegistro: () -> Unit
) {
    val context = LocalContext.current
    val resultado by viewModel.loginResult.observeAsState()

    // Estado local de los campos del formulario.
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Color azul marino de la marca.
    val azulMarca = Color(0xFF0A2A3D)

    // Reacciona a los cambios del resultado del ViewModel.
    LaunchedEffect(resultado) {
        when (resultado) {
            is NetworkResult.Success -> onLoginExitoso()
            is NetworkResult.Error -> Toast.makeText(
                context,
                (resultado as NetworkResult.Error).message,
                Toast.LENGTH_LONG
            ).show()
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEAF4FB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Botón de acceso al registro en la parte superior.
            TextButton(onClick = onIrARegistro) {
                Text(
                    text = "Crea tu cuenta de entrenador o de alumno aquí",
                    color = azulMarca,
                    fontSize = 13.sp
                )
            }

            // Logo de la aplicación.
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "Logo DolphinSwimmer",
                modifier = Modifier.size(120.dp)
            )

            // Campo de email.
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Usuario (email)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            // Campo de contraseña con icono para mostrar/ocultar.
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                trailingIcon = {
                    val icono = if (passwordVisible) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(icono, contentDescription = "Mostrar contraseña")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            // Botón de inicio de sesión o indicador de carga.
            if (resultado is NetworkResult.Loading) {
                CircularProgressIndicator(color = azulMarca)
            } else {
                Button(
                    onClick = {
                        if (email.isEmpty() || password.isEmpty()) {
                            Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.login(email.trim(), password.trim())
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = azulMarca)
                ) {
                    Text("Iniciar sesión", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
