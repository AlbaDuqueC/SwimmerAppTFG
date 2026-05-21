package com.swimming.app.presentation.home

import androidx.lifecycle.*
import com.swimming.app.domain.model.Rutina
import com.swimming.app.domain.usecase.rutina.ObtenerRutinasUseCase
import com.swimming.app.utils.NetworkResult
import com.swimming.app.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel de la pantalla principal (Home).
 * Carga todas las rutinas del usuario activo y las ordena cronológicamente
 * para que la vista pueda mostrar las más cercanas primero.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val obtenerRutinas: ObtenerRutinasUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _rutinas = MutableLiveData<NetworkResult<List<Rutina>>>()
    val rutinas: LiveData<NetworkResult<List<Rutina>>> = _rutinas

    /**
     * Carga las rutinas asociadas al usuario activo
     * y las ordena por fecha de la más cercana a la más lejana.
     */
    fun cargarRutinas() {
        viewModelScope.launch {
            _rutinas.value = NetworkResult.Loading
            val resultado = obtenerRutinas(sessionManager.getUserId())
            _rutinas.value = if (resultado is NetworkResult.Success) {
                NetworkResult.Success(resultado.data.sortedBy { it.fecha })
            } else resultado
        }
    }
}