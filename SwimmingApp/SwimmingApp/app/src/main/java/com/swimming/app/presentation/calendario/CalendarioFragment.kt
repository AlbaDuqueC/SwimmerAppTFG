package com.swimming.app.presentation.calendario

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.swimming.app.databinding.DialogCrearEventoBinding
import com.swimming.app.databinding.FragmentCalendarioBinding
import com.swimming.app.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

/**
 * Pantalla del calendario donde el usuario puede crear eventos (rutinas)
 * seleccionando una fecha y una hora del día.
 */
@AndroidEntryPoint
class CalendarioFragment : Fragment() {

    // Patrón ViewBinding nullable: se asigna en onCreateView y se libera en onDestroyView
    // para evitar fugas de memoria al destruirse la vista del Fragment.
    private var _binding: FragmentCalendarioBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CalendarioViewModel by viewModels()

    /** Infla el layout del fragmento. */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    /** Configura listeners y observadores cuando la vista ya está creada. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Al seleccionar una fecha del calendario se abre el diálogo para crear evento.
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            mostrarDialogoCrearEvento(year, month, dayOfMonth)
        }

        // Observa el resultado de creación de evento y refresca la lista al éxito.
        viewModel.eventoCreado.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    Toast.makeText(requireContext(), "Evento creado", Toast.LENGTH_SHORT).show()
                    viewModel.cargarRutinas()
                }
                is NetworkResult.Error -> Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }

        viewModel.cargarRutinas()
    }

    /**
     * Abre un diálogo donde el usuario escribe el título del evento y elige una hora.
     * Al confirmar, construye la fecha en formato UTC y llama al ViewModel.
     */
    private fun mostrarDialogoCrearEvento(año: Int, mes: Int, día: Int) {
        val dialogBinding = DialogCrearEventoBinding.inflate(layoutInflater)

        val fechaTexto = "%02d/%02d/%d".format(día, mes + 1, año)
        dialogBinding.tvFechaSeleccionada.text = "Fecha: $fechaTexto"

        // Hora seleccionada por defecto: 18:00.
        var hora = 18
        var minuto = 0
        dialogBinding.btnHora.text = "Hora: %02d:%02d".format(hora, minuto)

        // Botón que abre el TimePicker para que el usuario elija una hora distinta.
        dialogBinding.btnHora.setOnClickListener {
            TimePickerDialog(requireContext(), { _, h, m ->
                hora = h
                minuto = m
                dialogBinding.btnHora.text = "Hora: %02d:%02d".format(h, m)
            }, hora, minuto, true).show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Nuevo evento")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                val contenido = dialogBinding.etContenido.text.toString().trim()
                if (contenido.isEmpty()) {
                    Toast.makeText(requireContext(), "Escribe un título", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                // Fecha en formato ISO 8601 con UTC: "2026-05-15T19:30:00Z".
                val fechaIso = construirFechaUtc(año, mes, día, hora, minuto)
                viewModel.crearEvento(contenido, fechaIso)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Convierte una fecha y hora locales a un string ISO 8601 en UTC.
     *
     * PostgreSQL guarda la columna Fecha como "timestamp with time zone"
     * y Npgsql exige que el DateTime sea UTC. Por eso convertimos la hora local
     * que ha elegido el usuario a UTC y añadimos la "Z" final del formato ISO.
     */
    private fun construirFechaUtc(año: Int, mes: Int, día: Int, hora: Int, minuto: Int): String {
        val cal = Calendar.getInstance()
        cal.set(año, mes, día, hora, minuto)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
        fmt.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return fmt.format(cal.time)
    }

    /** Libera el binding al destruirse la vista para evitar fugas de memoria. */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}