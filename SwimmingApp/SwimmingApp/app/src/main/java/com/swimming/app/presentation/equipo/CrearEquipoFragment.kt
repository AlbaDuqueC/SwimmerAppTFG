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
import dagger.hilt.android.AndroidEntryPoint

/** Pantalla para crear un nuevo Equipo. Solo accesible para entrenadores. */
@AndroidEntryPoint
class CrearEquipoFragment : Fragment() {

    private var _binding: FragmentCrearEquipoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EquipoViewModel by viewModels()

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
            val nombre = binding.etNombreEquipo.text.toString().trim()
            if (nombre.isEmpty()) {
                Toast.makeText(requireContext(), "Escribe el nombre del equipo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.crearNuevoEquipo(nombre)
        }
    }

    private fun observarResultado() {
        viewModel.equipoCreado.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    Toast.makeText(requireContext(), "Equipo creado con éxito", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is NetworkResult.Error -> Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
