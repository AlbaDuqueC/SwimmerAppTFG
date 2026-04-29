package com.swimming.app.presentation.equipo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.swimming.app.databinding.FragmentIngresarEquipoBinding
import com.swimming.app.domain.model.Nadador
import com.swimming.app.domain.usecase.nadador.VincularNadadorUseCase
import com.swimming.app.utils.NetworkResult
import com.swimming.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class IngresarEquipoFragment : Fragment() {

    private var _binding: FragmentIngresarEquipoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: IngresarEquipoViewModel by viewModels()

    @Inject lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentIngresarEquipoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnIngresar.setOnClickListener {
            val codigoStr = binding.etCodigo.text.toString().trim()
            val codigo = codigoStr.toIntOrNull()
            if (codigo == null || codigoStr.length != 6) {
                Toast.makeText(requireContext(), "El código debe tener 6 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val idNadador = sessionManager.getIdNadador()
            if (idNadador == -1) {
                Toast.makeText(requireContext(), "Sesión inválida. Vuelve a iniciar sesión.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnIngresar.isEnabled = false
            viewModel.vincular(idNadador, codigo)
        }

        viewModel.resultado.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    // Actualizamos la sesión local con el nuevo equipo y NadadorEquipo
                    val nadador = result.data
                    sessionManager.guardarIdNadadorEquipo(nadador.idNadadorEquipo ?: -1)
                    nadador.idEquipo?.let { sessionManager.guardarEquipoId(it) }

                    Toast.makeText(requireContext(), "Te has unido al equipo correctamente", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is NetworkResult.Error -> {
                    binding.btnIngresar.isEnabled = true
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
                is NetworkResult.Loading -> { /* opcional: spinner */ }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

@HiltViewModel
class IngresarEquipoViewModel @Inject constructor(
    private val vincular: VincularNadadorUseCase
) : ViewModel() {

    private val _resultado = MutableLiveData<NetworkResult<Nadador>>()
    val resultado: LiveData<NetworkResult<Nadador>> = _resultado

    fun vincular(idNadador: Int, codigo: Int) {
        viewModelScope.launch {
            _resultado.value = NetworkResult.Loading
            _resultado.value = vincular.invoke(idNadador, codigo)
        }
    }
}