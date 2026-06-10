package com.swimming.app.presentation.equipo

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.swimming.app.domain.model.NadadorEquipo
import com.swimming.app.presentation.main.Rutas
import com.swimming.app.utils.NetworkResult
import com.swimming.app.utils.SessionManager

// ────────────────────────────────────────────────────────────────────────────
// EquipoScreen
// ────────────────────────────────────────────────────────────────────────────

/**
 * Pantalla principal del equipo.
 * Adapta su interfaz según el rol del usuario:
 *   - Entrenador con equipo: cabecera con nombre, botones de gestión y lista de nadadores.
 *   - Entrenador sin equipo: solo muestra el botón de crear equipo.
 *   - Nadador sin equipo: redirige automáticamente a la pantalla de ingresar código.
 *   - Nadador con equipo: muestra la lista de nadadores del equipo.
 */
@Composable
fun EquipoScreen(
    navController: NavController,
    viewModel: EquipoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val esEntrenador = sessionManager.esEntrenador()
    val idEquipo = sessionManager.getEquipoId()

    val nadadores by viewModel.nadadores.observeAsState()
    val equipo by viewModel.equipo.observeAsState()
    val equipoActualizado by viewModel.equipoActualizado.observeAsState()
    val equipoEliminado by viewModel.equipoEliminado.observeAsState()
    val nadadorEliminado by viewModel.nadadorEliminado.observeAsState()
    val nadadorActualizado by viewModel.nadadorActualizado.observeAsState()

    val azulMarca = Color(0xFF0A2A3D)
    var nombreEquipoActual by remember { mutableStateOf("") }

    // Diálogos de confirmación.
    var mostrarDialogoEditarEquipo by remember { mutableStateOf(false) }
    var mostrarDialogoEliminarEquipo by remember { mutableStateOf(false) }
    var nadadorAEliminar by remember { mutableStateOf<NadadorEquipo?>(null) }
    var nadadorAEditar by remember { mutableStateOf<NadadorEquipo?>(null) }

    // Redirige al nadador sin equipo a la pantalla de ingresar código.
    LaunchedEffect(Unit) {
        if (!esEntrenador && idEquipo == null) {
            navController.navigate(Rutas.INGRESAR_EQUIPO)
        } else if (idEquipo != null) {
            viewModel.cargarNadadores(idEquipo)
            viewModel.cargarEquipo(idEquipo)
        }
    }

    // Actualiza el nombre del equipo cuando llegan los datos.
    LaunchedEffect(equipo) {
        if (equipo is NetworkResult.Success) {
            nombreEquipoActual = (equipo as NetworkResult.Success).data.nombre
        }
    }

    // Feedback de actualización del equipo.
    LaunchedEffect(equipoActualizado) {
        if (equipoActualizado is NetworkResult.Success) {
            nombreEquipoActual = (equipoActualizado as NetworkResult.Success).data.nombre
            Toast.makeText(context, "Nombre actualizado", Toast.LENGTH_SHORT).show()
        }
    }

    // Feedback de eliminación del equipo.
    LaunchedEffect(equipoEliminado) {
        if (equipoEliminado is NetworkResult.Success) {
            Toast.makeText(context, "Equipo eliminado", Toast.LENGTH_SHORT).show()
            sessionManager.borrarEquipoId()
        }
    }

    // Feedback de eliminación de nadador.
    LaunchedEffect(nadadorEliminado) {
        if (nadadorEliminado is NetworkResult.Success) {
            Toast.makeText(context, "Nadador eliminado", Toast.LENGTH_SHORT).show()
            idEquipo?.let {
                viewModel.cargarNadadores(it)
                viewModel.cargarEquipo(it)
            }
        }
    }

    // Feedback de actualización de nadador.
    LaunchedEffect(nadadorActualizado) {
        if (nadadorActualizado is NetworkResult.Success) {
            Toast.makeText(context, "Nadador actualizado", Toast.LENGTH_SHORT).show()
            idEquipo?.let { viewModel.cargarNadadores(it) }
        }
    }

    // ── Diálogo para editar el nombre del equipo ─────────────────────────────
    if (mostrarDialogoEditarEquipo) {
        var nuevoNombre by remember { mutableStateOf(nombreEquipoActual) }
        AlertDialog(
            onDismissRequest = { mostrarDialogoEditarEquipo = false },
            title = { Text("Editar nombre del equipo") },
            text = {
                OutlinedTextField(
                    value = nuevoNombre,
                    onValueChange = { nuevoNombre = it },
                    label = { Text("Nombre del equipo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nuevoNombre.trim().isEmpty()) {
                        Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                    } else {
                        idEquipo?.let { viewModel.actualizarNombreEquipo(it, nuevoNombre.trim()) }
                        mostrarDialogoEditarEquipo = false
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEditarEquipo = false }) { Text("Cancelar") }
            }
        )
    }

    // ── Diálogo de confirmación para eliminar el equipo ───────────────────────
    if (mostrarDialogoEliminarEquipo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminarEquipo = false },
            title = { Text("Eliminar equipo") },
            text = { Text("¿Seguro que quieres eliminar el equipo \"$nombreEquipoActual\"? Los nadadores quedarán desvinculados.") },
            confirmButton = {
                TextButton(onClick = {
                    idEquipo?.let { viewModel.eliminarEquipo(it) }
                    mostrarDialogoEliminarEquipo = false
                }) { Text("Eliminar", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminarEquipo = false }) { Text("Cancelar") }
            }
        )
    }

    // ── Diálogo de confirmación para eliminar un nadador ──────────────────────
    nadadorAEliminar?.let { nadador ->
        AlertDialog(
            onDismissRequest = { nadadorAEliminar = null },
            title = { Text("Eliminar nadador") },
            text = { Text("¿Seguro que quieres eliminar a ${nadador.nombre} ${nadador.apellidos}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarNadador(nadador.idNadadorEquipo)
                    nadadorAEliminar = null
                }) { Text("Eliminar", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { nadadorAEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    // ── Diálogo para editar nombre y apellidos de un nadador ──────────────────
    nadadorAEditar?.let { nadador ->
        var nuevoNombre by remember { mutableStateOf(nadador.nombre) }
        var nuevosApellidos by remember { mutableStateOf(nadador.apellidos) }
        AlertDialog(
            onDismissRequest = { nadadorAEditar = null },
            title = { Text("Editar nadador") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nuevoNombre,
                        onValueChange = { nuevoNombre = it },
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nuevosApellidos,
                        onValueChange = { nuevosApellidos = it },
                        label = { Text("Apellidos") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nuevoNombre.trim().isEmpty() || nuevosApellidos.trim().isEmpty()) {
                        Toast.makeText(context, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                    } else {
                        idEquipo?.let {
                            viewModel.actualizarNadador(
                                nadador.idNadadorEquipo, nuevoNombre.trim(), nuevosApellidos.trim(), it
                            )
                        }
                        nadadorAEditar = null
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { nadadorAEditar = null }) { Text("Cancelar") }
            }
        )
    }

    // ── Contenido principal ───────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Vista del entrenador sin equipo.
        if (esEntrenador && idEquipo == null) {
            Button(
                onClick = { navController.navigate(Rutas.CREAR_EQUIPO) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = azulMarca)
            ) {
                Text("Crear equipo", color = Color.White)
            }
            return@Column
        }

        // Cabecera del equipo (solo entrenador con equipo).
        if (esEntrenador && idEquipo != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nombreEquipoActual,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = azulMarca,
                    modifier = Modifier.weight(1f)
                )
                // Botón editar nombre del equipo.
                IconButton(onClick = { mostrarDialogoEditarEquipo = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar equipo", tint = azulMarca)
                }
                // Botón eliminar equipo.
                IconButton(onClick = { mostrarDialogoEliminarEquipo = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar equipo", tint = Color.Red)
                }
            }

            // Botones de añadir nadador e imprimir.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { navController.navigate(Rutas.CREAR_NADADOR) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = azulMarca)
                ) {
                    Text("+ Nadador", color = Color.White)
                }
                OutlinedButton(
                    onClick = { navController.navigate(Rutas.IMPRIMIR) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Imprimir")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Lista de nadadores del equipo.
        val lista = if (nadadores is NetworkResult.Success)
            (nadadores as NetworkResult.Success<List<NadadorEquipo>>).data
        else emptyList()

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(lista, key = { it.idNadadorEquipo }) { nadador ->
                ItemNadador(
                    nadador = nadador,
                    esEntrenador = esEntrenador,
                    onClick = {
                        if (esEntrenador) {
                            navController.navigate("${Rutas.CREAR_TIEMPO}/${nadador.idNadadorEquipo}")
                        }
                    },
                    onEditar = { nadadorAEditar = nadador },
                    onEliminar = { nadadorAEliminar = nadador }
                )
            }
        }
    }
}

/**
 * Composable de cada fila de nadador en la lista del equipo.
 */
@Composable
private fun ItemNadador(
    nadador: NadadorEquipo,
    esEntrenador: Boolean,
    onClick: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val azulMarca = Color(0xFF0A2A3D)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = esEntrenador, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Círculo con la inicial del nombre.
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(azulMarca, shape = MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nadador.nombre.firstOrNull()?.uppercase() ?: "N",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("${nadador.nombre} ${nadador.apellidos}", fontWeight = FontWeight.SemiBold)
            Text("Código: ${nadador.codigo}", fontSize = 12.sp, color = Color.Gray)
        }
        // Botones de editar y eliminar (solo para entrenadores).
        if (esEntrenador) {
            IconButton(onClick = onEditar) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = azulMarca)
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// CrearEquipoScreen
// ────────────────────────────────────────────────────────────────────────────

/**
 * Pantalla para crear un nuevo equipo. Solo accesible para entrenadores.
 */
@Composable
fun CrearEquipoScreen(
    navController: NavController,
    viewModel: EquipoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val resultado by viewModel.equipoCreado.observeAsState()
    var nombre by remember { mutableStateOf("") }
    val azulMarca = Color(0xFF0A2A3D)

    LaunchedEffect(resultado) {
        if (resultado is NetworkResult.Success) {
            sessionManager.guardarEquipoId((resultado as NetworkResult.Success).data.id)
            Toast.makeText(context, "Equipo creado con éxito", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del equipo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                if (nombre.trim().isEmpty()) {
                    Toast.makeText(context, "Escribe el nombre del equipo", Toast.LENGTH_SHORT).show()
                } else {
                    val idEntrenador = sessionManager.getUserId().takeIf { it != -1 }
                    viewModel.crearNuevoEquipo(nombre.trim(), idEntrenador)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = azulMarca)
        ) {
            Text("Crear equipo", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// CrearNadadorScreen
// ────────────────────────────────────────────────────────────────────────────

/**
 * Pantalla para crear una ficha de NadadorEquipo dentro del equipo del entrenador.
 */
@Composable
fun CrearNadadorScreen(
    navController: NavController,
    viewModel: EquipoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val resultado by viewModel.nadadorCreado.observeAsState()
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    val azulMarca = Color(0xFF0A2A3D)

    LaunchedEffect(resultado) {
        if (resultado is NetworkResult.Success) {
            Toast.makeText(context, "Nadador creado con éxito", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = apellidos,
            onValueChange = { apellidos = it },
            label = { Text("Apellidos") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                val idEquipo = sessionManager.getEquipoId()
                when {
                    nombre.trim().isEmpty() || apellidos.trim().isEmpty() ->
                        Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                    idEquipo == null ->
                        Toast.makeText(context, "No tienes un equipo asignado", Toast.LENGTH_SHORT).show()
                    else -> viewModel.crearNuevoNadador(nombre.trim(), apellidos.trim(), idEquipo)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = azulMarca)
        ) {
            Text("Crear nadador", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// IngresarEquipoScreen
// ────────────────────────────────────────────────────────────────────────────

/**
 * Pantalla donde el nadador introduce el código de 6 dígitos para unirse al equipo.
 */
@Composable
fun IngresarEquipoScreen(
    navController: NavController,
    viewModel: IngresarEquipoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val resultado by viewModel.resultado.observeAsState()
    var codigo by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    val azulMarca = Color(0xFF0A2A3D)

    LaunchedEffect(resultado) {
        when (resultado) {
            is NetworkResult.Success -> {
                val nadador = (resultado as NetworkResult.Success).data
                sessionManager.guardarIdNadadorEquipo(nadador.idNadadorEquipo ?: -1)
                nadador.idEquipo?.let { sessionManager.guardarEquipoId(it) }
                Toast.makeText(context, "Te has unido al equipo correctamente", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            is NetworkResult.Error -> {
                cargando = false
                Toast.makeText(context, (resultado as NetworkResult.Error).message, Toast.LENGTH_LONG).show()
            }
            is NetworkResult.Loading -> cargando = true
            else -> {}
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = codigo,
            onValueChange = { if (it.length <= 6) codigo = it },
            label = { Text("Código del equipo (6 dígitos)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (cargando) {
            CircularProgressIndicator(color = azulMarca)
        } else {
            Button(
                onClick = {
                    val codigoInt = codigo.toIntOrNull()
                    val idNadador = sessionManager.getIdNadador()
                    when {
                        codigoInt == null || codigo.length != 6 ->
                            Toast.makeText(context, "El código debe tener 6 dígitos", Toast.LENGTH_SHORT).show()
                        idNadador == -1 ->
                            Toast.makeText(context, "Sesión inválida. Vuelve a iniciar sesión.", Toast.LENGTH_SHORT).show()
                        else -> {
                            cargando = true
                            viewModel.vincular(idNadador, codigoInt)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = azulMarca)
            ) {
                Text("Ingresar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// ImprimirScreen
// ────────────────────────────────────────────────────────────────────────────

/**
 * Pantalla de impresión. Muestra todas las marcas del equipo.
 */
@Composable
fun ImprimirScreen(
    navController: NavController,
    viewModel: ImprimirViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val resultado by viewModel.marcas.observeAsState()
    val azulMarca = Color(0xFF0A2A3D)

    val marcas = if (resultado is NetworkResult.Success)
        (resultado as NetworkResult.Success).data
    else emptyList()

    LaunchedEffect(Unit) {
        sessionManager.getEquipoId()?.let { viewModel.cargarMarcas(it) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = { Toast.makeText(context, "Preparando impresión...", Toast.LENGTH_SHORT).show() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = azulMarca)
        ) {
            Text("Imprimir", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(marcas, key = { it.id }) { marca ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(marca.descripcion, modifier = Modifier.weight(1f))
                    Text(
                        text = marca.tiempo ?: "",
                        color = azulMarca,
                        fontWeight = FontWeight.Bold
                    )
                }
                Divider()
            }
        }
    }
}
