package com.swimming.app.presentation.tiempos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.swimming.app.R
import com.swimming.app.databinding.FragmentCrearTiempoBinding
import com.swimming.app.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint

/**
 * Pantalla para crear una nueva marca de tiempo.
 * Permite elegir entre dos modos:
 *   - Cronómetro: mide el tiempo en directo (lleva al CronometroFragment).
 *   - Introducir tiempo manualmente: el usuario escribe el tiempo en formato MM:SS o MM:SS.cc.
 *
 * Si llega con el argumento idNadadorEquipo, significa que el entrenador
 * está asignando una marca a un nadador concreto desde la pantalla de equipo.
 */
@AndroidEntryPoint
class CrearTiempoFragment : Fragment() {

    private var _binding: FragmentCrearTiempoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CronometroViewModel by viewModels()

    /** Si es distinto de -1, indica que el flujo viene del entrenador asignando una marca. */
    private var idNadadorEquipoOverride: Int = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCrearTiempoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Si viene desde el equipo, el argumento contiene el ID del nadador receptor.
        idNadadorEquipoOverride = arguments?.getInt("idNadadorEquipo", -1) ?: -1

        // Cambia entre cronómetro e introducción manual según la opción elegida.
        binding.rgTipoTiempo.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbTemporizador -> binding.layoutTiempoManual.visibility = View.GONE
                R.id.rbIntroducirTiempo -> binding.layoutTiempoManual.visibility = View.VISIBLE
            }
        }

        observarResultado()

        binding.btnContinuar.setOnClickListener {
            val estilo = binding.etEstilo.text.toString().trim()
            val metros = binding.etMetros.text.toString().trim()

            if (estilo.isEmpty() || metros.isEmpty()) {
                Toast.makeText(requireContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val descripcion = "$estilo - ${metros}m"

            if (binding.rbTemporizador.isChecked) {
                // Modo cronómetro: navega al CronometroFragment pasando los datos.
                val bundle = Bundle().apply {
                    putString("prueba", descripcion)
                    putInt("idNadadorEquipo", idNadadorEquipoOverride)
                }
                findNavController().navigate(R.id.action_crearTiempo_to_cronometro, bundle)
            } else {
                // Modo manual: valida el tiempo introducido y lo guarda directamente.
                val tiempoBruto = binding.etTiempoManual.text.toString().trim()
                val tiempoNormalizado = normalizarTiempo(tiempoBruto)
                if (tiempoNormalizado == null) {
                    Toast.makeText(
                        requireContext(),
                        "Formato no válido. Usa mm:ss, mm:ss.cc o mm:ss.mmm\n" +
                                "Ej: 1:30, 1:30.45 (centésimas) o 1:30.456 (milisegundos)",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }
                viewModel.descripcion = descripcion
                viewModel.guardarMarca(tiempoNormalizado, idNadadorEquipoOverride.takeIf { it != -1 })
            }
        }
    }

    /**
     * Convierte una entrada del usuario al formato completo "HH:MM:SS.fff" que espera la API.
     * Devuelve null si el formato no es válido.
     */
    private fun normalizarTiempo(entrada: String): String? {
        if (entrada.isEmpty()) return null
        val partes = entrada.split(":")
        return when (partes.size) {
            2 -> {
                // Formato corto MM:SS o MM:SS.decimales.
                val minutos = partes[0].toIntOrNull() ?: return null
                val segundosStr = partes[1]
                // Verifica que la parte de segundos sea un número válido.
                if (segundosStr.toDoubleOrNull() == null) return null

                // Se separan los segundos enteros de los decimales (si los hay).
                val (segundos, milisegundos) = if (segundosStr.contains(".")) {
                    val (segParte, decParte) = segundosStr.split(".")
                    // Normalizamos los decimales a 3 dígitos (milisegundos):
                    //   - 1 dígito  → décimas, rellenamos con 00 al final (5 → 500 ms)
                    //   - 2 dígitos → centésimas, rellenamos con 0 al final (45 → 450 ms)
                    //   - 3+ dígitos → milisegundos, recortamos al primero (456 → 456 ms)
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

    /**
     * Observa el resultado del guardado de la marca y vuelve a la pantalla anterior:
     *   - Si el flujo era del entrenador, vuelve a la pantalla de equipo.
     *   - Si era del nadador, vuelve a la pantalla de tiempos.
     */
    private fun observarResultado() {
        viewModel.marcaGuardada.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    Toast.makeText(requireContext(), "Marca guardada", Toast.LENGTH_SHORT).show()
                    val destino = if (idNadadorEquipoOverride != -1) R.id.equipoFragment else R.id.tiemposFragment
                    findNavController().popBackStack(destino, false)
                }
                is NetworkResult.Error ->
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}