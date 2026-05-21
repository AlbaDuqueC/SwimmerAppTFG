package com.swimming.app.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.swimming.app.domain.usecase.auth.RegisterEntrenadorUseCase
import com.swimming.app.domain.usecase.auth.RegisterNadadorUseCase
import com.swimming.app.utils.NetworkResult
import com.swimming.app.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * ViewModel de la pantalla de registro.
 * Coordina la creación del usuario en Firebase Auth y en la API,
 * con rollback automático si alguno de los dos pasos falla.
 *
 * Flujo:
 *   1. Crear usuario en Firebase.
 *   2. Actualizar el nombre en el perfil de Firebase.
 *   3. Registrar al usuario en la API según el rol elegido.
 *   4. Enviar email de verificación.
 *   5. Cerrar sesión hasta que el usuario verifique el email.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerNadador: RegisterNadadorUseCase,
    private val registerEntrenador: RegisterEntrenadorUseCase,
    private val sessionManager: SessionManager,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _registerResult = MutableLiveData<NetworkResult<Boolean>>()
    val registerResult: LiveData<NetworkResult<Boolean>> = _registerResult

    /**
     * Registra al usuario en Firebase y en la API en una transacción coordinada.
     * Si la creación en la API falla, elimina el usuario de Firebase para mantener consistencia.
     */
    fun registrar(nombre: String, apellidos: String, email: String, password: String, rol: String) {
        viewModelScope.launch {
            _registerResult.value = NetworkResult.Loading
            try {
                // 1. Crear usuario en Firebase Auth.
                val firebaseResultado = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val user = firebaseResultado.user
                if (user == null) {
                    _registerResult.value = NetworkResult.Error("Error al crear la cuenta en Firebase")
                    return@launch
                }

                // 2. Actualizar el nombre completo en el perfil de Firebase.
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName("$nombre $apellidos")
                    .build()
                user.updateProfile(profileUpdate).await()

                // 3. Registrar al usuario en la API según el rol seleccionado.
                // Si falla, se borra el usuario de Firebase para no dejar cuentas huérfanas.
                if (rol == "Entrenador") {
                    val resultado = registerEntrenador(nombre, apellidos, email, password)
                    if (resultado is NetworkResult.Error) {
                        user.delete().await()
                        _registerResult.value = NetworkResult.Error("Error al registrar en el servidor")
                        return@launch
                    }
                } else {
                    val resultado = registerNadador(nombre, apellidos, email, password)
                    if (resultado is NetworkResult.Error) {
                        user.delete().await()
                        _registerResult.value = NetworkResult.Error("Error al registrar en el servidor")
                        return@launch
                    }
                }

                // 4. Enviar email de verificación a la dirección registrada.
                user.sendEmailVerification().await()

                // 5. Cerrar sesión: el usuario debe verificar el email antes de poder entrar.
                firebaseAuth.signOut()
                sessionManager.cerrarSesion()

                _registerResult.value = NetworkResult.Success(true)

            } catch (e: Exception) {
                _registerResult.value = NetworkResult.Error(e.message ?: "Error al crear la cuenta")
            }
        }
    }
}