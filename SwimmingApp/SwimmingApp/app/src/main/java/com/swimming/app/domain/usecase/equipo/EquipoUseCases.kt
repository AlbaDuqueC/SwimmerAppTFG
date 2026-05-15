package com.swimming.app.domain.usecase.equipo

import com.swimming.app.domain.model.Equipo
import com.swimming.app.domain.repository.EquipoRepository
import com.swimming.app.utils.NetworkResult
import javax.inject.Inject

class ObtenerEquipoUseCase @Inject constructor(private val repo: EquipoRepository) {
    suspend operator fun invoke(id: Int): NetworkResult<Equipo> {
        val resultado = repo.obtenerEquipo(id)
        return resultado
    }
}

class CrearEquipoUseCase @Inject constructor(private val repo: EquipoRepository) {
    suspend operator fun invoke(nombre: String, idEntrenador: Int?): NetworkResult<Equipo> {
        val resultado = repo.crearEquipo(nombre, idEntrenador)
        return resultado
    }
}

class ActualizarEquipoUseCase @Inject constructor(private val repo: EquipoRepository) {
    suspend operator fun invoke(id: Int, nombre: String): NetworkResult<Equipo> {
        val resultado = repo.actualizarEquipo(id, nombre)
        return resultado
    }
}

class EliminarEquipoUseCase @Inject constructor(private val repo: EquipoRepository) {
    suspend operator fun invoke(id: Int): NetworkResult<Boolean> {
        val resultado = repo.eliminarEquipo(id)
        return resultado
    }
}