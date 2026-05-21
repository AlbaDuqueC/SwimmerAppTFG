package com.swimming.app.presentation.perfil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.swimming.app.databinding.FragmentEditarPerfilBinding
import com.swimming.app.presentation.auth.LoginActivity
import com.swimming.app.utils.NetworkResult
import com.swimming.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Pantalla de edición del perfil del usuario.
 * Permite actualizar el nombre y los apellidos, o eliminar la cuenta por completo.
 */
@AndroidEntryPoint
class EditarPerfilFragment : Fragment() {

    private var _binding: FragmentEditarPerfilBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditarPerfilViewModel by viewModels()

    @Inject lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditarPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rellenarCampos()
        configurarBotones()
        observarResultados()
    }

    /** Rellena los campos con los datos actuales del usuario. */
    private fun rellenarCampos() {
        binding.etNombre.setText(sessionManager.getUserNombre())
        binding.etApellidos.setText(sessionManager.getUserApellidos())
    }

    private fun configurarBotones() {
        binding.btnAceptar.setOnClickListener { actualizarPerfil() }
        binding.btnEliminarCuenta.setOnClickListener { confirmarEliminacion() }
    }

    /** Valida los campos y lanza la actualización del perfil en el ViewModel. */
    private fun actualizarPerfil() {
        val nombre = binding.etNombre.text?.toString()?.trim().orEmpty()
        val apellidos = binding.etApellidos.text?.toString()?.trim().orEmpty()

        if (nombre.isEmpty() || apellidos.isEmpty()) {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // El botón se deshabilita para evitar dobles pulsaciones mientras se procesa.
        binding.btnAceptar.isEnabled = false
        viewModel.actualizarPerfil(sessionManager.getUserId(), nombre, apellidos, sessionManager.esEntrenador())
    }

    /** Muestra un diálogo de confirmación antes de eliminar la cuenta. */
    private fun confirmarEliminacion() {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar cuenta")
            .setMessage("¿Estás seguro? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarCuenta(sessionManager.getUserId(), sessionManager.esEntrenador())
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /** Observa los resultados de actualización y eliminación del perfil. */
    private fun observarResultados() {
        viewModel.actualizacionResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is NetworkResult.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    binding.btnAceptar.isEnabled = true
                }
                else -> {}
            }
        }

        viewModel.eliminacionResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    // Tras eliminar la cuenta, se cierra sesión y se vuelve al splash.
                    sessionManager.cerrarSesion()
                    val intent = Intent(requireContext(), com.swimming.app.presentation.splash.SplashActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
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