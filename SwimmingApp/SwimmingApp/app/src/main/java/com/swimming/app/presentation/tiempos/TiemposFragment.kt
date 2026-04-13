package com.swimming.app.presentation.tiempos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.swimming.app.R
import com.swimming.app.databinding.FragmentTiemposBinding
import com.swimming.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Pantalla de tiempos. Permite rellenar los datos de la prueba antes de ir al cronómetro. */
@AndroidEntryPoint
class TiemposFragment : Fragment() {

    private var _binding: FragmentTiemposBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTiemposBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarVistaPorRol()
        configurarBoton()
    }

    private fun configurarVistaPorRol() {
        val esEntrenador = sessionManager.esEntrenador()
        binding.layoutSelectorNadador.visibility = if (esEntrenador) View.VISIBLE else View.GONE
    }

    private fun configurarBoton() {
        binding.btnContinuar.setOnClickListener {
            val prueba = binding.etPrueba.text.toString().trim()
            if (prueba.isEmpty()) {
                Toast.makeText(requireContext(), "Escribe el nombre de la prueba", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            findNavController().navigate(R.id.action_tiempos_to_cronometro)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
