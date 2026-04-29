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

    override fun onResume() {
        super.onResume()
        // Si el nadador acaba de unirse, al volver le aparecen los datos del equipo
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
        adapter = NadadorEquipoAdapter { nadador ->
            // Solo el entrenador puede asignar marcas tocando un nadador
            if (sessionManager.esEntrenador()) {
                val bundle = Bundle().apply {
                    putInt("idNadadorEquipo", nadador.idNadadorEquipo)
                }
                findNavController().navigate(R.id.action_equipo_to_crearTiempo, bundle)
            }
        }
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
        val esEntrenador = sessionManager.esEntrenador()

        when {
            // Nadador sin equipo → ir a la pantalla de unirse
            !esEntrenador && idEquipo == null -> {
                findNavController().navigate(R.id.action_equipo_to_ingresarEquipo)
            }
            // Tiene equipo (entrenador o nadador) → cargar lista de nadadores
            idEquipo != null -> viewModel.cargarNadadores(idEquipo)
            // Entrenador sin equipo → no carga nada, verá la lista vacía con sus botones
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}