package com.swimming.app.presentation.calendario

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swimming.app.domain.model.Rutina
import com.swimming.app.domain.usecase.rutina.ObtenerRutinasUseCase
import com.swimming.app.utils.NetworkResult
import com.swimming.app.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** ViewModel del calendario. Carga las rutinas para marcar días con eventos. */
@HiltViewModel
class CalendarioViewModel @Inject constructor(
    private val obtenerRutinas: ObtenerRutinasUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _rutinas = MutableLiveData<NetworkResult<List<Rutina>>>()
    val rutinas: LiveData<NetworkResult<List<Rutina>>> = _rutinas

    fun cargarRutinas() {
        viewModelScope.launch {
            _rutinas.value = NetworkResult.Loading
            val resultado = obtenerRutinas(sessionManager.getUserId())
            _rutinas.value = resultado
        }
    }
}
