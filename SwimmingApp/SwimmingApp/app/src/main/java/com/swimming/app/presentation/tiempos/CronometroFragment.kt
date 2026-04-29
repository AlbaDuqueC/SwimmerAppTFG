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

@AndroidEntryPoint
class CronometroFragment : Fragment() {

    private var _binding: FragmentCronometroBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CronometroViewModel by viewModels()

    @Inject lateinit var sessionManager: SessionManager

    private var corriendo = false
    private var tiempoInicio = 0L
    private var tiempoAcumulado = 0L
    private val handler = Handler(Looper.getMainLooper())

    private var prueba: String = ""
    private var idNadadorEquipoOverride: Int = -1

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            findNavController().popBackStack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

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

    private fun resetCronometro() {
        handler.removeCallbacks(actualizador)
        corriendo = false
        tiempoAcumulado = 0L
        actualizarDisplay(0)
        binding.btnPlayStop.setImageResource(android.R.drawable.ic_media_play)
    }

    private fun actualizarDisplay(ms: Long) {
        val minutos = (ms / 60000) % 60
        val segundos = (ms / 1000) % 60
        binding.tvTiempo.text = String.format("%02d:%02d", minutos, segundos)
    }

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

    override fun onPause() {
        super.onPause()
        if (corriendo) {
            handler.removeCallbacks(actualizador)
            tiempoAcumulado += System.currentTimeMillis() - tiempoInicio
            corriendo = false
        }
    }

    override fun onDestroyView() {
        handler.removeCallbacks(actualizador)
        super.onDestroyView()
        _binding = null
    }
}

@HiltViewModel
class CronometroViewModel @Inject constructor(
    private val crearMarca: CrearMarcaUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _marcaGuardada = MutableLiveData<NetworkResult<MarcaDeTiempo>>()
    val marcaGuardada: LiveData<NetworkResult<MarcaDeTiempo>> = _marcaGuardada

    var descripcion: String = "Entrenamiento"

    /**
     * Guarda una marca de tiempo.
     * - Si idNadadorEquipoOverride viene → flujo ENTRENADOR: la marca se asigna a ese nadador del equipo,
     *   con idNadador = null porque no la registró el propio nadador.
     * - Si no viene → flujo NADADOR: usa los IDs de la sesión (idNadador siempre, idNadadorEquipo si lo tiene).
     */
    fun guardarMarca(tiempo: String, idNadadorEquipoOverride: Int? = null) {
        viewModelScope.launch {
            _marcaGuardada.value = NetworkResult.Loading

            val idNadadorEquipo: Int?
            val idNadador: Int?

            if (idNadadorEquipoOverride != null) {
                // Flujo entrenador
                idNadadorEquipo = idNadadorEquipoOverride
                idNadador = null
            } else {
                // Flujo nadador
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