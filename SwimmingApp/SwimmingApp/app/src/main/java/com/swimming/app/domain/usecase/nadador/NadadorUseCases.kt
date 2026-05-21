package com.swimming.app.domain.usecase.nadador

import com.swimming.app.domain.model.Nadador
import com.swimming.app.domain.repository.NadadorRepository
import com.swimming.app.utils.NetworkResult
import javax.inject.Inject

/**
 * Caso de uso para obtener los datos de un nadador a partir de su ID.
 * Se utiliza al cargar el perfil del usuario en la pantalla correspondiente.
 */
class ObtenerNadadorUseCase @Inject constructor(private val repo: NadadorRepository) {
    suspend operator fun invoke(id: Int): NetworkResult<Nadador> {
        val resultado = repo.obtenerNadador(id)
        return resultado
    }
}

/**
 * Caso de uso para actualizar el nombre y los apellidos de un nadador.
 * Se utiliza al editar el perfil.
 */
class ActualizarNadadorUseCase @Inject constructor(private val repo: NadadorRepository) {
    suspend operator fun invoke(id: Int, nombre: String, apellidos: String): NetworkResult<Nadador> {
        val resultado = repo.actualizarNadador(id, nombre, apellidos)
        return resultado
    }
}

/**
 * Caso de uso para eliminar lógicamente la cuenta de un nadador.
 */
class EliminarNadadorUseCase @Inject constructor(private val repo: NadadorRepository) {
    suspend operator fun invoke(id: Int): NetworkResult<Boolean> {
        val resultado = repo.eliminarNadador(id)
        return resultado
    }
}

/**
 * Caso de uso para obtener un nadador por su correo electrónico.
 * Se utiliza al iniciar sesión, ya que Firebase identifica al usuario por email.
 */
class ObtenerNadadorPorEmailUseCase @Inject constructor(private val repo: NadadorRepository) {
    suspend operator fun invoke(email: String): NetworkResult<Nadador> {
        val resultado = repo.obtenerNadadorPorEmail(email)
        return resultado
    }
}

/**
 * Caso de uso para vincular la cuenta de un nadador a un equipo
 * utilizando el código de 6 dígitos que le ha proporcionado el entrenador.
 */
class VincularNadadorUseCase @Inject constructor(private val repo: NadadorRepository) {
    suspend operator fun invoke(idNadador: Int, codigo: Int): NetworkResult<Nadador> {
        return repo.vincularNadador(idNadador, codigo)
    }
}