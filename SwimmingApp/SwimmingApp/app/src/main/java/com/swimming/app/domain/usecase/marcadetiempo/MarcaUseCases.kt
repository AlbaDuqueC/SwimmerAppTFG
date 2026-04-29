package com.swimming.app.domain.usecase.marcadetiempo

import com.swimming.app.domain.model.MarcaDeTiempo
import com.swimming.app.domain.repository.MarcaDeTiempoRepository
import com.swimming.app.utils.NetworkResult
import javax.inject.Inject

class ObtenerMarcasUseCase @Inject constructor(private val repo: MarcaDeTiempoRepository) {
    suspend operator fun invoke(idNadadorEquipo: Int): NetworkResult<List<MarcaDeTiempo>> {
        val resultado = repo.obtenerMarcasPorNadadorEquipo(idNadadorEquipo)
        return resultado
    }
}

class ObtenerMarcasPorNadadorUseCase @Inject constructor(private val repo: MarcaDeTiempoRepository) {
    suspend operator fun invoke(idNadador: Int): NetworkResult<List<MarcaDeTiempo>> {
        val resultado = repo.obtenerMarcasPorNadador(idNadador)
        return resultado
    }
}

class CrearMarcaUseCase @Inject constructor(private val repo: MarcaDeTiempoRepository) {
    suspend operator fun invoke(tiempo: String, descripcion: String, idNadadorEquipo: Int?, idNadador: Int?): NetworkResult<MarcaDeTiempo> {
        val resultado = repo.crearMarca(tiempo, descripcion, idNadadorEquipo, idNadador)
        return resultado
    }
}