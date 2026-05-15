package com.swimming.app.presentation.equipo

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
    private var nombreEquipoActual: String = ""

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
        observarEquipo()
        observarActualizacion()
        observarEliminacionEquipo()
        observarEliminacion()
        cargarDatos()
    }

    override fun onResume() {
        super.onResume()
        configurarVistaPorRol()
        cargarDatos()
    }

    private fun configurarVistaPorRol() {
        val esEntrenador = sessionManager.esEntrenador()
        val tieneEquipo = sessionManager.getEquipoId() != null

        if (esEntrenador) {
            binding.layoutBotonesEntrenador.visibility = View.VISIBLE

            if (tieneEquipo) {
                binding.btnCrearNadador.visibility = View.VISIBLE
                binding.btnImprimir.visibility = View.VISIBLE
                binding.btnCrearEquipo.visibility = View.GONE
                binding.layoutCabeceraEquipo.visibility = View.VISIBLE
            } else {
                binding.btnCrearNadador.visibility = View.GONE
                binding.btnImprimir.visibility = View.GONE
                binding.btnCrearEquipo.visibility = View.VISIBLE
                binding.layoutCabeceraEquipo.visibility = View.GONE
            }
        } else {
            binding.layoutBotonesEntrenador.visibility = View.GONE
            binding.layoutCabeceraEquipo.visibility = View.GONE
        }

        binding.btnCrearNadador.setOnClickListener {
            findNavController().navigate(R.id.action_equipo_to_crearNadador)
        }
        binding.btnCrearEquipo.setOnClickListener {
            findNavController().navigate(R.id.action_equipo_to_crearEquipo)
        }
        binding.btnImprimir.setOnClickListener {
            findNavController().navigate(R.id.action_equipo_to_imprimir)
        }
        binding.btnEditarEquipo.setOnClickListener {
            mostrarDialogoEditarNombre()
        }
        binding.btnEliminarEquipo.setOnClickListener {
            mostrarDialogoEliminarEquipo()
        }
    }

    private fun configurarRecyclerView() {
        val esEntrenador = sessionManager.esEntrenador()
        adapter = NadadorEquipoAdapter(
            mostrarBotonEliminar = esEntrenador,
            onClick = { nadador ->
                if (esEntrenador) {
                    val bundle = Bundle().apply { putInt("idNadadorEquipo", nadador.idNadadorEquipo) }
                    findNavController().navigate(R.id.action_equipo_to_crearTiempo, bundle)
                }
            },
            onEliminarClick = { nadador -> mostrarConfirmacionEliminar(nadador) }
        )
        binding.rvNadadores.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNadadores.adapter = adapter
    }

    private fun mostrarDialogoEditarNombre() {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            setText(nombreEquipoActual)
            setSelection(text.length)
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Editar nombre del equipo")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevo = input.text.toString().trim()
                val idEquipo = sessionManager.getEquipoId()
                if (nuevo.isEmpty()) {
                    Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                } else if (idEquipo != null) {
                    viewModel.actualizarNombreEquipo(idEquipo, nuevo)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEliminarEquipo() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar equipo")
            .setMessage("¿Seguro que quieres eliminar el equipo \"$nombreEquipoActual\"? " +
                    "Los nadadores asociados quedarán desvinculados y tendrás que crear un nuevo equipo.")
            .setPositiveButton("Eliminar") { _, _ ->
                val idEquipo = sessionManager.getEquipoId()
                if (idEquipo != null) viewModel.eliminarEquipo(idEquipo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarConfirmacionEliminar(nadador: com.swimming.app.domain.model.NadadorEquipo) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar nadador")
            .setMessage("¿Seguro que quieres eliminar a ${nadador.nombre} ${nadador.apellidos} del equipo? " +
                    "Si está conectado a una cuenta de usuario, esa cuenta saldrá del equipo automáticamente.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarNadador(nadador.idNadadorEquipo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
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

    private fun observarEquipo() {
        viewModel.equipo.observe(viewLifecycleOwner) { result ->
            if (result is NetworkResult.Success) {
                nombreEquipoActual = result.data.nombre
                binding.tvNombreEquipo.text = result.data.nombre
            }
        }
    }

    private fun observarActualizacion() {
        viewModel.equipoActualizado.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    nombreEquipoActual = result.data.nombre
                    binding.tvNombreEquipo.text = result.data.nombre
                    Toast.makeText(requireContext(), "Nombre actualizado", Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Error -> Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                is NetworkResult.Loading -> {}
            }
        }
    }

    private fun observarEliminacionEquipo() {
        viewModel.equipoEliminado.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    Toast.makeText(requireContext(), "Equipo eliminado", Toast.LENGTH_SHORT).show()
                    // Limpiar equipoId de la sesión local → vuelve al estado "entrenador sin equipo"
                    sessionManager.borrarEquipoId()
                    nombreEquipoActual = ""
                    configurarVistaPorRol()
                    adapter.submitList(emptyList())
                }
                is NetworkResult.Error -> Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                is NetworkResult.Loading -> {}
            }
        }
    }

    private fun observarEliminacion() {
        viewModel.nadadorEliminado.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    Toast.makeText(requireContext(), "Nadador eliminado", Toast.LENGTH_SHORT).show()
                    cargarDatos()
                }
                is NetworkResult.Error -> Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                is NetworkResult.Loading -> {}
            }
        }
    }

    private fun cargarDatos() {
        val idEquipo = sessionManager.getEquipoId()
        val esEntrenador = sessionManager.esEntrenador()

        when {
            !esEntrenador && idEquipo == null -> {
                findNavController().navigate(R.id.action_equipo_to_ingresarEquipo)
            }
            idEquipo != null -> {
                viewModel.cargarNadadores(idEquipo)
                viewModel.cargarEquipo(idEquipo)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}