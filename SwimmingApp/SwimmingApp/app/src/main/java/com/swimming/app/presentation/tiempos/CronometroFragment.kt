package com.swimming.app.presentation.tiempos

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.swimming.app.R
import com.swimming.app.databinding.FragmentCronometroBinding
import com.swimming.app.domain.model.MarcaDeTiempo
import com.swimming.app.domain.usecase.marcadetiempo.CrearMarcaUseCase
import com.swimming.app.utils.NetworkResult
import com.swimming.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Pantalla del cronómetro para medir y guardar tiempos en directo.
 * Permite iniciar, pausar, reanudar y resetear el cronómetro,
 * y guardar el tiempo medido como una marca cuando el usuario lo decide.
 */
@AndroidEntryPoint
class CronometroFragment : Fragment() {

    private var _binding: FragmentCronometroBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CronometroViewModel by viewModels()

    @Inject lateinit var sessionManager: SessionManager

    // Estado interno del cronómetro.
    private var corriendo = false
    private var tiempoInicio = 0L     // Marca de tiempo del último arranque.
    private var tiempoAcumulado = 0L  // Tiempo acumulado de pausas anteriores.
    private val handler = Handler(Looper.getMainLooper())

    private var prueba: String = ""
    private var idNadadorEquipoOverride: Int = -1

    /**
     * Runnable que se ejecuta cada 10 ms para actualizar el display del cronómetro.
     * Suma el tiempo acumulado más el tiempo transcurrido desde el último arranque.
     */
    private val actualizador = object : Runnable {
        override fun run() {
            val transcurrido = tiempoAcumulado + (System.currentTimeMillis() - tiempoInicio)
            actualizarDisplay(transcurrido)
            handler.postDelayed(this, 10)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCronometroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recupera los datos pasados desde la pantalla anterior.
        prueba = arguments?.getString("prueba") ?: ""
        idNadadorEquipoOverride = arguments?.getInt("idNadadorEquipo", -1) ?: -1
        viewModel.descripcion = prueba.ifEmpty { "Entrenamiento" }

        binding.tvNadadorPrueba.text = prueba.ifEmpty { "Prueba" }

        actualizarDisplay(0)
        binding.btnPlayStop.setImageResource(android.R.drawable.ic_media_play)
        binding.btnPlayStop.setOnClickListener { toggleCronometro() }
        binding.btnReset.setOnClickListener { resetCronometro() }
        binding.btnCrear.setOnClickListener { guardarMarca() }

        observarResultado()

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    /** Listener de la flecha de atrás de la action bar. */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            findNavController().popBackStack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Alterna entre iniciar y pausar el cronómetro.
     * Al pausarlo, se guarda el tiempo acumulado para reanudarlo desde ahí más tarde.
     */
    private fun toggleCronometro() {
        if (corriendo) {
            handler.removeCallbacks(actualizador)
            tiempoAcumulado += System.currentTimeMillis() - tiempoInicio
            corriendo = false
            binding.btnPlayStop.setImageResource(android.R.drawable.ic_media_play)
        } else {
            tiempoInicio = System.currentTimeMillis()
            handler.post(actualizador)
            corriendo = true
            binding.btnPlayStop.setImageResource(android.R.drawable.ic_media_pause)
        }
    }

    /** Resetea el cronómetro a 00:00. */
    private fun resetCronometro() {
        handler.removeCallbacks(actualizador)
        corriendo = false
        tiempoAcumulado = 0L
        actualizarDisplay(0)
        binding.btnPlayStop.setImageResource(android.R.drawable.ic_media_play)
    }

    /** Convierte el tiempo en milisegundos a un texto "MM:SS" para mostrar en pantalla. */
    private fun actualizarDisplay(ms: Long) {
        val minutos = (ms / 60000) % 60
        val segundos = (ms / 1000) % 60
        binding.tvTiempo.text = String.format("%02d:%02d", minutos, segundos)
    }

    /**
     * Pausa el cronómetro si está corriendo y guarda el tiempo final como una marca
     * en el formato "HH:MM:SS.cc" que la API entiende.
     */
    private fun guardarMarca() {
        if (tiempoAcumulado == 0L && !corriendo) {
            Toast.makeText(requireContext(), "Arranca el cronómetro primero", Toast.LENGTH_SHORT).show()
            return
        }
        if (corriendo) {
            handler.removeCallbacks(actualizador)
            tiempoAcumulado += System.currentTimeMillis() - tiempoInicio
            corriendo = false
            binding.btnPlayStop.setImageResource(android.R.drawable.ic_media_play)
        }
        val ms = tiempoAcumulado
        val horas = ms / 3600000
        val minutos = (ms / 60000) % 60
        val segundos = (ms / 1000) % 60
        val centesimas = (ms / 10) % 100
        val tiempo = String.format("%02d:%02d:%02d.%02d", horas, minutos, segundos, centesimas)
        viewModel.guardarMarca(tiempo, idNadadorEquipoOverride.takeIf { it != -1 })
    }

    /**
     * Observa el resultado del guardado de la marca y navega a la pantalla apropiada:
     *   - Vuelve a equipo si era flujo entrenador.
     *   - Vuelve a tiempos si era flujo nadador.
     */
    private fun observarResultado() {
        viewModel.marcaGuardada.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    Toast.makeText(requireContext(), "Marca guardada", Toast.LENGTH_SHORT).show()
                    val destino = if (idNadadorEquipoOverride != -1) R.id.equipoFragment else R.id.tiemposFragment
                    findNavController().popBackStack(destino, false)
                }
                is NetworkResult.Error ->
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                is NetworkResult.Loading -> {}
                else -> {}
            }
        }
    }

    /** Pausa el cronómetro al salir momentáneamente de la pantalla. */
    override fun onPause() {
        super.onPause()
        if (corriendo) {
            handler.removeCallbacks(actualizador)
            tiempoAcumulado += System.currentTimeMillis() - tiempoInicio
            corriendo = false
        }
    }

    /** Cancela el actualizador y libera el binding al destruirse la vista. */
    override fun onDestroyView() {
        handler.removeCallbacks(actualizador)
        super.onDestroyView()
        _binding = null
    }
}

