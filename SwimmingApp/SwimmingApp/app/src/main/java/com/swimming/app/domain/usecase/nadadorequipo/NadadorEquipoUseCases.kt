package com.swimming.app.domain.usecase.nadadorequipo

import com.swimming.app.domain.model.NadadorEquipo
import com.swimming.app.domain.repository.NadadorEquipoRepository
import com.swimming.app.utils.NetworkResult
import javax.inject.Inject

/**
 * Caso de uso para obtener todos los nadadores (fichas) registrados en un equipo.
 * Es la consulta principal de la pantalla de equipo del entrenador.
 */
class ObtenerNadadoresEquipoUseCase @Inject constructor(private val repo: NadadorEquipoRepository) {
    suspend operator fun invoke(idEquipo: Int): NetworkResult<List<NadadorEquipo>> {
        val resultado = repo.obtenerNadadoresPorEquipo(idEquipo)
        return resultado
    }
}

/**
 * Caso de uso para crear una nueva ficha de nadador dentro de un equipo.
 * Solo lo puede hacer el entrenador. El sistema genera automáticamente
 * un código único de 6 dígitos para esa ficha.
 */
class CrearNadadorEquipoUseCase @Inject constructor(private val repo: NadadorEquipoRepository) {
    suspend operator fun invoke(nombre: String, apellidos: String, idEquipo: Int): NetworkResult<NadadorEquipo> {
        val resultado = repo.crearNadadorEquipo(nombre, apellidos, idEquipo)
        return resultado
    }
}

/**
 * Caso de uso para eliminar lógicamente una ficha de NadadorEquipo del equipo.
 * Si había un usuario vinculado a esa ficha, queda desvinculado automáticamente.
 */
class EliminarNadadorEquipoUseCase @Inject constructor(private val repo: NadadorEquipoRepository) {
    suspend operator fun invoke(id: Int): NetworkResult<Boolean> {
        val resultado = repo.eliminarNadadorEquipo(id)
        return resultado
    }
}

/**
 * Caso de uso para actualizar el nombre y los apellidos de un NadadorEquipo.
 * Lo utiliza el entrenador desde la pantalla de equipo al editar una ficha.
 */
class ActualizarNadadorEquipoUseCase @Inject constructor(
    private val repo: NadadorEquipoRepository
) {
    suspend operator fun invoke(
        id: Int,
        nombre: String,
        apellidos: String,
        idEquipo: Int
    ): NetworkResult<NadadorEquipo> {
        val resultado = repo.actualizarNadadorEquipo(id, nombre, apellidos, idEquipo)
        return resultado
    }
}