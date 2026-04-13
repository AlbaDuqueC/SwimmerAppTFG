package com.swimming.app.domain.usecase.equipo

import com.swimming.app.domain.model.Equipo
import com.swimming.app.domain.repository.EquipoRepository
import com.swimming.app.utils.NetworkResult
import javax.inject.Inject

class ObtenerEquipoUseCase @Inject constructor(private val repo: EquipoRepository) {
    suspend operator fun invoke(id: Int): NetworkResult<Equipo> {
        val resultado = repo.obtenerEquipo(id)
        return resultado
    }
}

class CrearEquipoUseCase @Inject constructor(private val repo: EquipoRepository) {
    suspend operator fun invoke(nombre: String): NetworkResult<Equipo> {
        val resultado = repo.crearEquipo(nombre)
        return resultado
    }
}
