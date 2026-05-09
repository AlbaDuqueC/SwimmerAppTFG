package com.swimming.app.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.swimming.app.data.network.ApiService
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
    private val apiService: ApiService
) : ViewModel() {

    private val _loginResult = MutableLiveData<NetworkResult<Boolean>>()
    val loginResult: LiveData<NetworkResult<Boolean>> = _loginResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = NetworkResult.Loading
            try {
                val resultado = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val user = resultado.user

                if (user == null) {
                    _loginResult.value = NetworkResult.Error("No se pudo iniciar sesión.")
                    return@launch
                }

                // 1. Comprobar si el email está verificado
                user.reload().await()
                if (!user.isEmailVerified) {
                    try { user.sendEmailVerification().await() } catch (_: Exception) {}
                    firebaseAuth.signOut()
                    _loginResult.value = NetworkResult.Error(
                        "Tu correo no está verificado. Te hemos enviado un nuevo email — pulsa el enlace y vuelve a intentar."
                    )
                    return@launch
                }

                // 2. Buscar el usuario en la API por email — primero como nadador, si no, como entrenador
                val emailUsuario = user.email ?: ""
                val nombreFb = user.displayName ?: ""

                val resNadador = apiService.obtenerNadadorPorEmail(emailUsuario)
                if (resNadador.isSuccessful && resNadador.body()?.datos != null) {
                    val nadador = resNadador.body()!!.datos!!
                    sessionManager.guardarSesion(
                        id = nadador.idNadador,
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

                val resEntrenador = apiService.obtenerEntrenadorPorEmail(emailUsuario)
                if (resEntrenador.isSuccessful && resEntrenador.body()?.datos != null) {
                    val entrenador = resEntrenador.body()!!.datos!!
                    sessionManager.guardarSesion(
                        id = entrenador.idEntrenador,
                        email = entrenador.email,
                        rol = Constants.ROL_ENTRENADOR,
                        nombre = entrenador.nombre,
                        apellidos = entrenador.apellidos,
                        equipoId = entrenador.idEquipo
                    )
                    _loginResult.value = NetworkResult.Success(true)
                    return@launch
                }

                _loginResult.value = NetworkResult.Error("No se encontraron datos del usuario en el servidor.")

            } catch (e: Exception) {
                _loginResult.value = NetworkResult.Error("Error: ${e.localizedMessage}")
            }
        }
    }
}