package com.swimming.app.presentation.calendario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.swimming.app.databinding.FragmentCalendarioBinding
import com.swimming.app.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint

/** Pantalla de calendario mensual. Muestra las rutinas marcadas en cada día. */
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
        observarRutinas()
        viewModel.cargarRutinas()
    }

    private fun observarRutinas() {
        viewModel.rutinas.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    // Aquí se marcarían los días con rutinas en el CalendarView
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
