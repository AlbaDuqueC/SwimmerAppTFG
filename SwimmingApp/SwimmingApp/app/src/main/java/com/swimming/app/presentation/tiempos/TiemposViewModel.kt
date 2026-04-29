package com.swimming.app.presentation.tiempos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swimming.app.domain.model.MarcaDeTiempo
import com.swimming.app.domain.usecase.marcadetiempo.ObtenerMarcasPorNadadorUseCase
import com.swimming.app.domain.usecase.marcadetiempo.ObtenerMarcasUseCase
import com.swimming.app.domain.usecase.nadadorequipo.ObtenerNadadoresEquipoUseCase
import com.swimming.app.utils.NetworkResult
import com.swimming.app.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TiemposViewModel @Inject constructor(
    private val obtenerMarcasPorEquipo: ObtenerMarcasUseCase,
    private val obtenerMarcasPorNadador: ObtenerMarcasPorNadadorUseCase,
    private val obtenerNadadores: ObtenerNadadoresEquipoUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    /** Mis marcas (las que yo registro), o todas las del equipo si soy entrenador. */
    private val _marcasMias = MutableLiveData<NetworkResult<List<MarcaDeTiempo>>>()
    val marcasMias: LiveData<NetworkResult<List<MarcaDeTiempo>>> = _marcasMias

    /** Marcas que el entrenador me ha asignado (solo nadador con equipo). */
    private val _marcasEntrenador = MutableLiveData<List<MarcaDeTiempo>>()
    val marcasEntrenador: LiveData<List<MarcaDeTiempo>> = _marcasEntrenador

    fun cargarMarcas() {
        viewModelScope.launch {
            _marcasMias.value = NetworkResult.Loading
            _marcasEntrenador.value = emptyList()

            val esEntrenador = sessionManager.esEntrenador()
            val idEquipo = sessionManager.getEquipoId()
            val idNadador = sessionManager.getIdNadador()
            val idNadadorEquipo = sessionManager.getIdNadadorEquipo().takeIf { it != -1 }

            when {
                // ENTRENADOR con equipo: una sola lista con todas las marcas del equipo
                esEntrenador && idEquipo != null -> {
                    _marcasMias.value = cargarTodasLasMarcasDelEquipo(idEquipo)
                }

                // NADADOR vinculado a un equipo: dos listas (mías y del entrenador)
                !esEntrenador && idNadador != -1 && idNadadorEquipo != null -> {
                    cargarMarcasNadadorConEquipo(idNadador, idNadadorEquipo)
                }

                // NADADOR sin equipo: solo las suyas
                !esEntrenador && idNadador != -1 -> {
                    _marcasMias.value = obtenerMarcasPorNadador(idNadador)
                }

                else ->
                    _marcasMias.value = NetworkResult.Error("Sesión inválida o entrenador sin equipo asignado")
            }
        }
    }

    /**
     * Para un nadador con equipo: pedimos sus marcas por nadador (las que él creó, con o sin equipo)
     * y las del NadadorEquipo (incluyen las del entrenador). Combinamos sin duplicar y separamos.
     */
    private suspend fun cargarMarcasNadadorConEquipo(idNadador: Int, idNadadorEquipo: Int) {
        val mias = obtenerMarcasPorNadador(idNadador)
        val delEquipo = obtenerMarcasPorEquipo(idNadadorEquipo)

        if (mias is NetworkResult.Error && delEquipo is NetworkResult.Error) {
            _marcasMias.value = NetworkResult.Error("No se pudieron cargar las marcas")
            return
        }

        val listaMias = (mias as? NetworkResult.Success)?.data.orEmpty()
        val listaEquipo = (delEquipo as? NetworkResult.Success)?.data.orEmpty()

        // Deduplicar por id (una marca puede aparecer en ambas listas)
        val todas = (listaMias + listaEquipo).distinctBy { it.id }

        // Las "mías" tienen mi idNadador. Las del entrenador llegan con idNadador == null.
        val miasFinales = todas.filter { it.idNadador == idNadador }
        val delEntrenadorFinales = todas.filter { it.idNadador == null }

        _marcasMias.value = NetworkResult.Success(miasFinales)
        _marcasEntrenador.value = delEntrenadorFinales
    }

    /** Para el entrenador: lista plana con todas las marcas del equipo, prefijadas con el nombre. */
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