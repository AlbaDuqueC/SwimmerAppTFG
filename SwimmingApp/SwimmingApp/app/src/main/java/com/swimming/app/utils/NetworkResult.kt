package com.swimming.app.utils

/**
 * Envoltorio para representar el estado de una operación asíncrona.
 * Permite que los ViewModels comuniquen a las vistas si una operación
 * está en curso, ha tenido éxito o ha fallado, sin lanzar excepciones.
 *
 * Al ser una sealed class, el compilador obliga a manejar todos los casos
 * cuando se usa en un `when`, lo que evita olvidos.
 */
sealed class NetworkResult<out T> {
    /** Estado de éxito que contiene los datos devueltos. */
    data class Success<T>(val data: T) : NetworkResult<T>()

    /** Estado de error con el mensaje correspondiente. */
    data class Error(val message: String) : NetworkResult<Nothing>()

    /** Estado intermedio mientras la operación está en curso. */
    object Loading : NetworkResult<Nothing>()
}