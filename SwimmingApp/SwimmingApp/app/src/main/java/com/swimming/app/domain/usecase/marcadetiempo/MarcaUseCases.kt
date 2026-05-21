package com.swimming.app.domain.usecase.marcadetiempo

import com.swimming.app.domain.model.MarcaDeTiempo
import com.swimming.app.domain.repository.MarcaDeTiempoRepository
import com.swimming.app.utils.NetworkResult
import javax.inject.Inject

/**
 * Caso de uso para obtener todas las marcas de tiempo asociadas a un NadadorEquipo.
 * Se utiliza cuando el entrenador consulta el histórico de un nadador de su equipo.
 */
class ObtenerMarcasUseCase @Inject constructor(private val repo: MarcaDeTiempoRepository) {
    suspend operator fun invoke(idNadadorEquipo: Int): NetworkResult<List<MarcaDeTiempo>> {
        val resultado = repo.obtenerMarcasPorNadadorEquipo(idNadadorEquipo)
        return resultado
    }
}

/**
 * Caso de uso para obtener las marcas registradas directamente por un nadador.
 * Se utiliza cuando el propio nadador consulta sus marcas personales en la app.
 */
class ObtenerMarcasPorNadadorUseCase @Inject constructor(private val repo: MarcaDeTiempoRepository) {
    suspend operator fun invoke(idNadador: Int): NetworkResult<List<MarcaDeTiempo>> {
        val resultado = repo.obtenerMarcasPorNadador(idNadador)
        return resultado
    }
}

/**
 * Caso de uso para registrar una nueva marca de tiempo.
 * Si idNadador es nulo, significa que la marca la asigna el entrenador.
 * Si tiene valor, la marca la registra el propio nadador.
 */
class CrearMarcaUseCase @Inject constructor(private val repo: MarcaDeTiempoRepository) {
    suspend operator fun invoke(tiempo: String, descripcion: String, idNadadorEquipo: Int?, idNadador: Int?): NetworkResult<MarcaDeTiempo> {
        val resultado = repo.crearMarca(tiempo, descripcion, idNadadorEquipo, idNadador)
        return resultado
    }
}

/**
 * Caso de uso para eliminar lógicamente una marca de tiempo por su ID.
 * El registro permanece en la base de datos pero se marca como inactivo,
 * por lo que ya no aparece en las consultas.
 */
class EliminarMarcaUseCase @Inject constructor(private val repo: MarcaDeTiempoRepository) {
    suspend operator fun invoke(id: Int): NetworkResult<Boolean> {
        val resultado = repo.eliminarMarca(id)
        return resultado
    }
}