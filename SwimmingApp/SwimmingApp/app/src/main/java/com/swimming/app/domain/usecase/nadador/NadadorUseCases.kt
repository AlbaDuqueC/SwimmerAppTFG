package com.swimming.app.domain.usecase.nadador

import com.swimming.app.domain.model.Nadador
import com.swimming.app.domain.repository.NadadorRepository
import com.swimming.app.utils.NetworkResult
import javax.inject.Inject

class ObtenerNadadorUseCase @Inject constructor(private val repo: NadadorRepository) {
    suspend operator fun invoke(id: Int): NetworkResult<Nadador> {
        val resultado = repo.obtenerNadador(id)
        return resultado
    }
}

class ActualizarNadadorUseCase @Inject constructor(private val repo: NadadorRepository) {
    suspend operator fun invoke(id: Int, nombre: String, apellidos: String): NetworkResult<Nadador> {
        val resultado = repo.actualizarNadador(id, nombre, apellidos)
        return resultado
    }
}

class EliminarNadadorUseCase @Inject constructor(private val repo: NadadorRepository) {
    suspend operator fun invoke(id: Int): NetworkResult<Boolean> {
        val resultado = repo.eliminarNadador(id)
        return resultado
    }
}

class ObtenerNadadorPorEmailUseCase @Inject constructor(private val repo: NadadorRepository) {
    suspend operator fun invoke(email: String): NetworkResult<Nadador> {
        val resultado = repo.obtenerNadadorPorEmail(email)
        return resultado
    }
}

class VincularNadadorUseCase @Inject constructor(private val repo: NadadorRepository) {
    suspend operator fun invoke(idNadador: Int, codigo: Int): NetworkResult<Nadador> {
        return repo.vincularNadador(idNadador, codigo)
    }
}