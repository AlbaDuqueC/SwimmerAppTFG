package com.swimming.app.domain.usecase.rutina

import com.swimming.app.domain.model.Rutina
import com.swimming.app.domain.repository.RutinaRepository
import com.swimming.app.utils.NetworkResult
import javax.inject.Inject

/**
 * Caso de uso para obtener todas las rutinas asociadas a un usuario concreto.
 * Se utiliza en la pantalla de inicio para mostrar las rutinas personales.
 */
class ObtenerRutinasUseCase @Inject constructor(private val repo: RutinaRepository) {
    suspend operator fun invoke(idUsuario: Int): NetworkResult<List<Rutina>> {
        val resultado = repo.obtenerRutinasPorUsuario(idUsuario)
        return resultado
    }
}

/**
 * Caso de uso para crear una nueva rutina.
 * Si el usuario es un entrenador con equipo, la API replica automáticamente
 * la rutina para todos los nadadores del equipo.
 */
class CrearRutinaUseCase @Inject constructor(private val repo: RutinaRepository) {
    suspend operator fun invoke(contenido: String, fecha: String, mostrar: Boolean, idUsuario: Int): NetworkResult<Rutina> {
        val resultado = repo.crearRutina(contenido, fecha, mostrar, idUsuario)
        return resultado
    }
}