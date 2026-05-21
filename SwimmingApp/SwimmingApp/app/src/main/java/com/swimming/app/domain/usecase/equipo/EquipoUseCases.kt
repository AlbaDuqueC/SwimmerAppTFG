package com.swimming.app.domain.usecase.equipo

import com.swimming.app.domain.model.Equipo
import com.swimming.app.domain.repository.EquipoRepository
import com.swimming.app.utils.NetworkResult
import javax.inject.Inject

/**
 * Caso de uso para obtener los datos de un equipo a partir de su ID.
 */
class ObtenerEquipoUseCase @Inject constructor(private val repo: EquipoRepository) {
    suspend operator fun invoke(id: Int): NetworkResult<Equipo> {
        val resultado = repo.obtenerEquipo(id)
        return resultado
    }
}

/**
 * Caso de uso para crear un nuevo equipo.
 * Si se pasa idEntrenador, el equipo queda vinculado al entrenador como su equipo gestionado.
 */
class CrearEquipoUseCase @Inject constructor(private val repo: EquipoRepository) {
    suspend operator fun invoke(nombre: String, idEntrenador: Int?): NetworkResult<Equipo> {
        val resultado = repo.crearEquipo(nombre, idEntrenador)
        return resultado
    }
}

/**
 * Caso de uso para actualizar el nombre de un equipo existente.
 */
class ActualizarEquipoUseCase @Inject constructor(private val repo: EquipoRepository) {
    suspend operator fun invoke(id: Int, nombre: String): NetworkResult<Equipo> {
        val resultado = repo.actualizarEquipo(id, nombre)
        return resultado
    }
}

/**
 * Caso de uso para eliminar lógicamente un equipo.
 */
class EliminarEquipoUseCase @Inject constructor(private val repo: EquipoRepository) {
    suspend operator fun invoke(id: Int): NetworkResult<Boolean> {
        val resultado = repo.eliminarEquipo(id)
        return resultado
    }
}