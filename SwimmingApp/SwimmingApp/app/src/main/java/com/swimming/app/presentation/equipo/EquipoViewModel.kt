package com.swimming.app.presentation.equipo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swimming.app.domain.model.Equipo
import com.swimming.app.domain.model.NadadorEquipo
import com.swimming.app.domain.usecase.equipo.CrearEquipoUseCase
import com.swimming.app.domain.usecase.nadadorequipo.CrearNadadorEquipoUseCase
import com.swimming.app.domain.usecase.nadadorequipo.ObtenerNadadoresEquipoUseCase
import com.swimming.app.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** ViewModel del equipo. Gestiona la lista de nadadores y la creación de equipos. */
@HiltViewModel
class EquipoViewModel @Inject constructor(
    private val obtenerNadadores: ObtenerNadadoresEquipoUseCase,
    private val crearNadadorEquipo: CrearNadadorEquipoUseCase,
    private val crearEquipo: CrearEquipoUseCase
) : ViewModel() {

    private val _nadadores = MutableLiveData<NetworkResult<List<NadadorEquipo>>>()
    val nadadores: LiveData<NetworkResult<List<NadadorEquipo>>> = _nadadores

    private val _equipoCreado = MutableLiveData<NetworkResult<Equipo>>()
    val equipoCreado: LiveData<NetworkResult<Equipo>> = _equipoCreado

    private val _nadadorCreado = MutableLiveData<NetworkResult<NadadorEquipo>>()
    val nadadorCreado: LiveData<NetworkResult<NadadorEquipo>> = _nadadorCreado

    fun cargarNadadores(idEquipo: Int) {
        viewModelScope.launch {
            _nadadores.value = NetworkResult.Loading
            val resultado = obtenerNadadores(idEquipo)
            _nadadores.value = resultado
        }
    }

    fun crearNuevoNadador(nombre: String, apellidos: String, idEquipo: Int) {
        viewModelScope.launch {
            _nadadorCreado.value = NetworkResult.Loading
            val resultado = crearNadadorEquipo(nombre, apellidos, idEquipo)
            _nadadorCreado.value = resultado
        }
    }

    fun crearNuevoEquipo(nombre: String) {
        viewModelScope.launch {
            _equipoCreado.value = NetworkResult.Loading
            val resultado = crearEquipo(nombre)
            _equipoCreado.value = resultado
        }
    }
}
