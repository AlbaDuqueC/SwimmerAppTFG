package com.swimming.app.domain.usecase.nadadorequipo

import com.swimming.app.domain.model.NadadorEquipo
import com.swimming.app.domain.repository.NadadorEquipoRepository
import com.swimming.app.utils.NetworkResult
import javax.inject.Inject

class ObtenerNadadoresEquipoUseCase @Inject constructor(private val repo: NadadorEquipoRepository) {
    suspend operator fun invoke(idEquipo: Int): NetworkResult<List<NadadorEquipo>> {
        val resultado = repo.obtenerNadadoresPorEquipo(idEquipo)
        return resultado
    }
}

class CrearNadadorEquipoUseCase @Inject constructor(private val repo: NadadorEquipoRepository) {
    suspend operator fun invoke(nombre: String, apellidos: String, idEquipo: Int): NetworkResult<NadadorEquipo> {
        val resultado = repo.crearNadadorEquipo(nombre, apellidos, idEquipo)
        return resultado
    }
}


