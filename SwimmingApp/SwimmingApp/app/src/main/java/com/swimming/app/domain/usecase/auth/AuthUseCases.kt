package com.swimming.app.domain.usecase.auth

import com.swimming.app.domain.model.*
import com.swimming.app.domain.repository.*
import com.swimming.app.utils.NetworkResult
import javax.inject.Inject

class RegisterNadadorUseCase @Inject constructor(private val repo: NadadorRepository) {
    suspend operator fun invoke(nombre: String, apellidos: String, email: String, password: String): NetworkResult<Nadador> {
        val resultado = repo.crearNadador(nombre, apellidos, email, password)
        return resultado
    }
}

class RegisterEntrenadorUseCase @Inject constructor(private val repo: EntrenadorRepository) {
    suspend operator fun invoke(nombre: String, apellidos: String, email: String, password: String): NetworkResult<Entrenador> {
        val resultado = repo.crearEntrenador(nombre, apellidos, email, password)
        return resultado
    }
}
