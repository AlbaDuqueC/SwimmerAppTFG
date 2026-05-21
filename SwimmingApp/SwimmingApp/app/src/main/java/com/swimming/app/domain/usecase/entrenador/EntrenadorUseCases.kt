package com.swimming.app.domain.usecase.entrenador

import com.swimming.app.domain.model.Entrenador
import com.swimming.app.domain.repository.EntrenadorRepository
import com.swimming.app.utils.NetworkResult
import javax.inject.Inject

/**
 * Caso de uso para obtener los datos de un entrenador a partir de su ID.
 * Se utiliza al cargar el perfil del usuario en la pantalla correspondiente.
 */
class ObtenerEntrenadorUseCase @Inject constructor(private val repo: EntrenadorRepository) {
    suspend operator fun invoke(id: Int): NetworkResult<Entrenador> {
        val resultado = repo.obtenerEntrenador(id)
        return resultado
    }
}

/**
 * Caso de uso para actualizar el nombre y los apellidos de un entrenador.
 * Se utiliza al editar el perfil.
 */
class ActualizarEntrenadorUseCase @Inject constructor(private val repo: EntrenadorRepository) {
    suspend operator fun invoke(id: Int, nombre: String, apellidos: String): NetworkResult<Entrenador> {
        val resultado = repo.actualizarEntrenador(id, nombre, apellidos)
        return resultado
    }
}

/**
 * Caso de uso para eliminar lógicamente la cuenta de un entrenador.
 */
class EliminarEntrenadorUseCase @Inject constructor(private val repo: EntrenadorRepository) {
    suspend operator fun invoke(id: Int): NetworkResult<Boolean> {
        val resultado = repo.eliminarEntrenador(id)
        return resultado
    }
}

/**
 * Caso de uso para obtener un entrenador por su correo electrónico.
 * Se utiliza al iniciar sesión, ya que Firebase identifica al usuario por email.
 */
class ObtenerEntrenadorPorEmailUseCase @Inject constructor(private val repo: EntrenadorRepository) {
    suspend operator fun invoke(email: String): NetworkResult<Entrenador> {
        val resultado = repo.obtenerEntrenadorPorEmail(email)
        return resultado
    }
}