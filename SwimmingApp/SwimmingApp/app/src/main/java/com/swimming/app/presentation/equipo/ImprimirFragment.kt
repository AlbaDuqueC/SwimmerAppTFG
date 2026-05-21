package com.swimming.app.presentation.equipo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.swimming.app.databinding.FragmentImprimirBinding
import com.swimming.app.utils.NetworkResult
import com.swimming.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Pantalla de impresión. Muestra todas las marcas del equipo
 * y permite exportarlas para imprimirlas.
 */
@AndroidEntryPoint
class ImprimirFragment : Fragment() {

    private var _binding: FragmentImprimirBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ImprimirViewModel by viewModels()
    private lateinit var adapter: MarcasAdapter

    @Inject lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentImprimirBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarRecyclerView()
        observarMarcas()
        configurarBotonImprimir()
        cargarMarcas()
    }

    /** Configura el RecyclerView con el adapter de marcas y un LinearLayoutManager. */
    private fun configurarRecyclerView() {
        adapter = MarcasAdapter()
        binding.rvMarcas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMarcas.adapter = adapter
    }

    /** Observa la lista de marcas del ViewModel y la inyecta en el adapter. */
    private fun observarMarcas() {
        viewModel.marcas.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> adapter.submitList(result.data)
                is NetworkResult.Error -> Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }
    }

    /** Configura el botón de impresión. */
    private fun configurarBotonImprimir() {
        binding.btnImprimir.setOnClickListener {
            Toast.makeText(requireContext(), "Preparando impresión...", Toast.LENGTH_SHORT).show()
        }
    }

    /** Carga las marcas del equipo activo de la sesión. */
    private fun cargarMarcas() {
        val idEquipo = sessionManager.getEquipoId()
        if (idEquipo != null) viewModel.cargarMarcas(idEquipo)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}