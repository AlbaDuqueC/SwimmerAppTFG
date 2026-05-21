package com.swimming.app.domain.repository

import com.swimming.app.domain.model.*
import com.swimming.app.utils.NetworkResult

/**
 * Contrato del repositorio de Nadador en la capa de dominio.
 * Define las operaciones que la capa de presentación puede realizar
 * sobre los nadadores, sin saber si los datos vienen de la red o de la caché local.
 * Todas las operaciones devuelven NetworkResult para representar éxito o error.
 */
interface NadadorRepository {
    /** Obtiene un nadador por su ID. */
    suspend fun obtenerNadador(id: Int): NetworkResult<Nadador>

    /** Obtiene un nadador por su correo electrónico (usado al iniciar sesión). */
    suspend fun obtenerNadadorPorEmail(email: String): NetworkResult<Nadador>

    /** Crea un nuevo nadador con los datos básicos del registro. */
    suspend fun crearNadador(nombre: String, apellidos: String, email: String, password: String): NetworkResult<Nadador>

    /** Actualiza el nombre y los apellidos de un nadador existente. */
    suspend fun actualizarNadador(id: Int, nombre: String, apellidos: String): NetworkResult<Nadador>

    /** Elimina lógicamente un nadador por su ID. */
    suspend fun eliminarNadador(id: Int): NetworkResult<Boolean>

    /**
     * Vincula la cuenta del nadador con un NadadorEquipo del equipo
     * utilizando el código de 6 dígitos que le proporciona el entrenador.
     */
    suspend fun vincularNadador(idNadador: Int, codigo: Int): NetworkResult<Nadador>
}

/**
 * Contrato del repositorio de Entrenador en la capa de dominio.
 */
interface EntrenadorRepository {
    /** Obtiene un entrenador por su ID. */
    suspend fun obtenerEntrenador(id: Int): NetworkResult<Entrenador>

    /** Obtiene un entrenador por su correo electrónico (usado al iniciar sesión). */
    suspend fun obtenerEntrenadorPorEmail(email: String): NetworkResult<Entrenador>

    /** Crea un nuevo entrenador con los datos básicos del registro. */
    suspend fun crearEntrenador(nombre: String, apellidos: String, email: String, password: String): NetworkResult<Entrenador>

    /** Actualiza el nombre y los apellidos de un entrenador existente. */
    suspend fun actualizarEntrenador(id: Int, nombre: String, apellidos: String): NetworkResult<Entrenador>

    /** Elimina lógicamente un entrenador por su ID. */
    suspend fun eliminarEntrenador(id: Int): NetworkResult<Boolean>
}

/**
 * Contrato del repositorio de Equipo en la capa de dominio.
 */
interface EquipoRepository {
    /** Obtiene un equipo por su ID. */
    suspend fun obtenerEquipo(id: Int): NetworkResult<Equipo>

    /**
     * Crea un nuevo equipo. Si idEntrenador no es nulo,
     * el equipo queda vinculado al entrenador como su equipo gestionado.
     */
    suspend fun crearEquipo(nombre: String, idEntrenador: Int?): NetworkResult<Equipo>

    /** Actualiza el nombre de un equipo existente. */
    suspend fun actualizarEquipo(id: Int, nombre: String): NetworkResult<Equipo>

    /** Elimina lógicamente un equipo por su ID. */
    suspend fun eliminarEquipo(id: Int): NetworkResult<Boolean>
}

/**
 * Contrato del repositorio de NadadorEquipo en la capa de dominio.
 */
interface NadadorEquipoRepository {
    /** Obtiene todos los nadadores (fichas) registrados en un equipo concreto. */
    suspend fun obtenerNadadoresPorEquipo(idEquipo: Int): NetworkResult<List<NadadorEquipo>>

    /** Obtiene un NadadorEquipo a partir de su código de 6 dígitos. */
    suspend fun obtenerPorCodigo(codigo: Int): NetworkResult<NadadorEquipo>

    /** Crea una nueva ficha de nadador dentro de un equipo. */
    suspend fun crearNadadorEquipo(nombre: String, apellidos: String, idEquipo: Int): NetworkResult<NadadorEquipo>

    /** Elimina lógicamente una ficha de NadadorEquipo por su ID. */
    suspend fun eliminarNadadorEquipo(id: Int): NetworkResult<Boolean>

    /** Actualiza el nombre y los apellidos de un NadadorEquipo existente. */
    suspend fun actualizarNadadorEquipo(
        id: Int,
        nombre: String,
        apellidos: String,
        idEquipo: Int
    ): NetworkResult<NadadorEquipo>
}

/**
 * Contrato del repositorio de Rutina en la capa de dominio.
 */
interface RutinaRepository {
    /** Obtiene todas las rutinas asociadas a un usuario concreto. */
    suspend fun obtenerRutinasPorUsuario(idUsuario: Int): NetworkResult<List<Rutina>>

    /** Crea una nueva rutina para un usuario. */
    suspend fun crearRutina(contenido: String, fecha: String, mostrar: Boolean, idUsuario: Int): NetworkResult<Rutina>

    /** Elimina lógicamente una rutina por su ID. */
    suspend fun eliminarRutina(id: Int): NetworkResult<Boolean>
}

/**
 * Contrato del repositorio de MarcaDeTiempo en la capa de dominio.
 */
interface MarcaDeTiempoRepository {
    /** Obtiene todas las marcas de tiempo asociadas a un NadadorEquipo concreto. */
    suspend fun obtenerMarcasPorNadadorEquipo(idNadadorEquipo: Int): NetworkResult<List<MarcaDeTiempo>>

    /** Obtiene todas las marcas de tiempo registradas por un nadador concreto. */
    suspend fun obtenerMarcasPorNadador(idNadador: Int): NetworkResult<List<MarcaDeTiempo>>

    /**
     * Crea una nueva marca de tiempo.
     * Si idNadador es nulo, significa que la registra el entrenador.
     */
    suspend fun crearMarca(tiempo: String, descripcion: String, idNadadorEquipo: Int?, idNadador: Int?): NetworkResult<MarcaDeTiempo>

    /** Elimina lógicamente una marca de tiempo por su ID. */
    suspend fun eliminarMarca(id: Int): NetworkResult<Boolean>
}