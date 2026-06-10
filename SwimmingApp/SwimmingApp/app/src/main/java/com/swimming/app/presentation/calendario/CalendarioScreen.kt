package com.swimming.app.presentation.calendario

import android.widget.CalendarView
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.swimming.app.utils.NetworkResult
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla del calendario donde el usuario puede crear eventos (rutinas)
 * seleccionando una fecha y una hora del día.
 *
 * Nota: CalendarView es un componente de Android View que no existe en Compose.
 * Se integra con AndroidView, que permite embeber vistas Android clásicas
 * dentro de un árbol de Compose.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioScreen(
    viewModel: CalendarioViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val resultado by viewModel.eventoCreado.observeAsState()

    // Estado para controlar si el diálogo de crear evento está visible.
    var mostrarDialogo by remember { mutableStateOf(false) }
    // Fecha seleccionada en el calendario.
    var añoSeleccionado by remember { mutableStateOf(0) }
    var mesSeleccionado by remember { mutableStateOf(0) }
    var diaSeleccionado by remember { mutableStateOf(0) }

    // Recarga las rutinas al éxito y muestra feedback.
    LaunchedEffect(resultado) {
        when (resultado) {
            is NetworkResult.Success -> {
                Toast.makeText(context, "Evento creado", Toast.LENGTH_SHORT).show()
                viewModel.cargarRutinas()
            }
            is NetworkResult.Error -> Toast.makeText(
                context,
                (resultado as NetworkResult.Error).message,
                Toast.LENGTH_SHORT
            ).show()
            else -> {}
        }
    }

    // Carga inicial de las rutinas.
    LaunchedEffect(Unit) {
        viewModel.cargarRutinas()
    }

    // Diálogo para crear un nuevo evento sobre el día seleccionado.
    if (mostrarDialogo) {
        DialogCrearEvento(
            año = añoSeleccionado,
            mes = mesSeleccionado,
            dia = diaSeleccionado,
            onGuardar = { contenido, fechaIso ->
                viewModel.crearEvento(contenido, fechaIso)
                mostrarDialogo = false
            },
            onCancelar = { mostrarDialogo = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // CalendarView embebido con AndroidView porque Compose no tiene uno nativo.
        AndroidView(
            factory = { ctx ->
                CalendarView(ctx).apply {
                    // Al seleccionar una fecha, guardamos los valores y abrimos el diálogo.
                    setOnDateChangeListener { _, year, month, dayOfMonth ->
                        añoSeleccionado = year
                        mesSeleccionado = month
                        diaSeleccionado = dayOfMonth
                        mostrarDialogo = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )

        Text(
            text = "Toca un día para añadir un evento",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

/**
 * Diálogo que permite al usuario escribir el título del evento
 * y seleccionar la hora mediante un TimePicker.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogCrearEvento(
    año: Int,
    mes: Int,
    dia: Int,
    onGuardar: (contenido: String, fechaIso: String) -> Unit,
    onCancelar: () -> Unit
) {
    val context = LocalContext.current
    var contenido by remember { mutableStateOf("") }
    // Hora seleccionada por defecto: 18:00.
    var hora by remember { mutableStateOf(18) }
    var minuto by remember { mutableStateOf(0) }

    val fechaTexto = "%02d/%02d/%d".format(dia, mes + 1, año)

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Nuevo evento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Fecha seleccionada (solo lectura).
                Text("Fecha: $fechaTexto")

                // Campo de texto para el título del evento.
                OutlinedTextField(
                    value = contenido,
                    onValueChange = { contenido = it },
                    label = { Text("Título del evento") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Botón que abre el TimePicker nativo de Android.
                OutlinedButton(
                    onClick = {
                        android.app.TimePickerDialog(
                            context,
                            { _, h, m ->
                                hora = h
                                minuto = m
                            },
                            hora, minuto, true
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Hora: %02d:%02d".format(hora, minuto))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (contenido.isEmpty()) {
                    Toast.makeText(context, "Escribe un título", Toast.LENGTH_SHORT).show()
                    return@TextButton
                }
                // Construye la fecha en formato ISO 8601 UTC para la API.
                val fechaIso = construirFechaUtc(año, mes, dia, hora, minuto)
                onGuardar(contenido, fechaIso)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Convierte una fecha y hora locales a un string ISO 8601 en UTC.
 * PostgreSQL guarda la columna Fecha como "timestamp with time zone"
 * y Npgsql exige que el DateTime sea UTC.
 */
private fun construirFechaUtc(año: Int, mes: Int, día: Int, hora: Int, minuto: Int): String {
    val cal = Calendar.getInstance()
    cal.set(año, mes, día, hora, minuto)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    fmt.timeZone = TimeZone.getTimeZone("UTC")
    return fmt.format(cal.time)
}
