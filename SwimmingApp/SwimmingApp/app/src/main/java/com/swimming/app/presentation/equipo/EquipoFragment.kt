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

/**
 * Pantalla principal del equipo.
 * Adapta su interfaz según el rol del usuario:
 *   - Entrenador con equipo: ve la cabecera con el nombre, los botones de crear nadador,
 *     imprimir, editar nombre y eliminar equipo.
 *   - Entrenador sin equipo: solo ve el botón de crear equipo.
 *   - Nadador sin equipo: se le redirige automáticamente a la pantalla de ingresar código.
 *   - Nadador con equipo: ve solo la lista de nadadores del equipo.
 */
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

    /** Configura todos los listeners y observadores cuando la vista ya está creada. */
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
        observarActualizacionNadador()
    }

    /** Refresca la vista al volver a la pantalla para reflejar posibles cambios de estado. */
    override fun onResume() {
        super.onResume()
        configurarVistaPorRol()
        cargarDatos()
    }

    /**
     * Muestra u oculta los botones según el rol del usuario y si tiene equipo o no.
     * También configura los listeners de los botones principales.
     */
    private fun configurarVistaPorRol() {
        val esEntrenador = sessionManager.esEntrenador()
        val tieneEquipo = sessionManager.getEquipoId() != null

        if (esEntrenador) {
            binding.layoutBotonesEntrenador.visibility = View.VISIBLE

            if (tieneEquipo) {
                // Entrenador con equipo: ve gestión completa.
                binding.btnCrearNadador.visibility = View.VISIBLE
                binding.btnImprimir.visibility = View.VISIBLE
                binding.btnCrearEquipo.visibility = View.GONE
                binding.layoutCabeceraEquipo.visibility = View.VISIBLE
            } else {
                // Entrenador sin equipo: solo ve el botón de crear.
                binding.btnCrearNadador.visibility = View.GONE
                binding.btnImprimir.visibility = View.GONE
                binding.btnCrearEquipo.visibility = View.VISIBLE
                binding.layoutCabeceraEquipo.visibility = View.GONE
            }
        } else {
            // Nadador: no ve ninguno de los botones de gestión.
            binding.layoutBotonesEntrenador.visibility = View.GONE
            binding.layoutCabeceraEquipo.visibility = View.GONE
        }

        // Listeners de los botones de navegación y acciones del entrenador.
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
            // Los botones de editar y eliminar solo aparecen en modo entrenador.
            mostrarBotones = esEntrenador,
            onClick = { nadador ->
                // Al pulsar sobre un nadador, el entrenador navega a la pantalla de asignar tiempos.
                if (esEntrenador) {
                    val bundle = Bundle().apply { putInt("idNadadorEquipo", nadador.idNadadorEquipo) }
                    findNavController().navigate(R.id.action_equipo_to_crearTiempo, bundle)
                }
            },
            onEditarClick = { nadador -> mostrarDialogoEditarNadador(nadador) },
            onEliminarClick = { nadador -> mostrarConfirmacionEliminar(nadador) }
        )
        binding.rvNadadores.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNadadores.adapter = adapter
    }

    /** Diálogo para editar el nombre del equipo. */
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

    /** Diálogo de confirmación para eliminar el equipo. */
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

    /** Diálogo de confirmación para eliminar un nadador del equipo. */
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

    /**
     * Muestra un diálogo con dos campos para editar el nombre y los apellidos
     * de una ficha de nadador existente.
     */
    private fun mostrarDialogoEditarNadador(nadador: com.swimming.app.domain.model.NadadorEquipo) {
        // Contenedor vertical que apila los dos EditText.
        val contenedor = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
        }

        val inputNombre = android.widget.EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            hint = "Nombre"
            setText(nadador.nombre)
        }
        val inputApellidos = android.widget.EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            hint = "Apellidos"
            setText(nadador.apellidos)
        }

        contenedor.addView(inputNombre)
        contenedor.addView(inputApellidos)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Editar nadador")
            .setView(contenedor)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNombre = inputNombre.text.toString().trim()
                val nuevosApellidos = inputApellidos.text.toString().trim()
                val idEquipo = sessionManager.getEquipoId()

                if (nuevoNombre.isEmpty() || nuevosApellidos.isEmpty()) {
                    Toast.makeText(requireContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                } else if (idEquipo != null) {
                    viewModel.actualizarNadador(nadador.idNadadorEquipo, nuevoNombre, nuevosApellidos, idEquipo)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /** Observa la lista de nadadores del equipo. */
    private fun observarNadadores() {
        viewModel.nadadores.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> adapter.submitList(result.data)
                is NetworkResult.Error -> Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                is NetworkResult.Loading -> {}
            }
        }
    }

    /** Observa los datos del equipo y actualiza la cabecera. */
    private fun observarEquipo() {
        viewModel.equipo.observe(viewLifecycleOwner) { result ->
            if (result is NetworkResult.Success) {
                nombreEquipoActual = result.data.nombre
                binding.tvNombreEquipo.text = result.data.nombre
            }
        }
    }

    /** Observa el resultado de la actualización del nombre del equipo. */
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

    /**
     * Observa el resultado de la eliminación del equipo.
     * Al éxito, limpia la sesión local para que el entrenador vuelva al estado "sin equipo".
     */
    private fun observarEliminacionEquipo() {
        viewModel.equipoEliminado.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    Toast.makeText(requireContext(), "Equipo eliminado", Toast.LENGTH_SHORT).show()
                    // Se limpia el equipoId de la sesión local para que aparezca el botón de crear.
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

    /** Observa el resultado de la eliminación de un nadador. */
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

    /** Observa el resultado de la actualización de un nadador del equipo. */
    private fun observarActualizacionNadador() {
        viewModel.nadadorActualizado.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    Toast.makeText(requireContext(), "Nadador actualizado", Toast.LENGTH_SHORT).show()
                    cargarDatos()  // refresca la lista
                }
                is NetworkResult.Error -> Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                is NetworkResult.Loading -> {}
            }
        }
    }

    /**
     * Decide qué datos cargar según el estado del usuario:
     *   - Si es nadador sin equipo y todavía estamos en EquipoFragment,
     *     lo redirige a la pantalla de ingresar código.
     *   - Si tiene equipo, carga los nadadores y los datos del equipo.
     *
     * Comprobamos el destino actual antes de navegar para evitar el crash
     * que se producía si onResume() se lanzaba mientras el usuario ya estaba
     * en IngresarEquipoFragment (la acción de navegación no existe desde ahí
     * hacia sí misma).
     */
    private fun cargarDatos() {
        val idEquipo = sessionManager.getEquipoId()
        val esEntrenador = sessionManager.esEntrenador()
        val navController = findNavController()
        // Solo navegamos si seguimos en la pantalla de equipo.
        val seguimosEnEquipo = navController.currentDestination?.id == R.id.equipoFragment

        when {
            !esEntrenador && idEquipo == null && seguimosEnEquipo -> {
                navController.navigate(R.id.action_equipo_to_ingresarEquipo)
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