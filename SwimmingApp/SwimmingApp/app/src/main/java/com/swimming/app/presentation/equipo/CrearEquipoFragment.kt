package com.swimming.app.presentation.equipo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.swimming.app.databinding.FragmentCrearEquipoBinding
import com.swimming.app.utils.NetworkResult
import com.swimming.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Pantalla para crear un nuevo Equipo. Solo accesible para entrenadores. */
@AndroidEntryPoint
class CrearEquipoFragment : Fragment() {

    private var _binding: FragmentCrearEquipoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EquipoViewModel by viewModels()

    @Inject lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCrearEquipoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarBoton()
        observarResultado()
    }

    private fun configurarBoton() {
        binding.btnCrear.setOnClickListener {
            val nombre = binding.etNombreEquipo.text?.toString()?.trim().orEmpty()
            if (nombre.isEmpty()) {
                Toast.makeText(requireContext(), "Escribe el nombre del equipo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Solo el entrenador crea equipos: pasamos su ID para que la API lo vincule
            val idEntrenador = sessionManager.getUserId().takeIf { it != -1 }
            viewModel.crearNuevoEquipo(nombre, idEntrenador)
        }
    }

    private fun observarResultado() {
        viewModel.equipoCreado.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    // Guardamos el equipoId en la sesión local para que el resto de pantallas lo vea
                    sessionManager.guardarEquipoId(result.data.id)
                    Toast.makeText(requireContext(), "Equipo creado con éxito", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is NetworkResult.Error ->
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}