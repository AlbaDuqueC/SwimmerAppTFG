package com.swimming.app.presentation.equipo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.swimming.app.databinding.FragmentCrearNadadorBinding
import com.swimming.app.utils.NetworkResult
import com.swimming.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Pantalla para crear un NadadorEquipo. Solo accesible para entrenadores. */
@AndroidEntryPoint
class CrearNadadorFragment : Fragment() {

    private var _binding: FragmentCrearNadadorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EquipoViewModel by viewModels()

    @Inject lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCrearNadadorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarBoton()
        observarResultado()
    }

    private fun configurarBoton() {
        binding.btnCrear.setOnClickListener { intentarCrear() }
    }

    private fun intentarCrear() {
        val nombre = binding.etNombre.text.toString().trim()
        val apellidos = binding.etApellidos.text.toString().trim()
        val idEquipo = sessionManager.getEquipoId()

        if (nombre.isEmpty() || apellidos.isEmpty()) {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (idEquipo == null) {
            Toast.makeText(requireContext(), "No tienes un equipo asignado", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.crearNuevoNadador(nombre, apellidos, idEquipo)
    }

    private fun observarResultado() {
        viewModel.nadadorCreado.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    Toast.makeText(requireContext(), "Nadador creado con éxito", Toast.LENGTH_SHORT).show()
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
