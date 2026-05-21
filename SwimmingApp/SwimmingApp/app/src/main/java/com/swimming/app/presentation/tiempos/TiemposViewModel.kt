package com.swimming.app.presentation.tiempos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swimming.app.domain.model.MarcaDeTiempo
import com.swimming.app.domain.usecase.marcadetiempo.EliminarMarcaUseCase
import com.swimming.app.domain.usecase.marcadetiempo.ObtenerMarcasPorNadadorUseCase
import com.swimming.app.domain.usecase.marcadetiempo.ObtenerMarcasUseCase
import com.swimming.app.domain.usecase.nadadorequipo.ObtenerNadadoresEquipoUseCase
import com.swimming.app.utils.NetworkResult
import com.swimming.app.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel de la pantalla de tiempos.
 * Adapta el contenido a mostrar según el rol del usuario y permite eliminar marcas.
 */
@HiltViewModel
class TiemposViewModel @Inject constructor(
    private val obtenerMarcasPorEquipo: ObtenerMarcasUseCase,
    private val obtenerMarcasPorNadador: ObtenerMarcasPorNadadorUseCase,
    private val obtenerNadadores: ObtenerNadadoresEquipoUseCase,
    private val eliminarMarcaUC: EliminarMarcaUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    /** Mis marcas (para nadador) o todas las del equipo (para entrenador). */
    private val _marcasMias = MutableLiveData<NetworkResult<List<MarcaDeTiempo>>>()
    val marcasMias: LiveData<NetworkResult<List<MarcaDeTiempo>>> = _marcasMias

    /** Marcas que el entrenador ha asignado al nadador (solo nadador con equipo). */
    private val _marcasEntrenador = MutableLiveData<List<MarcaDeTiempo>>()
    val marcasEntrenador: LiveData<List<MarcaDeTiempo>> = _marcasEntrenador

    /** Resultado de la eliminación de una marca, para que la UI pueda reaccionar. */
    private val _marcaEliminada = MutableLiveData<NetworkResult<Boolean>>()
    val marcaEliminada: LiveData<NetworkResult<Boolean>> = _marcaEliminada

    /** Punto de entrada que decide qué cargar según el rol del usuario. */
    fun cargarMarcas() {
        viewModelScope.launch {
            _marcasMias.value = NetworkResult.Loading
            _marcasEntrenador.value = emptyList()

            val esEntrenador = sessionManager.esEntrenador()
            val idEquipo = sessionManager.getEquipoId()
            val idNadador = sessionManager.getIdNadador()
            val idNadadorEquipo = sessionManager.getIdNadadorEquipo().takeIf { it != -1 }

            when {
                esEntrenador && idEquipo != null -> {
                    _marcasMias.value = cargarTodasLasMarcasDelEquipo(idEquipo)
                }
                !esEntrenador && idNadador != -1 && idNadadorEquipo != null -> {
                    cargarMarcasNadadorConEquipo(idNadador, idNadadorEquipo)
                }
                !esEntrenador && idNadador != -1 -> {
                    _marcasMias.value = obtenerMarcasPorNadador(idNadador)
                }
                else ->
                    _marcasMias.value = NetworkResult.Error("Sesión inválida o entrenador sin equipo asignado")
            }
        }
    }

    /**
     * Elimina lógicamente una marca de tiempo a través del caso de uso correspondiente.
     * Tras la operación se recarga la lista independientemente del resultado,
     * para garantizar que la UI refleje el estado real del servidor incluso si
     * hubo un problema parcial (ej. eliminado en servidor pero respuesta inesperada).
     */
    fun eliminarMarca(idMarca: Int) {
        viewModelScope.launch {
            _marcaEliminada.value = NetworkResult.Loading
            val resultado = eliminarMarcaUC(idMarca)
            _marcaEliminada.value = resultado
            // Recargamos siempre para sincronizar con el servidor.
            cargarMarcas()
        }
    }

    private suspend fun cargarMarcasNadadorConEquipo(idNadador: Int, idNadadorEquipo: Int) {
        val mias = obtenerMarcasPorNadador(idNadador)
        val delEquipo = obtenerMarcasPorEquipo(idNadadorEquipo)

        if (mias is NetworkResult.Error && delEquipo is NetworkResult.Error) {
            _marcasMias.value = NetworkResult.Error("No se pudieron cargar las marcas")
            return
        }

        val listaMias = (mias as? NetworkResult.Success)?.data.orEmpty()
        val listaEquipo = (delEquipo as? NetworkResult.Success)?.data.orEmpty()

        val todas = (listaMias + listaEquipo).distinctBy { it.id }

        val miasFinales = todas.filter { it.idNadador == idNadador }
        val delEntrenadorFinales = todas.filter { it.idNadador == null }

        _marcasMias.value = NetworkResult.Success(miasFinales)
        _marcasEntrenador.value = delEntrenadorFinales
    }

    private suspend fun cargarTodasLasMarcasDelEquipo(idEquipo: Int): NetworkResult<List<MarcaDeTiempo>> {
        val nadadoresResult = obtenerNadadores(idEquipo)
        if (nadadoresResult !is NetworkResult.Success) {
            return NetworkResult.Error(
                (nadadoresResult as? NetworkResult.Error)?.message ?: "No se pudieron cargar los nadadores"
            )
        }

        val nadadoresOrdenados = nadadoresResult.data.sortedBy { it.nombre.lowercase() }
        val todasLasMarcas = mutableListOf<MarcaDeTiempo>()

        for (nadador in nadadoresOrdenados) {
            val marcasResult = obtenerMarcasPorEquipo(nadador.idNadadorEquipo)
            if (marcasResult is NetworkResult.Success) {
                val nombreCompleto = "${nadador.nombre} ${nadador.apellidos}".trim()
                marcasResult.data.forEach { marca ->
                    todasLasMarcas.add(
                        marca.copy(descripcion = "$nombreCompleto • ${marca.descripcion}")
                    )
                }
            }
        }
        return NetworkResult.Success(todasLasMarcas)
    }
}