/**
 * ViewModel del cronómetro. Encapsula la lógica de guardado de la marca
 * decidiendo qué IDs enviar según si el flujo es de nadador o de entrenador.
 */
@HiltViewModel
class CronometroViewModel @Inject constructor(
    private val crearMarca: CrearMarcaUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _marcaGuardada = MutableLiveData<NetworkResult<MarcaDeTiempo>>()
    val marcaGuardada: LiveData<NetworkResult<MarcaDeTiempo>> = _marcaGuardada

    /** Descripción de la marca (estilo + distancia). Se establece desde el fragment. */
    var descripcion: String = "Entrenamiento"

    /**
     * Guarda una marca de tiempo decidiendo qué IDs enviar a la API:
     *   - Si llega idNadadorEquipoOverride: flujo ENTRENADOR. La marca se asigna a ese nadador
     *     con idNadador = null porque no la registró el propio nadador.
     *   - Si no llega: flujo NADADOR. Se usan los IDs guardados en la sesión local.
     */
    fun guardarMarca(tiempo: String, idNadadorEquipoOverride: Int? = null) {
        viewModelScope.launch {
            _marcaGuardada.value = NetworkResult.Loading

            val idNadadorEquipo: Int?
            val idNadador: Int?

            if (idNadadorEquipoOverride != null) {
                // Flujo entrenador: la marca se asigna a un nadador del equipo.
                idNadadorEquipo = idNadadorEquipoOverride
                idNadador = null
            } else {
                // Flujo nadador: usa los IDs de la sesión.
                idNadadorEquipo = sessionManager.getIdNadadorEquipo().takeIf { it != -1 }
                val nadadorId = sessionManager.getIdNadador()
                if (nadadorId == -1) {
                    _marcaGuardada.value = NetworkResult.Error("Sesión inválida. Vuelve a iniciar sesión.")
                    return@launch
                }
                idNadador = nadadorId
            }

            val resultado = crearMarca(tiempo, descripcion, idNadadorEquipo, idNadador)
            _marcaGuardada.value = resultado
        }
    }
}