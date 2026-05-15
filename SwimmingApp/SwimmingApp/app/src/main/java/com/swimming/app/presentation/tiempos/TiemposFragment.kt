package com.swimming.app.presentation.tiempos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.swimming.app.R
import com.swimming.app.databinding.FragmentTiemposBinding
import com.swimming.app.utils.NetworkResult
import com.swimming.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TiemposFragment : Fragment() {

    private var _binding: FragmentTiemposBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TiemposViewModel by viewModels()
    private lateinit var adapterMias: MarcasAdapter
    private lateinit var adapterEntrenador: MarcasAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTiemposBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapterMias = MarcasAdapter()
        adapterEntrenador = MarcasAdapter()

        binding.rvMarcas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMarcas.adapter = adapterMias

        binding.rvMarcasEntrenador.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMarcasEntrenador.adapter = adapterEntrenador

        // Ocultar el FAB si el usuario es entrenador.
        // El entrenador solo puede crear tiempos pulsando sobre un nadador de su equipo.
        val sessionManager = SessionManager(requireContext())
        if (sessionManager.esEntrenador()) {
            binding.fabAnadir.visibility = View.GONE
        } else {
            binding.fabAnadir.setOnClickListener {
                findNavController().navigate(R.id.action_tiempos_to_crearTiempo)
            }
        }

        // Lista "Mis marcas" (o lista única para entrenador)
        viewModel.marcasMias.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    val lista = result.data?.toList().orEmpty()
                    adapterMias.submitList(lista)
                    binding.tvHeaderMias.visibility = if (lista.isNotEmpty()) View.VISIBLE else View.GONE
                }
                is NetworkResult.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    binding.tvHeaderMias.visibility = View.GONE
                }
                is NetworkResult.Loading -> {}
            }
        }

        // Lista "Asignadas por el entrenador" (solo si hay)
        viewModel.marcasEntrenador.observe(viewLifecycleOwner) { lista ->
            adapterEntrenador.submitList(lista.toList())
            binding.tvHeaderEntrenador.visibility = if (lista.isNotEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.cargarMarcas()
    }

    override fun onResume() {
        super.onResume()
        viewModel.cargarMarcas()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}