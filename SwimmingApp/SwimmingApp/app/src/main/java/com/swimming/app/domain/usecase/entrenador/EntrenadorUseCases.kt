package com.swimming.app.domain.usecase.entrenador

import com.swimming.app.domain.model.Entrenador
import com.swimming.app.domain.repository.EntrenadorRepository
import com.swimming.app.utils.NetworkResult
import javax.inject.Inject

class ObtenerEntrenadorUseCase @Inject constructor(private val repo: EntrenadorRepository) {
    suspend operator fun invoke(id: Int): NetworkResult<Entrenador> {
        val resultado = repo.obtenerEntrenador(id)
        return resultado
    }
}

class ActualizarEntrenadorUseCase @Inject constructor(private val repo: EntrenadorRepository) {
    suspend operator fun invoke(id: Int, nombre: String, apellidos: String): NetworkResult<Entrenador> {
        val resultado = repo.actualizarEntrenador(id, nombre, apellidos)
        return resultado
    }
}

class EliminarEntrenadorUseCase @Inject constructor(private val repo: EntrenadorRepository) {
    suspend operator fun invoke(id: Int): NetworkResult<Boolean> {
        val resultado = repo.eliminarEntrenador(id)
        return resultado
    }
}

class ObtenerEntrenadorPorEmailUseCase @Inject constructor(private val repo: EntrenadorRepository) {
    suspend operator fun invoke(email: String): NetworkResult<Entrenador> {
        val resultado = repo.obtenerEntrenadorPorEmail(email)
        return resultado
    }
}
