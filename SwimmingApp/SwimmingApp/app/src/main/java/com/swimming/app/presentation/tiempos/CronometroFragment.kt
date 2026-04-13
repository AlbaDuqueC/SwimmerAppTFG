package com.swimming.app.presentation.tiempos

import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
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
    private var corriendo = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCronometroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnPlayStop.setOnClickListener { toggleCronometro() }
        binding.btnCrear.setOnClickListener { guardarMarca() }
        viewModel.marcaGuardada.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> { Toast.makeText(requireContext(), "Marca guardada", Toast.LENGTH_SHORT).show(); parentFragmentManager.popBackStack() }
                is NetworkResult.Error -> Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }
    }

    private fun toggleCronometro() {
        if (corriendo) {
            binding.cronometro.stop()
            corriendo = false
        } else {
            binding.cronometro.base = SystemClock.elapsedRealtime()
            binding.cronometro.start()
            corriendo = true
        }
    }

    private fun guardarMarca() {
        if (corriendo) binding.cronometro.stop()
        val ms = SystemClock.elapsedRealtime() - binding.cronometro.base
        val tiempo = String.format("%02d:%02d:%02d", ms / 3600000, (ms / 60000) % 60, (ms / 1000) % 60)
        viewModel.guardarMarca(tiempo)
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

@HiltViewModel
class CronometroViewModel @Inject constructor(
    private val crearMarca: CrearMarcaUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _marcaGuardada = MutableLiveData<NetworkResult<MarcaDeTiempo>>()
    val marcaGuardada: LiveData<NetworkResult<MarcaDeTiempo>> = _marcaGuardada

    var idNadadorEquipo: Int = -1
    var descripcion: String = "Entrenamiento"

    fun guardarMarca(tiempo: String) {
        viewModelScope.launch {
            _marcaGuardada.value = NetworkResult.Loading
            val resultado = crearMarca(tiempo, descripcion, idNadadorEquipo, sessionManager.getUserId())
            _marcaGuardada.value = resultado
        }
    }
}
