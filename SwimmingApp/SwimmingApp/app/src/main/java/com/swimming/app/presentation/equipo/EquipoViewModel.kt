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
import com.swimming.app.domain.usecase.nadadorequipo.ActualizarNadadorEquipoUseCase

/**
 * ViewModel central de las pantallas relacionadas con el equipo.
 * Gestiona la lista de nadadores y el ciclo de vida completo del equipo
 * (crear, consultar, editar, eliminar), tanto del equipo como de las fichas de nadadores.
 *
 * Cada operación expone su propio LiveData para que las pantallas que lo necesiten
 * puedan observar solo los cambios que les interesan.
 */
@HiltViewModel
class EquipoViewModel @Inject constructor(
    private val obtenerNadadores: ObtenerNadadoresEquipoUseCase,
    private val crearNadadorEquipo: CrearNadadorEquipoUseCase,
    private val obtenerEquipo: ObtenerEquipoUseCase,
    private val crearEquipo: CrearEquipoUseCase,
    private val actualizarEquipoUC: ActualizarEquipoUseCase,
    private val eliminarEquipoUC: EliminarEquipoUseCase,
    private val eliminarNadadorEquipo: EliminarNadadorEquipoUseCase,
    private val actualizarNadadorEquipoUC: ActualizarNadadorEquipoUseCase
) : ViewModel() {

    // Lista de nadadores del equipo activo.
    private val _nadadores = MutableLiveData<NetworkResult<List<NadadorEquipo>>>()
    val nadadores: LiveData<NetworkResult<List<NadadorEquipo>>> = _nadadores

    // Datos del equipo actual.
    private val _equipo = MutableLiveData<NetworkResult<Equipo>>()
    val equipo: LiveData<NetworkResult<Equipo>> = _equipo

    // Resultado de creación de un nuevo equipo.
    private val _equipoCreado = MutableLiveData<NetworkResult<Equipo>>()
    val equipoCreado: LiveData<NetworkResult<Equipo>> = _equipoCreado

    // Resultado de actualización del nombre del equipo.
    private val _equipoActualizado = MutableLiveData<NetworkResult<Equipo>>()
    val equipoActualizado: LiveData<NetworkResult<Equipo>> = _equipoActualizado

    // Resultado de eliminación del equipo.
    private val _equipoEliminado = MutableLiveData<NetworkResult<Boolean>>()
    val equipoEliminado: LiveData<NetworkResult<Boolean>> = _equipoEliminado

    // Resultado de creación de un nuevo nadador en el equipo.
    private val _nadadorCreado = MutableLiveData<NetworkResult<NadadorEquipo>>()

    // Resultado de actualización de un nadador del equipo.
    private val _nadadorActualizado = MutableLiveData<NetworkResult<NadadorEquipo>>()
    val nadadorActualizado: LiveData<NetworkResult<NadadorEquipo>> = _nadadorActualizado

    val nadadorCreado: LiveData<NetworkResult<NadadorEquipo>> = _nadadorCreado

    // Resultado de eliminación de un nadador del equipo.
    private val _nadadorEliminado = MutableLiveData<NetworkResult<Boolean>>()
    val nadadorEliminado: LiveData<NetworkResult<Boolean>> = _nadadorEliminado

    /** Carga la lista de nadadores del equipo indicado. */
    fun cargarNadadores(idEquipo: Int) {
        viewModelScope.launch {
            _nadadores.value = NetworkResult.Loading
            _nadadores.value = obtenerNadadores(idEquipo)
        }
    }

    /** Carga los datos del equipo indicado. */
    fun cargarEquipo(idEquipo: Int) {
        viewModelScope.launch {
            _equipo.value = NetworkResult.Loading
            _equipo.value = obtenerEquipo(idEquipo)
        }
    }

    /** Crea una nueva ficha de nadador dentro del equipo. */
    fun crearNuevoNadador(nombre: String, apellidos: String, idEquipo: Int) {
        viewModelScope.launch {
            _nadadorCreado.value = NetworkResult.Loading
            _nadadorCreado.value = crearNadadorEquipo(nombre, apellidos, idEquipo)
        }
    }

    /** Crea un nuevo equipo, opcionalmente vinculado al entrenador creador. */
    fun crearNuevoEquipo(nombre: String, idEntrenador: Int?) {
        viewModelScope.launch {
            _equipoCreado.value = NetworkResult.Loading
            _equipoCreado.value = crearEquipo(nombre, idEntrenador)
        }
    }

    /** Actualiza el nombre del equipo existente. */
    fun actualizarNombreEquipo(idEquipo: Int, nuevoNombre: String) {
        viewModelScope.launch {
            _equipoActualizado.value = NetworkResult.Loading
            _equipoActualizado.value = actualizarEquipoUC(idEquipo, nuevoNombre)
        }
    }

    /** Elimina lógicamente el equipo indicado. */
    fun eliminarEquipo(idEquipo: Int) {
        viewModelScope.launch {
            _equipoEliminado.value = NetworkResult.Loading
            _equipoEliminado.value = eliminarEquipoUC(idEquipo)
        }
    }

    /** Elimina lógicamente una ficha de nadador del equipo. */
    fun eliminarNadador(idNadadorEquipo: Int) {
        viewModelScope.launch {
            _nadadorEliminado.value = NetworkResult.Loading
            _nadadorEliminado.value = eliminarNadadorEquipo(idNadadorEquipo)
        }
    }

    /** Actualiza el nombre y apellidos de una ficha de nadador existente. */
    fun actualizarNadador(id: Int, nombre: String, apellidos: String, idEquipo: Int) {
        viewModelScope.launch {
            _nadadorActualizado.value = NetworkResult.Loading
            _nadadorActualizado.value = actualizarNadadorEquipoUC(id, nombre, apellidos, idEquipo)
        }
    }
}