package com.swimming.app.presentation.perfil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.swimming.app.R
import com.swimming.app.databinding.FragmentPerfilBinding
import com.swimming.app.presentation.auth.LoginActivity
import com.swimming.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Pantalla de perfil del usuario.
 * Muestra los datos básicos guardados en la sesión local
 * y permite editar el perfil o cerrar sesión.
 */
@AndroidEntryPoint
class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mostrarDatosUsuario()
        configurarBotones()
    }

    /** Rellena la UI con los datos del usuario guardados en la sesión. */
    private fun mostrarDatosUsuario() {
        binding.tvNombreCompleto.text = "${sessionManager.getUserNombre()} ${sessionManager.getUserApellidos()}"
        binding.tvEmail.text = sessionManager.getUserEmail()
        binding.tvRol.text = sessionManager.getUserRol()
        binding.tvEquipo.text = "Equipo: ${sessionManager.getEquipoId() ?: "Sin equipo"}"
    }

    /** Configura los listeners de editar perfil y cerrar sesión. */
    private fun configurarBotones() {
        binding.btnEditarPerfil.setOnClickListener {
            findNavController().navigate(R.id.action_perfil_to_editarPerfil)
        }

        binding.btnCerrarSesion.setOnClickListener {
            // Se borran los datos de sesión y se reinicia la app desde el splash.
            sessionManager.cerrarSesion()
            val intent = Intent(requireContext(), com.swimming.app.presentation.splash.SplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}