package com.swimming.app.domain.repository

import com.swimming.app.domain.model.*
import com.swimming.app.utils.NetworkResult

interface NadadorRepository {
    suspend fun obtenerNadador(id: Int): NetworkResult<Nadador>
    suspend fun obtenerNadadorPorEmail(email: String): NetworkResult<Nadador>  // ✨ NUEVO
    suspend fun crearNadador(nombre: String, apellidos: String, email: String, password: String): NetworkResult<Nadador>
    suspend fun actualizarNadador(id: Int, nombre: String, apellidos: String): NetworkResult<Nadador>
    suspend fun eliminarNadador(id: Int): NetworkResult<Boolean>
    suspend fun vincularNadador(idNadador: Int, codigo: Int): NetworkResult<Nadador>
}

interface EntrenadorRepository {
    suspend fun obtenerEntrenador(id: Int): NetworkResult<Entrenador>
    suspend fun obtenerEntrenadorPorEmail(email: String): NetworkResult<Entrenador>  // ✨ NUEVO
    suspend fun crearEntrenador(nombre: String, apellidos: String, email: String, password: String): NetworkResult<Entrenador>
    suspend fun actualizarEntrenador(id: Int, nombre: String, apellidos: String): NetworkResult<Entrenador>
    suspend fun eliminarEntrenador(id: Int): NetworkResult<Boolean>
}

interface EquipoRepository {
    suspend fun obtenerEquipo(id: Int): NetworkResult<Equipo>
    suspend fun crearEquipo(nombre: String, idEntrenador: Int?): NetworkResult<Equipo>  // ✨ NUEVO param
    suspend fun eliminarEquipo(id: Int): NetworkResult<Boolean>
}

interface NadadorEquipoRepository {
    suspend fun obtenerNadadoresPorEquipo(idEquipo: Int): NetworkResult<List<NadadorEquipo>>
    suspend fun obtenerPorCodigo(codigo: Int): NetworkResult<NadadorEquipo>
    suspend fun crearNadadorEquipo(nombre: String, apellidos: String, idEquipo: Int): NetworkResult<NadadorEquipo>
    suspend fun eliminarNadadorEquipo(id: Int): NetworkResult<Boolean>
}

interface RutinaRepository {
    suspend fun obtenerRutinasPorUsuario(idUsuario: Int): NetworkResult<List<Rutina>>
    suspend fun crearRutina(contenido: String, fecha: String, mostrar: Boolean, idUsuario: Int): NetworkResult<Rutina>
    suspend fun eliminarRutina(id: Int): NetworkResult<Boolean>
}

interface MarcaDeTiempoRepository {
    suspend fun obtenerMarcasPorNadadorEquipo(idNadadorEquipo: Int): NetworkResult<List<MarcaDeTiempo>>
    suspend fun obtenerMarcasPorNadador(idNadador: Int): NetworkResult<List<MarcaDeTiempo>>  // ✨ NUEVO
    suspend fun crearMarca(tiempo: String, descripcion: String, idNadadorEquipo: Int?, idNadador: Int?): NetworkResult<MarcaDeTiempo>
    suspend fun eliminarMarca(id: Int): NetworkResult<Boolean>
}


