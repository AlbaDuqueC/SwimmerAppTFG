package com.swimming.app.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.swimming.app.domain.usecase.entrenador.ObtenerEntrenadorPorEmailUseCase
import com.swimming.app.domain.usecase.nadador.ObtenerNadadorPorEmailUseCase
import com.swimming.app.utils.Constants
import com.swimming.app.utils.NetworkResult
import com.swimming.app.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val sessionManager: SessionManager,
    private val obtenerNadadorPorEmail: ObtenerNadadorPorEmailUseCase,
    private val obtenerEntrenadorPorEmail: ObtenerEntrenadorPorEmailUseCase
) : ViewModel() {

    private val _loginResult = MutableLiveData<NetworkResult<Boolean>>()
    val loginResult: LiveData<NetworkResult<Boolean>> = _loginResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = NetworkResult.Loading
            try {
                // 1) Autenticar contra Firebase
                val firebaseUser = firebaseAuth.signInWithEmailAndPassword(email, password)
                    .await().user
                if (firebaseUser == null) {
                    _loginResult.value = NetworkResult.Error("Credenciales no válidas")
                    return@launch
                }

                // 2) Buscar primero como nadador
                val nadadorResult = obtenerNadadorPorEmail(email)
                if (nadadorResult is NetworkResult.Success) {
                    val nadador = nadadorResult.data
                    sessionManager.guardarSesion(
                        id = nadador.id,
                        email = nadador.email,
                        rol = Constants.ROL_NADADOR,
                        nombre = nadador.nombre,
                        apellidos = nadador.apellidos,
                        equipoId = nadador.idEquipo
                    )
                    sessionManager.guardarIdNadador(nadador.idNadador)
                    sessionManager.guardarIdNadadorEquipo(nadador.idNadadorEquipo ?: -1)
                    _loginResult.value = NetworkResult.Success(true)
                    return@launch
                }

                // 3) Si no es nadador, probar como entrenador
                val entrenadorResult = obtenerEntrenadorPorEmail(email)
                if (entrenadorResult is NetworkResult.Success) {
                    val entrenador = entrenadorResult.data
                    sessionManager.guardarSesion(
                        id = entrenador.id,
                        email = entrenador.email,
                        rol = Constants.ROL_ENTRENADOR,
                        nombre = entrenador.nombre,
                        apellidos = entrenador.apellidos,
                        equipoId = entrenador.idEquipoGestionado
                    )
                    _loginResult.value = NetworkResult.Success(true)
                    return@launch
                }

                // 4) No es ninguno
                _loginResult.value = NetworkResult.Error(
                    "No se encontró el usuario en el servidor. ¿Está registrado correctamente?"
                )
            } catch (e: Exception) {
                _loginResult.value = NetworkResult.Error("Error: ${e.localizedMessage}")
            }
        }
    }
}