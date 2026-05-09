package com.swimming.app.presentation.perfil

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swimming.app.domain.usecase.entrenador.ActualizarEntrenadorUseCase
import com.swimming.app.domain.usecase.entrenador.EliminarEntrenadorUseCase
import com.swimming.app.domain.usecase.nadador.ActualizarNadadorUseCase
import com.swimming.app.domain.usecase.nadador.EliminarNadadorUseCase
import com.swimming.app.utils.NetworkResult
import com.swimming.app.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** ViewModel de editar perfil. Llama al UseCase correcto según si es nadador o entrenador. */
@HiltViewModel
class EditarPerfilViewModel @Inject constructor(
    private val actualizarNadador: ActualizarNadadorUseCase,
    private val actualizarEntrenador: ActualizarEntrenadorUseCase,
    private val eliminarNadador: EliminarNadadorUseCase,
    private val eliminarEntrenador: EliminarEntrenadorUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _actualizacionResult = MutableLiveData<NetworkResult<Boolean>>()
    val actualizacionResult: LiveData<NetworkResult<Boolean>> = _actualizacionResult

    private val _eliminacionResult = MutableLiveData<NetworkResult<Boolean>>()
    val eliminacionResult: LiveData<NetworkResult<Boolean>> = _eliminacionResult

    fun actualizarPerfil(id: Int, nombre: String, apellidos: String, esEntrenador: Boolean) {
        viewModelScope.launch {
            _actualizacionResult.value = NetworkResult.Loading
            val resultado = if (esEntrenador) {
                actualizarEntrenador(id, nombre, apellidos)
            } else {
                actualizarNadador(id, nombre, apellidos)
            }
            _actualizacionResult.value = when (resultado) {
                is NetworkResult.Success -> {
                    // Actualizar sesión local con los nuevos datos
                    sessionManager.actualizarNombreApellidos(nombre, apellidos)
                    NetworkResult.Success(true)
                }
                is NetworkResult.Error -> NetworkResult.Error(resultado.message)
                else -> NetworkResult.Error("Error desconocido")
            }
        }
    }

    fun eliminarCuenta(id: Int, esEntrenador: Boolean) {
        viewModelScope.launch {
            _eliminacionResult.value = NetworkResult.Loading
            val resultado = if (esEntrenador) {
                eliminarEntrenador(id)
            } else {
                eliminarNadador(id)
            }
            _eliminacionResult.value = resultado
        }
    }
}