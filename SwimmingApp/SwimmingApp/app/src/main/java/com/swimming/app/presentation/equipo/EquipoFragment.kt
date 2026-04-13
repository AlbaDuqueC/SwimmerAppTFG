package com.swimming.app.presentation.equipo

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
import com.swimming.app.databinding.FragmentEquipoBinding
import com.swimming.app.utils.NetworkResult
import com.swimming.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Pantalla del equipo. Muestra la lista de nadadores. El entrenador puede crear nadadores y equipos. */
@AndroidEntryPoint
class EquipoFragment : Fragment() {

    private var _binding: FragmentEquipoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EquipoViewModel by viewModels()
    private lateinit var adapter: NadadorEquipoAdapter

    @Inject lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEquipoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarVistaPorRol()
        configurarRecyclerView()
        observarNadadores()
        cargarDatos()
    }

    private fun configurarVistaPorRol() {
        val esEntrenador = sessionManager.esEntrenador()
        binding.layoutBotonesEntrenador.visibility = if (esEntrenador) View.VISIBLE else View.GONE
        binding.btnCrearNadador.setOnClickListener {
            findNavController().navigate(R.id.action_equipo_to_crearNadador)
        }
        binding.btnCrearEquipo.setOnClickListener {
            findNavController().navigate(R.id.action_equipo_to_crearEquipo)
        }
        binding.btnImprimir.setOnClickListener {
            findNavController().navigate(R.id.action_equipo_to_imprimir)
        }
    }

    private fun configurarRecyclerView() {
        adapter = NadadorEquipoAdapter()
        binding.rvNadadores.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNadadores.adapter = adapter
    }

    private fun observarNadadores() {
        viewModel.nadadores.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> adapter.submitList(result.data)
                is NetworkResult.Error -> Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                is NetworkResult.Loading -> {}
            }
        }
    }

    private fun cargarDatos() {
        val idEquipo = sessionManager.getEquipoId()
        if (idEquipo != null) viewModel.cargarNadadores(idEquipo)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
