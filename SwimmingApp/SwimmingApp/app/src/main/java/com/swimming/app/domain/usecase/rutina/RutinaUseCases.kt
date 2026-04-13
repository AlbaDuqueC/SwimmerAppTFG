package com.swimming.app.domain.usecase.rutina

import com.swimming.app.domain.model.Rutina
import com.swimming.app.domain.repository.RutinaRepository
import com.swimming.app.utils.NetworkResult
import javax.inject.Inject

class ObtenerRutinasUseCase @Inject constructor(private val repo: RutinaRepository) {
    suspend operator fun invoke(idUsuario: Int): NetworkResult<List<Rutina>> {
        val resultado = repo.obtenerRutinasPorUsuario(idUsuario)
        return resultado
    }
}

class CrearRutinaUseCase @Inject constructor(private val repo: RutinaRepository) {
    suspend operator fun invoke(contenido: String, fecha: String, mostrar: Boolean, idUsuario: Int): NetworkResult<Rutina> {
        val resultado = repo.crearRutina(contenido, fecha, mostrar, idUsuario)
        return resultado
    }
}
