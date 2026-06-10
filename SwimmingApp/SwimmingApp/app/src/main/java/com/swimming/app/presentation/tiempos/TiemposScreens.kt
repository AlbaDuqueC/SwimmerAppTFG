package com.swimming.app.presentation.tiempos

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.swimming.app.domain.model.MarcaDeTiempo
import com.swimming.app.presentation.main.Rutas
import com.swimming.app.utils.NetworkResult
import com.swimming.app.utils.SessionManager
import dagger.hilt.android.EntryPointAccessors
import androidx.compose.ui.platform.LocalContext
import com.swimming.app.utils.TiempoFormatter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// ────────────────────────────────────────────────────────────────────────────
// TiemposScreen
// ────────────────────────────────────────────────────────────────────────────

/**
 * Pantalla de tiempos. Muestra dos secciones separadas (mías / asignadas por entrenador)
 * y permite eliminar marcas con un botón de papelera.
 */
@Composable
fun TiemposScreen(
    navController: NavController,
    viewModel: TiemposViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val esEntrenador = sessionManager.esEntrenador()

    val marcasMias by viewModel.marcasMias.observeAsState()
    val marcasEntrenador by viewModel.marcasEntrenador.observeAsState(emptyList())
    val marcaEliminada by viewModel.marcaEliminada.observeAsState()

    val azulMarca = Color(0xFF0A2A3D)

    // Marcas a mostrar calculadas según el resultado del ViewModel.
    val listaMias = if (marcasMias is NetworkResult.Success)
        (marcasMias as NetworkResult.Success<List<MarcaDeTiempo>>).data.orEmpty()
    else emptyList()

    // Diálogo de confirmación de eliminación.
    var marcaAEliminar by remember { mutableStateOf<MarcaDeTiempo?>(null) }

    // Recarga las marcas al entrar a la pantalla.
    LaunchedEffect(Unit) { viewModel.cargarMarcas() }

    // Muestra feedback de la eliminación.
    LaunchedEffect(marcaEliminada) {
        when (marcaEliminada) {
            is NetworkResult.Success ->
                Toast.makeText(context, "Marca eliminada", Toast.LENGTH_SHORT).show()
            is NetworkResult.Error ->
                Toast.makeText(context, (marcaEliminada as NetworkResult.Error).message, Toast.LENGTH_SHORT).show()
            else -> {}
        }
    }

    // Diálogo de confirmación antes de eliminar.
    marcaAEliminar?.let { marca ->
        AlertDialog(
            onDismissRequest = { marcaAEliminar = null },
            title = { Text("Eliminar marca") },
            text = { Text("¿Seguro que quieres eliminar esta marca?\n\n${marca.descripcion}") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarMarca(marca.id)
                    marcaAEliminar = null
                }) { Text("Eliminar", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { marcaAEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        // El FAB de añadir marca solo aparece para nadadores.
        floatingActionButton = {
            if (!esEntrenador) {
                FloatingActionButton(
                    onClick = { navController.navigate(Rutas.CREAR_TIEMPO) },
                    containerColor = azulMarca
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir marca", tint = Color.White)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Sección "Mis marcas" o "Tiempos del equipo" según el rol.
            if (listaMias.isNotEmpty()) {
                item {
                    Text(
                        text = if (esEntrenador) "Tiempos del equipo" else "Mis marcas",
                        fontWeight = FontWeight.Bold,
                        color = azulMarca,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                items(listaMias, key = { it.id }) { marca ->
                    ItemMarca(marca = marca, onEliminar = { marcaAEliminar = marca })
                }
            }

            // Sección de marcas asignadas por el entrenador (solo visible para nadadores).
            if (!esEntrenador && marcasEntrenador.isNotEmpty()) {
                item {
                    Text(
                        text = "Asignadas por mi entrenador",
                        fontWeight = FontWeight.Bold,
                        color = azulMarca,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                items(marcasEntrenador, key = { "ent_${it.id}" }) { marca ->
                    ItemMarca(marca = marca, onEliminar = { marcaAEliminar = marca })
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

/**
 * Composable de cada fila de la lista de marcas de tiempo.
 */
@Composable
private fun ItemMarca(marca: MarcaDeTiempo, onEliminar: () -> Unit) {
    val azulMarca = Color(0xFF0A2A3D)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Círculo con la letra inicial del estilo.
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(azulMarca, shape = MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = marca.descripcion.firstOrNull()?.uppercase() ?: "M",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        // Descripción de la marca.
        Text(
            text = marca.descripcion,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp
        )
        // Tiempo formateado.
        Text(
            text = marca.tiempo ?: "",
            fontSize = 14.sp,
            color = azulMarca,
            fontWeight = FontWeight.Bold
        )
        // Botón de eliminar.
        IconButton(onClick = onEliminar) {
            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// CrearTiempoScreen
// ────────────────────────────────────────────────────────────────────────────

/**
 * Pantalla para crear una nueva marca de tiempo.
 * Permite elegir entre cronómetro o introducción manual del tiempo.
 */
@Composable
fun CrearTiempoScreen(
    navController: NavController,
    idNadadorEquipoOverride: Int,
    viewModel: CronometroViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val resultado by viewModel.marcaGuardada.observeAsState()

    var estilo by remember { mutableStateOf("") }
    var metros by remember { mutableStateOf("") }
    var usarCronometro by remember { mutableStateOf(true) }
    var tiempoManual by remember { mutableStateOf("") }

    val azulMarca = Color(0xFF0A2A3D)

    // Navega al destino correcto tras guardar la marca.
    LaunchedEffect(resultado) {
        when (resultado) {
            is NetworkResult.Success -> {
                Toast.makeText(context, "Marca guardada", Toast.LENGTH_SHORT).show()
                val destino = if (idNadadorEquipoOverride != -1) Rutas.EQUIPO else Rutas.TIEMPOS
                navController.popBackStack(destino, false)
            }
            is NetworkResult.Error ->
                Toast.makeText(context, (resultado as NetworkResult.Error).message, Toast.LENGTH_LONG).show()
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Campo de estilo (Crol, Espalda...).
        OutlinedTextField(
            value = estilo,
            onValueChange = { estilo = it },
            label = { Text("Estilo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Campo de metros (50, 100, 200...).
        OutlinedTextField(
            value = metros,
            onValueChange = { metros = it },
            label = { Text("Metros") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Selector de modo: cronómetro o manual.
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = usarCronometro, onClick = { usarCronometro = true })
            Text("Cronómetro", modifier = Modifier.padding(end = 16.dp))
            RadioButton(selected = !usarCronometro, onClick = { usarCronometro = false })
            Text("Introducir tiempo")
        }

        // Campo de tiempo manual (solo visible en modo manual).
        if (!usarCronometro) {
            OutlinedTextField(
                value = tiempoManual,
                onValueChange = { tiempoManual = it },
                label = { Text("Tiempo (mm:ss o mm:ss.cc)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Button(
            onClick = {
                if (estilo.isEmpty() || metros.isEmpty()) {
                    Toast.makeText(context, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val descripcion = "$estilo - ${metros}m"
                if (usarCronometro) {
                    // Navega al cronómetro pasando los datos por la ruta.
                    val pruebaEncoded = URLEncoder.encode(descripcion, StandardCharsets.UTF_8.toString())
                    navController.navigate("cronometro/$pruebaEncoded/$idNadadorEquipoOverride")
                } else {
                    val tiempoNormalizado = normalizarTiempo(tiempoManual)
                    if (tiempoNormalizado == null) {
                        Toast.makeText(
                            context,
                            "Formato no válido. Usa mm:ss, mm:ss.cc o mm:ss.mmm",
                            Toast.LENGTH_LONG
                        ).show()
                        return@Button
                    }
                    viewModel.descripcion = descripcion
                    viewModel.guardarMarca(tiempoNormalizado, idNadadorEquipoOverride.takeIf { it != -1 })
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = azulMarca)
        ) {
            Text("Continuar", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// CronometroScreen
// ────────────────────────────────────────────────────────────────────────────

/**
 * Pantalla del cronómetro para medir y guardar tiempos en directo.
 * Permite iniciar, pausar, reanudar y resetear, y guardar la marca cuando el usuario decide.
 */
@Composable
fun CronometroScreen(
    navController: NavController,
    prueba: String,
    idNadadorEquipoOverride: Int,
    viewModel: CronometroViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val resultado by viewModel.marcaGuardada.observeAsState()

    // Estado del cronómetro en Compose.
    var corriendo by remember { mutableStateOf(false) }
    var tiempoAcumulado by remember { mutableStateOf(0L) }
    var tiempoInicio by remember { mutableStateOf(0L) }
    var displayMs by remember { mutableStateOf(0L) }

    val azulMarca = Color(0xFF0A2A3D)

    // Actualiza el display cada 10 ms usando un Handler en un side effect.
    DisposableEffect(corriendo) {
        val handler = Handler(Looper.getMainLooper())
        val actualizador = object : Runnable {
            override fun run() {
                if (corriendo) {
                    displayMs = tiempoAcumulado + (System.currentTimeMillis() - tiempoInicio)
                    handler.postDelayed(this, 10)
                }
            }
        }
        if (corriendo) handler.post(actualizador)
        onDispose { handler.removeCallbacks(actualizador) }
    }

    // Navega al destino correcto tras guardar la marca.
    LaunchedEffect(resultado) {
        when (resultado) {
            is NetworkResult.Success -> {
                Toast.makeText(context, "Marca guardada", Toast.LENGTH_SHORT).show()
                val destino = if (idNadadorEquipoOverride != -1) Rutas.EQUIPO else Rutas.TIEMPOS
                navController.popBackStack(destino, false)
            }
            is NetworkResult.Error ->
                Toast.makeText(context, (resultado as NetworkResult.Error).message, Toast.LENGTH_LONG).show()
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Nombre de la prueba.
        Text(
            text = prueba.ifEmpty { "Prueba" },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = azulMarca
        )

        // Display del tiempo en formato MM:SS.
        val minutos = (displayMs / 60000) % 60
        val segundos = (displayMs / 1000) % 60
        Text(
            text = String.format("%02d:%02d", minutos, segundos),
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = azulMarca
        )

        // Botones de control del cronómetro.
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Botón Play/Pausa.
            Button(
                onClick = {
                    if (corriendo) {
                        tiempoAcumulado += System.currentTimeMillis() - tiempoInicio
                        corriendo = false
                    } else {
                        tiempoInicio = System.currentTimeMillis()
                        corriendo = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = azulMarca)
            ) {
                Text(if (corriendo) "Pausar" else "Iniciar", color = Color.White)
            }

            // Botón Reset.
            OutlinedButton(
                onClick = {
                    corriendo = false
                    tiempoAcumulado = 0L
                    displayMs = 0L
                }
            ) {
                Text("Resetear")
            }
        }

        // Botón Guardar marca.
        Button(
            onClick = {
                if (tiempoAcumulado == 0L && !corriendo) {
                    Toast.makeText(context, "Arranca el cronómetro primero", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                // Si está corriendo, lo para antes de guardar.
                if (corriendo) {
                    tiempoAcumulado += System.currentTimeMillis() - tiempoInicio
                    corriendo = false
                    displayMs = tiempoAcumulado
                }
                val ms = tiempoAcumulado
                val horas = ms / 3600000
                val mins = (ms / 60000) % 60
                val segs = (ms / 1000) % 60
                val cents = (ms / 10) % 100
                val tiempo = String.format("%02d:%02d:%02d.%02d", horas, mins, segs, cents)
                viewModel.descripcion = prueba.ifEmpty { "Entrenamiento" }
                viewModel.guardarMarca(tiempo, idNadadorEquipoOverride.takeIf { it != -1 })
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF29B6F6))
        ) {
            Text("Guardar marca", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// Función de normalización de tiempo (compartida entre CrearTiempo y Cronómetro)
// ────────────────────────────────────────────────────────────────────────────

/**
 * Convierte una entrada del usuario al formato completo "HH:MM:SS.fff" que espera la API.
 * Devuelve null si el formato no es válido.
 */
fun normalizarTiempo(entrada: String): String? {
    if (entrada.isEmpty()) return null
    val partes = entrada.split(":")
    return when (partes.size) {
        2 -> {
            val minutos = partes[0].toIntOrNull() ?: return null
            val segundosStr = partes[1]
            if (segundosStr.toDoubleOrNull() == null) return null
            val (segundos, milisegundos) = if (segundosStr.contains(".")) {
                val (segParte, decParte) = segundosStr.split(".")
                val ms = when {
                    decParte.length == 1 -> decParte + "00"
                    decParte.length == 2 -> decParte + "0"
                    decParte.length >= 3 -> decParte.take(3)
                    else -> "000"
                }
                segParte.padStart(2, '0') to ms
            } else {
                segundosStr.padStart(2, '0') to "000"
            }
            "00:%02d:%s.%s".format(minutos, segundos, milisegundos)
        }
        3 -> entrada
        else -> null
    }
}
