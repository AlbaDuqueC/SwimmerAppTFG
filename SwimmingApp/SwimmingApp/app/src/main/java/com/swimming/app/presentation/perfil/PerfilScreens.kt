package com.swimming.app.presentation.perfil

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.swimming.app.presentation.splash.SplashActivity
import com.swimming.app.utils.NetworkResult
import com.swimming.app.utils.SessionManager

// ────────────────────────────────────────────────────────────────────────────
// PerfilScreen
// ────────────────────────────────────────────────────────────────────────────

/**
 * Pantalla de perfil del usuario.
 * Muestra los datos básicos guardados en la sesión local
 * y permite editar el perfil o cerrar sesión.
 */
@Composable
fun PerfilScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val azulMarca = Color(0xFF0A2A3D)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Perfil del usuario",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = azulMarca
        )

        // Icono de avatar del usuario.
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Avatar",
            modifier = Modifier.size(80.dp),
            tint = azulMarca
        )

        // Nombre completo del usuario.
        Text(
            text = "${sessionManager.getUserNombre()} ${sessionManager.getUserApellidos()}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = azulMarca
        )

        // Email del usuario.
        Text(
            text = sessionManager.getUserEmail(),
            color = Color.Gray,
            fontSize = 14.sp
        )

        // Rol del usuario.
        Text(
            text = sessionManager.getUserRol(),
            fontWeight = FontWeight.SemiBold,
            color = azulMarca
        )

        // Equipo al que pertenece (o "Sin equipo" si no tiene).
        Text(
            text = "Equipo: ${sessionManager.getEquipoId() ?: "Sin equipo"}",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Botón de editar perfil.
        Button(
            onClick = { navController.navigate("editarPerfil") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = azulMarca)
        ) {
            Text("Editar perfil", color = Color.White)
        }

        // Botón de cerrar sesión.
        OutlinedButton(
            onClick = {
                // Se borran los datos de sesión y se reinicia la app desde el splash.
                sessionManager.cerrarSesion()
                val intent = Intent(context, SplashActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión", color = Color.Red)
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// EditarPerfilScreen
// ────────────────────────────────────────────────────────────────────────────

/**
 * Pantalla de edición del perfil del usuario.
 * Permite actualizar el nombre y los apellidos, o eliminar la cuenta por completo.
 */
@Composable
fun EditarPerfilScreen(
    navController: NavController,
    viewModel: EditarPerfilViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val actualizacion by viewModel.actualizacionResult.observeAsState()
    val eliminacion by viewModel.eliminacionResult.observeAsState()

    var nombre by remember { mutableStateOf(sessionManager.getUserNombre()) }
    var apellidos by remember { mutableStateOf(sessionManager.getUserApellidos()) }
    var guardandо by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    val azulMarca = Color(0xFF0A2A3D)

    // Reacciona al resultado de la actualización.
    LaunchedEffect(actualizacion) {
        when (actualizacion) {
            is NetworkResult.Success -> {
                Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            is NetworkResult.Error -> {
                guardandо = false
                Toast.makeText(context, (actualizacion as NetworkResult.Error).message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    // Reacciona al resultado de la eliminación de cuenta.
    LaunchedEffect(eliminacion) {
        if (eliminacion is NetworkResult.Success) {
            // Cierra sesión y vuelve al splash.
            sessionManager.cerrarSesion()
            val intent = Intent(context, SplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        } else if (eliminacion is NetworkResult.Error) {
            Toast.makeText(context, (eliminacion as NetworkResult.Error).message, Toast.LENGTH_SHORT).show()
        }
    }

    // Diálogo de confirmación para eliminar la cuenta.
    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("Eliminar cuenta") },
            text = { Text("¿Estás seguro? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarCuenta(sessionManager.getUserId(), sessionManager.esEntrenador())
                    mostrarDialogoEliminar = false
                }) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) { Text("Cancelar") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Editar perfil",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = azulMarca
        )

        // Campo de nombre con el valor actual de la sesión.
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Campo de apellidos con el valor actual de la sesión.
        OutlinedTextField(
            value = apellidos,
            onValueChange = { apellidos = it },
            label = { Text("Apellidos") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Botón de guardar cambios o indicador de carga.
        if (guardandо) {
            CircularProgressIndicator(color = azulMarca)
        } else {
            Button(
                onClick = {
                    if (nombre.trim().isEmpty() || apellidos.trim().isEmpty()) {
                        Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                    } else {
                        guardandо = true
                        viewModel.actualizarPerfil(
                            sessionManager.getUserId(),
                            nombre.trim(),
                            apellidos.trim(),
                            sessionManager.esEntrenador()
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = azulMarca)
            ) {
                Text("Aceptar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Botón de eliminar cuenta.
        TextButton(
            onClick = { mostrarDialogoEliminar = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Eliminar cuenta", color = Color.Red)
        }
    }
}
