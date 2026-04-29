package com.swimming.app.presentation.calendario

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swimming.app.domain.model.Rutina
import com.swimming.app.domain.usecase.rutina.CrearRutinaUseCase
import com.swimming.app.domain.usecase.rutina.ObtenerRutinasUseCase
import com.swimming.app.utils.NetworkResult
import com.swimming.app.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarioViewModel @Inject constructor(
    private val obtenerRutinas: ObtenerRutinasUseCase,
    private val crearRutina: CrearRutinaUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _rutinas = MutableLiveData<NetworkResult<List<Rutina>>>()
    val rutinas: LiveData<NetworkResult<List<Rutina>>> = _rutinas

    private val _eventoCreado = MutableLiveData<NetworkResult<Rutina>>()
    val eventoCreado: LiveData<NetworkResult<Rutina>> = _eventoCreado

    fun cargarRutinas() {
        viewModelScope.launch {
            _rutinas.value = NetworkResult.Loading
            _rutinas.value = obtenerRutinas(sessionManager.getUserId())
        }
    }

    /** fechaIso: "2026-05-15T19:30:00" (ya con hora). */
    fun crearEvento(contenido: String, fechaIso: String) {
        viewModelScope.launch {
            _eventoCreado.value = NetworkResult.Loading
            _eventoCreado.value = crearRutina(contenido, fechaIso, true, sessionManager.getUserId())
        }
    }
}