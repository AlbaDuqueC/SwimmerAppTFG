package com.swimming.app.presentation.tiempos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.swimming.app.R
import com.swimming.app.databinding.FragmentTiemposBinding
import com.swimming.app.domain.model.MarcaDeTiempo
import com.swimming.app.utils.NetworkResult
import com.swimming.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint

/**
 * Pantalla de tiempos. Muestra dos listas separadas (mías / asignadas por entrenador)
 * y permite eliminar marcas con un botón de papelera.
 */
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

        // Se consulta el rol del usuario una sola vez para reutilizarlo abajo.
        val sessionManager = SessionManager(requireContext())
        val esEntrenador = sessionManager.esEntrenador()

        // Se ajusta el texto del header según el rol:
        //   - Entrenador → "Tiempos del equipo"
        //   - Nadador    → "Mis marcas"
        binding.tvHeaderMias.text = if (esEntrenador) "Tiempos del equipo" else "Mis marcas"

        // Ambos adapters muestran el botón de eliminar y reaccionan con el mismo callback.
        adapterMias = MarcasAdapter(
            mostrarBotonEliminar = true,
            onEliminarClick = { marca -> confirmarEliminacion(marca) }
        )
        adapterEntrenador = MarcasAdapter(
            mostrarBotonEliminar = true,
            onEliminarClick = { marca -> confirmarEliminacion(marca) }
        )

        binding.rvMarcas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMarcas.adapter = adapterMias

        binding.rvMarcasEntrenador.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMarcasEntrenador.adapter = adapterEntrenador

        // El FAB de crear marca se oculta para el entrenador.
        if (esEntrenador) {
            binding.fabAnadir.visibility = View.GONE
        } else {
            binding.fabAnadir.setOnClickListener {
                findNavController().navigate(R.id.action_tiempos_to_crearTiempo)
            }
        }

        observarMarcasMias()
        observarMarcasEntrenador()
        observarEliminacion()

        viewModel.cargarMarcas()
    }

    /** Muestra un diálogo de confirmación antes de eliminar una marca. */
    private fun confirmarEliminacion(marca: MarcaDeTiempo) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar marca")
            .setMessage("¿Seguro que quieres eliminar esta marca?\n\n${marca.descripcion}")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarMarca(marca.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /** Observa la lista de marcas propias del usuario activo. */
    private fun observarMarcasMias() {
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
    }

    /** Observa la lista de marcas que el entrenador ha asignado al nadador. */
    private fun observarMarcasEntrenador() {
        viewModel.marcasEntrenador.observe(viewLifecycleOwner) { lista ->
            adapterEntrenador.submitList(lista.toList())
            binding.tvHeaderEntrenador.visibility = if (lista.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    /** Observa el resultado de la eliminación de una marca para mostrar feedback. */
    private fun observarEliminacion() {
        viewModel.marcaEliminada.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success ->
                    Toast.makeText(requireContext(), "Marca eliminada", Toast.LENGTH_SHORT).show()
                is NetworkResult.Error ->
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                is NetworkResult.Loading -> {}
            }
        }
    }

    /** Refresca las marcas al volver a la pantalla. */
    override fun onResume() {
        super.onResume()
        viewModel.cargarMarcas()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}