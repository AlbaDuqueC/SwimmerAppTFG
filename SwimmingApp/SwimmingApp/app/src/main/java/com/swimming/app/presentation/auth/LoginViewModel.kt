package com.swimming.app.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.swimming.app.utils.NetworkResult
import com.swimming.app.utils.SessionManager
import com.swimming.app.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginResult = MutableLiveData<NetworkResult<Boolean>>()
    val loginResult: LiveData<NetworkResult<Boolean>> = _loginResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = NetworkResult.Loading
            try {
                val resultado = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val user = resultado.user
                if (user != null) {
                    sessionManager.guardarSesion(
                        id = user.uid.hashCode(),
                        email = user.email ?: "",
                        rol = Constants.ROL_NADADOR,
                        nombre = user.displayName ?: "",
                        apellidos = "",
                        equipoId = null
                    )
                    _loginResult.value = NetworkResult.Success(true)
                } else {
                    _loginResult.value = NetworkResult.Error("Error al iniciar sesión")
                }
            } catch (e: Exception) {
                _loginResult.value = NetworkResult.Error("Email o contraseña incorrectos")
            }
        }
    }
}