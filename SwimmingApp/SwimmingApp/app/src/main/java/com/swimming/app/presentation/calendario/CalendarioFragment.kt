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

@AndroidEntryPoint
class CalendarioFragment : Fragment() {

    private var _binding: FragmentCalendarioBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CalendarioViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            mostrarDialogoCrearEvento(year, month, dayOfMonth)
        }

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

    /** Abre un diálogo donde el usuario escribe el título del evento y elige una hora. */
    private fun mostrarDialogoCrearEvento(año: Int, mes: Int, día: Int) {
        val dialogBinding = DialogCrearEventoBinding.inflate(layoutInflater)

        val fechaTexto = "%02d/%02d/%d".format(día, mes + 1, año)
        dialogBinding.tvFechaSeleccionada.text = "Fecha: $fechaTexto"

        // Hora seleccionada por defecto: 18:00
        var hora = 18
        var minuto = 0
        dialogBinding.btnHora.text = "Hora: %02d:%02d".format(hora, minuto)

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
                // ISO 8601 sin zona: "2026-05-15T19:30:00"
                val fechaIso = construirFechaUtc(año, mes, día, hora, minuto)
                viewModel.crearEvento(contenido, fechaIso)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * PostgreSQL guarda la columna Fecha como "timestamp with time zone".
     * Npgsql exige que el DateTime sea UTC. Convertimos la hora local
     * que ha elegido el usuario a UTC y la mandamos con la "Z" al final.
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}