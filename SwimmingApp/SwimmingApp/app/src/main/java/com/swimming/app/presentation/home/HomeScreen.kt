package com.swimming.app.presentation.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.swimming.app.domain.model.Rutina
import com.swimming.app.utils.NetworkResult
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla principal de la app.
 * Muestra la vista semanal con los eventos del usuario distribuidos por día
 * y debajo una lista con los próximos eventos futuros ordenados por fecha.
 */
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val resultado by viewModel.rutinas.observeAsState()

    // Iniciales de los días de la semana para las cabeceras de las columnas.
    val nombresDias = listOf("L", "M", "X", "J", "V", "S", "D")
    val azulMarca = Color(0xFF0A2A3D)

    // Carga las rutinas al entrar en la pantalla.
    LaunchedEffect(Unit) {
        viewModel.cargarRutinas()
    }

    // Calcula la lista de próximos eventos y el mapa semana → eventos.
    val eventosProximos = remember(resultado) {
        if (resultado is NetworkResult.Success) {
            val ahora = Calendar.getInstance().time
            (resultado as NetworkResult.Success<List<Rutina>>).data
                .filter { parsearFecha(it.fecha)?.after(ahora) ?: false }
                .take(10)
        } else emptyList()
    }

    val eventosPorDia = remember(resultado) {
        if (resultado is NetworkResult.Success) {
            distribuirPorDiaSemana((resultado as NetworkResult.Success<List<Rutina>>).data)
        } else Array(7) { emptyList<Rutina>() }
    }

    // Muestra error si el resultado es un error de red.
    LaunchedEffect(resultado) {
        if (resultado is NetworkResult.Error) {
            Toast.makeText(context, (resultado as NetworkResult.Error).message, Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Cabecera con la vista semanal.
        item {
            Text(
                text = "Calendario semanal",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = azulMarca,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp)
            )

            // Fila con las 7 columnas de días.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                for (i in 0..6) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Cabecera del día.
                        Text(
                            text = nombresDias[i],
                            color = azulMarca,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Eventos del día apilados verticalmente.
                        eventosPorDia[i].forEach { rutina ->
                            val cal = Calendar.getInstance().apply {
                                time = parsearFecha(rutina.fecha) ?: return@forEach
                            }
                            val hora = "%02d:%02d".format(
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE)
                            )
                            // Tarjeta visual del evento con hora y título.
                            Text(
                                text = "$hora\n${rutina.contenido}",
                                color = Color.White,
                                fontSize = 8.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .background(azulMarca)
                                    .padding(horizontal = 2.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Título de la sección de próximos eventos.
            Text(
                text = "Siguientes eventos",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = azulMarca,
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
            )
        }

        // Lista de próximos eventos.
        if (eventosProximos.isEmpty()) {
            item {
                Text(
                    text = "No hay eventos próximos",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(eventosProximos, key = { it.id }) { rutina ->
                ItemEvento(rutina = rutina, azulMarca = azulMarca)
            }
        }

        // Espacio final para que el último elemento no quede pegado al borde.
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

/**
 * Composable de cada fila de la lista de próximos eventos.
 * Muestra la letra inicial del evento, su nombre y la fecha formateada.
 */
@Composable
private fun ItemEvento(rutina: Rutina, azulMarca: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Círculo con la letra inicial del evento.
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(azulMarca, shape = MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rutina.contenido.firstOrNull()?.uppercase() ?: "E",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Nombre del evento.
        Text(
            text = rutina.contenido,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = azulMarca
        )

        // Fecha formateada en formato corto dd/MM HH:mm.
        Text(
            text = formatearFechaCorta(rutina.fecha),
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

/**
 * Distribuye una lista de rutinas en un array de 7 listas (una por día de la semana),
 * filtrando solo las que caen en la semana actual.
 */
private fun distribuirPorDiaSemana(todas: List<Rutina>): Array<List<Rutina>> {
    val (inicioSemana, finSemana) = limitesSemanaActual()
    // Se declara como Array<List<Rutina>> usando MutableList internamente para poder hacer .add().
    val interno = Array(7) { mutableListOf<Rutina>() }

    todas
        .mapNotNull { rutina -> parsearFecha(rutina.fecha)?.let { rutina to it } }
        .filter { (_, fecha) -> !fecha.before(inicioSemana) && !fecha.after(finSemana) }
        .sortedBy { it.second }
        .forEach { (rutina, fecha) ->
            val cal = Calendar.getInstance().apply { time = fecha }
            // Conversión del día de la semana al índice 0..6 (Lunes=0, Domingo=6).
            val idx = ((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7)
            interno[idx].add(rutina)
        }

    // Se convierte a Array<List<Rutina>> para que el tipo de retorno sea inmutable.
    return Array(7) { i -> interno[i].toList() }
}

/**
 * Calcula el lunes a las 00:00 y el domingo a las 23:59:59 de la semana actual.
 */
private fun limitesSemanaActual(): Pair<Date, Date> {
    val cal = Calendar.getInstance()
    cal.firstDayOfWeek = Calendar.MONDAY
    while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) cal.add(Calendar.DAY_OF_MONTH, -1)
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
    val inicio = cal.time
    cal.add(Calendar.DAY_OF_MONTH, 7)
    cal.add(Calendar.SECOND, -1)
    val fin = cal.time
    return inicio to fin
}

/**
 * Parsea una fecha en alguno de los formatos típicos que devuelve la API,
 * probando varios patrones hasta encontrar uno que coincida.
 */
fun parsearFecha(iso: String): Date? {
    val intentos = listOf(
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'"
    )
    for (patron in intentos) {
        try {
            return SimpleDateFormat(patron, Locale.US).parse(iso)
        } catch (_: Exception) {}
    }
    // Último intento: usar solo los primeros 19 caracteres "yyyy-MM-ddTHH:mm:ss".
    return try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(iso.take(19))
    } catch (_: Exception) { null }
}

/** Convierte un ISO completo a un formato compacto "dd/MM HH:mm". */
private fun formatearFechaCorta(iso: String): String = try {
    val fecha = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(iso.take(19))
    SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(fecha!!)
} catch (_: Exception) { iso }
