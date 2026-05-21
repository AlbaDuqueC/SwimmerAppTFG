package com.swimming.app.domain.usecase.auth

import com.swimming.app.domain.model.*
import com.swimming.app.domain.repository.*
import com.swimming.app.utils.NetworkResult
import javax.inject.Inject

/**
 * Caso de uso para registrar un nuevo nadador en el sistema.
 * Encapsula la lógica de creación de cuenta para que el ViewModel
 * no dependa directamente del repositorio.
 *
 * Al ser un `operator fun invoke`, el ViewModel puede llamarlo como
 * si fuera una función: `registerNadador(nombre, apellidos, email, password)`.
 */
class RegisterNadadorUseCase @Inject constructor(private val repo: NadadorRepository) {
    suspend operator fun invoke(nombre: String, apellidos: String, email: String, password: String): NetworkResult<Nadador> {
        val resultado = repo.crearNadador(nombre, apellidos, email, password)
        return resultado
    }
}

/**
 * Caso de uso para registrar un nuevo entrenador en el sistema.
 * Funciona igual que RegisterNadadorUseCase pero para el rol de entrenador.
 */
class RegisterEntrenadorUseCase @Inject constructor(private val repo: EntrenadorRepository) {
    suspend operator fun invoke(nombre: String, apellidos: String, email: String, password: String): NetworkResult<Entrenador> {
        val resultado = repo.crearEntrenador(nombre, apellidos, email, password)
        return resultado
    }
}