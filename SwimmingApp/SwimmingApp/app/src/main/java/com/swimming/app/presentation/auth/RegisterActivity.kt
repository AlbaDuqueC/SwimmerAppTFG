package com.swimming.app.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.swimming.app.ui.theme.SwimmingAppTheme
import com.swimming.app.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint

/**
 * Pantalla de registro de nuevo usuario.
 * Permite elegir entre Nadador y Entrenador, valida los campos del formulario
 * y, si todo es correcto, delega la creación al RegisterViewModel.
 */
@AndroidEntryPoint
class RegisterActivity : ComponentActivity() {

    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SwimmingAppTheme {
                RegisterScreen(
                    viewModel = viewModel,
                    onRegistroExitoso = {
                        // Tras el registro exitoso, vuelve al login.
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

/**
 * Composable de la pantalla de registro.
 * Muestra el formulario con todos los campos necesarios para crear una cuenta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegistroExitoso: () -> Unit
) {
    val context = LocalContext.current
    val resultado by viewModel.registerResult.observeAsState()

    // Estado local de los campos del formulario.
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repetirPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var repetirVisible by remember { mutableStateOf(false) }

    // Estado del selector de rol (Nadador / Entrenador).
    var rolExpanded by remember { mutableStateOf(false) }
    var rolSeleccionado by remember { mutableStateOf("Nadador") }
    val rolesDisponibles = listOf("Nadador", "Entrenador")

    // Variable para controlar si el diálogo de confirmación está visible.
    var mostrarDialogo by remember { mutableStateOf(false) }

    val azulMarca = Color(0xFF0A2A3D)

    // Reacciona al resultado del ViewModel.
    LaunchedEffect(resultado) {
        when (resultado) {
            is NetworkResult.Success -> mostrarDialogo = true
            is NetworkResult.Error -> Toast.makeText(
                context,
                (resultado as NetworkResult.Error).message,
                Toast.LENGTH_LONG
            ).show()
            else -> {}
        }
    }

    // Diálogo de confirmación que aparece tras crear la cuenta con éxito.
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("¡Cuenta creada!") },
            text = {
                Text("Te hemos enviado un email de verificación. Pulsa el enlace del correo y luego inicia sesión.")
            },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    onRegistroExitoso()
                }) {
                    Text("Entendido")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Campo de nombre.
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Campo de apellidos.
        OutlinedTextField(
            value = apellidos,
            onValueChange = { apellidos = it },
            label = { Text("Apellidos") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Campo de correo electrónico.
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        // Campo de contraseña con icono de visibilidad.
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None
                                   else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Mostrar contraseña"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        // Campo de repetir contraseña.
        OutlinedTextField(
            value = repetirPassword,
            onValueChange = { repetirPassword = it },
            label = { Text("Repetir contraseña") },
            singleLine = true,
            visualTransformation = if (repetirVisible) VisualTransformation.None
                                   else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { repetirVisible = !repetirVisible }) {
                    Icon(
                        if (repetirVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Mostrar contraseña"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        // Selector de rol con ExposedDropdownMenu de Material3.
        ExposedDropdownMenuBox(
            expanded = rolExpanded,
            onExpandedChange = { rolExpanded = !rolExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = rolSeleccionado,
                onValueChange = {},
                readOnly = true,
                label = { Text("Rol") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rolExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = rolExpanded,
                onDismissRequest = { rolExpanded = false }
            ) {
                rolesDisponibles.forEach { rol ->
                    DropdownMenuItem(
                        text = { Text(rol) },
                        onClick = {
                            rolSeleccionado = rol
                            rolExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón de crear cuenta o indicador de carga.
        if (resultado is NetworkResult.Loading) {
            CircularProgressIndicator(color = azulMarca)
        } else {
            Button(
                onClick = {
                    when {
                        nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || password.isEmpty() ->
                            Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                        password != repetirPassword ->
                            Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                        else -> viewModel.registrar(
                            nombre.trim(), apellidos.trim(), email.trim(), password.trim(), rolSeleccionado
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = azulMarca)
            ) {
                Text("CREAR CUENTA", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
