package com.swimming.app.presentation.equipo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swimming.app.domain.model.Equipo
import com.swimming.app.domain.model.NadadorEquipo
import com.swimming.app.domain.usecase.equipo.ActualizarEquipoUseCase
import com.swimming.app.domain.usecase.equipo.CrearEquipoUseCase
import com.swimming.app.domain.usecase.equipo.EliminarEquipoUseCase
import com.swimming.app.domain.usecase.equipo.ObtenerEquipoUseCase
import com.swimming.app.domain.usecase.nadadorequipo.CrearNadadorEquipoUseCase
import com.swimming.app.domain.usecase.nadadorequipo.EliminarNadadorEquipoUseCase
import com.swimming.app.domain.usecase.nadadorequipo.ObtenerNadadoresEquipoUseCase
import com.swimming.app.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** ViewModel del equipo. Gestiona la lista de nadadores y el ciclo de vida del equipo. */
@HiltViewModel
class EquipoViewModel @Inject constructor(
    private val obtenerNadadores: ObtenerNadadoresEquipoUseCase,
    private val crearNadadorEquipo: CrearNadadorEquipoUseCase,
    private val obtenerEquipo: ObtenerEquipoUseCase,
    private val crearEquipo: CrearEquipoUseCase,
    private val actualizarEquipoUC: ActualizarEquipoUseCase,
    private val eliminarEquipoUC: EliminarEquipoUseCase,
    private val eliminarNadadorEquipo: EliminarNadadorEquipoUseCase
) : ViewModel() {

    private val _nadadores = MutableLiveData<NetworkResult<List<NadadorEquipo>>>()
    val nadadores: LiveData<NetworkResult<List<NadadorEquipo>>> = _nadadores

    private val _equipo = MutableLiveData<NetworkResult<Equipo>>()
    val equipo: LiveData<NetworkResult<Equipo>> = _equipo

    private val _equipoCreado = MutableLiveData<NetworkResult<Equipo>>()
    val equipoCreado: LiveData<NetworkResult<Equipo>> = _equipoCreado

    private val _equipoActualizado = MutableLiveData<NetworkResult<Equipo>>()
    val equipoActualizado: LiveData<NetworkResult<Equipo>> = _equipoActualizado

    private val _equipoEliminado = MutableLiveData<NetworkResult<Boolean>>()
    val equipoEliminado: LiveData<NetworkResult<Boolean>> = _equipoEliminado

    private val _nadadorCreado = MutableLiveData<NetworkResult<NadadorEquipo>>()
    val nadadorCreado: LiveData<NetworkResult<NadadorEquipo>> = _nadadorCreado

    private val _nadadorEliminado = MutableLiveData<NetworkResult<Boolean>>()
    val nadadorEliminado: LiveData<NetworkResult<Boolean>> = _nadadorEliminado

    fun cargarNadadores(idEquipo: Int) {
        viewModelScope.launch {
            _nadadores.value = NetworkResult.Loading
            _nadadores.value = obtenerNadadores(idEquipo)
        }
    }

    fun cargarEquipo(idEquipo: Int) {
        viewModelScope.launch {
            _equipo.value = NetworkResult.Loading
            _equipo.value = obtenerEquipo(idEquipo)
        }
    }

    fun crearNuevoNadador(nombre: String, apellidos: String, idEquipo: Int) {
        viewModelScope.launch {
            _nadadorCreado.value = NetworkResult.Loading
            _nadadorCreado.value = crearNadadorEquipo(nombre, apellidos, idEquipo)
        }
    }

    fun crearNuevoEquipo(nombre: String, idEntrenador: Int?) {
        viewModelScope.launch {
            _equipoCreado.value = NetworkResult.Loading
            _equipoCreado.value = crearEquipo(nombre, idEntrenador)
        }
    }

    fun actualizarNombreEquipo(idEquipo: Int, nuevoNombre: String) {
        viewModelScope.launch {
            _equipoActualizado.value = NetworkResult.Loading
            _equipoActualizado.value = actualizarEquipoUC(idEquipo, nuevoNombre)
        }
    }

    fun eliminarEquipo(idEquipo: Int) {
        viewModelScope.launch {
            _equipoEliminado.value = NetworkResult.Loading
            _equipoEliminado.value = eliminarEquipoUC(idEquipo)
        }
    }

    fun eliminarNadador(idNadadorEquipo: Int) {
        viewModelScope.launch {
            _nadadorEliminado.value = NetworkResult.Loading
            _nadadorEliminado.value = eliminarNadadorEquipo(idNadadorEquipo)
        }
    }
}