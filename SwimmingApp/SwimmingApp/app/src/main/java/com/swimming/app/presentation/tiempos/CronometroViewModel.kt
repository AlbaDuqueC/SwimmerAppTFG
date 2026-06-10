package com.swimming.app.presentation.tiempos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swimming.app.domain.model.MarcaDeTiempo
import com.swimming.app.domain.usecase.marcadetiempo.CrearMarcaUseCase
import com.swimming.app.utils.NetworkResult
import com.swimming.app.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel del cronómetro y de la pantalla de crear tiempo.
 * Encapsula la lógica de guardado de la marca decidiendo qué IDs
 * enviar según si el flujo es de nadador o de entrenador.
 *
 * Se extrae del CronometroFragment original a un archivo propio
 * para que pueda ser inyectado por Hilt en los Composables.
 */
@HiltViewModel
class CronometroViewModel @Inject constructor(
    private val crearMarca: CrearMarcaUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    // Resultado del guardado de la marca, observado por la UI para navegar o mostrar error.
    private val _marcaGuardada = MutableLiveData<NetworkResult<MarcaDeTiempo>>()
    val marcaGuardada: LiveData<NetworkResult<MarcaDeTiempo>> = _marcaGuardada

    /** Descripción de la marca (estilo + distancia). Se establece desde la pantalla. */
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

            _marcaGuardada.value = crearMarca(tiempo, descripcion, idNadadorEquipo, idNadador)
        }
    }
}
