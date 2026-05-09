package com.swimming.app.presentation.equipo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swimming.app.domain.model.Equipo
import com.swimming.app.domain.model.NadadorEquipo
import com.swimming.app.domain.usecase.equipo.CrearEquipoUseCase
import com.swimming.app.domain.usecase.nadadorequipo.CrearNadadorEquipoUseCase
import com.swimming.app.domain.usecase.nadadorequipo.EliminarNadadorEquipoUseCase
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
    private val crearEquipo: CrearEquipoUseCase,
    private val eliminarNadadorEquipo: EliminarNadadorEquipoUseCase
) : ViewModel() {

    private val _nadadores = MutableLiveData<NetworkResult<List<NadadorEquipo>>>()
    val nadadores: LiveData<NetworkResult<List<NadadorEquipo>>> = _nadadores

    private val _equipoCreado = MutableLiveData<NetworkResult<Equipo>>()
    val equipoCreado: LiveData<NetworkResult<Equipo>> = _equipoCreado

    private val _nadadorCreado = MutableLiveData<NetworkResult<NadadorEquipo>>()
    val nadadorCreado: LiveData<NetworkResult<NadadorEquipo>> = _nadadorCreado

    private val _nadadorEliminado = MutableLiveData<NetworkResult<Boolean>>()
    val nadadorEliminado: LiveData<NetworkResult<Boolean>> = _nadadorEliminado

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

    fun crearNuevoEquipo(nombre: String, idEntrenador: Int?) {
        viewModelScope.launch {
            _equipoCreado.value = NetworkResult.Loading
            val resultado = crearEquipo(nombre, idEntrenador)
            _equipoCreado.value = resultado
        }
    }

    fun eliminarNadador(idNadadorEquipo: Int) {
        viewModelScope.launch {
            _nadadorEliminado.value = NetworkResult.Loading
            val resultado = eliminarNadadorEquipo(idNadadorEquipo)
            _nadadorEliminado.value = resultado
        }
    }
}