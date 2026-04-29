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

@AndroidEntryPoint
class CrearTiempoFragment : Fragment() {

    private var _binding: FragmentCrearTiempoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CronometroViewModel by viewModels()

    /** Si > 0 → flujo entrenador asignando marca a un nadador concreto. */
    private var idNadadorEquipoOverride: Int = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCrearTiempoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        idNadadorEquipoOverride = arguments?.getInt("idNadadorEquipo", -1) ?: -1

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
                // Pasamos el override al cronómetro para que sepa que es flujo entrenador
                val bundle = Bundle().apply {
                    putString("prueba", descripcion)
                    putInt("idNadadorEquipo", idNadadorEquipoOverride)
                }
                findNavController().navigate(R.id.action_crearTiempo_to_cronometro, bundle)
            } else {
                // Camino manual: guardar directamente
                val tiempoBruto = binding.etTiempoManual.text.toString().trim()
                val tiempoNormalizado = normalizarTiempo(tiempoBruto)
                if (tiempoNormalizado == null) {
                    Toast.makeText(
                        requireContext(),
                        "Formato no válido. Usa mm:ss o mm:ss.cc (ej: 1:30 o 1:30.45)",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }
                viewModel.descripcion = descripcion
                viewModel.guardarMarca(tiempoNormalizado, idNadadorEquipoOverride.takeIf { it != -1 })
            }
        }
    }

    /** Normaliza "MM:SS" o "MM:SS.cc" a un TimeSpan válido para la API. */
    private fun normalizarTiempo(entrada: String): String? {
        if (entrada.isEmpty()) return null
        val partes = entrada.split(":")
        return when (partes.size) {
            2 -> {
                val minutos = partes[0].toIntOrNull() ?: return null
                val segundosStr = partes[1]
                if (segundosStr.toDoubleOrNull() == null) return null
                "00:%02d:%s".format(minutos, segundosStr.padStart(2, '0'))
            }
            3 -> entrada
            else -> null
        }
    }

    private fun observarResultado() {
        viewModel.marcaGuardada.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    Toast.makeText(requireContext(), "Marca guardada", Toast.LENGTH_SHORT).show()
                    // Si vino del flujo entrenador, vuelve a Equipo; si no, a Tiempos
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