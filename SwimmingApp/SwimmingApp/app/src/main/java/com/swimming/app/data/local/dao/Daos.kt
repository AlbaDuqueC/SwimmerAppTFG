package com.swimming.app.data.local.dao

import androidx.room.*
import com.swimming.app.data.local.entity.*

/**
 * DAO de acceso a la tabla "nadadores" en la base de datos local.
 * Permite consultar, insertar y eliminar nadadores desde Room.
 */
@Dao
interface NadadorDao {
    /** Obtiene un nadador por su ID. Devuelve null si no existe. */
    @Query("SELECT * FROM nadadores WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): NadadorEntity?

    /** Inserta o reemplaza un nadador en la base de datos local. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(nadador: NadadorEntity)

    /** Elimina un nadador por su ID. */
    @Query("DELETE FROM nadadores WHERE id = :id")
    suspend fun eliminar(id: Int)
}

/**
 * DAO de acceso a la tabla "entrenadores" en la base de datos local.
 */
@Dao
interface EntrenadorDao {
    /** Obtiene un entrenador por su ID. Devuelve null si no existe. */
    @Query("SELECT * FROM entrenadores WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): EntrenadorEntity?

    /** Inserta o reemplaza un entrenador en la base de datos local. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(entrenador: EntrenadorEntity)

    /** Elimina un entrenador por su ID. */
    @Query("DELETE FROM entrenadores WHERE id = :id")
    suspend fun eliminar(id: Int)
}

/**
 * DAO de acceso a la tabla "equipos" en la base de datos local.
 */
@Dao
interface EquipoDao {
    /** Obtiene un equipo por su ID. Devuelve null si no existe. */
    @Query("SELECT * FROM equipos WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): EquipoEntity?

    /** Inserta o reemplaza un equipo en la base de datos local. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(equipo: EquipoEntity)
}

/**
 * DAO de acceso a la tabla "nadadores_equipo" en la base de datos local.
 * Gestiona las fichas de nadadores asociadas a cada equipo.
 */
@Dao
interface NadadorEquipoDao {
    /** Obtiene todas las fichas de nadadores de un equipo concreto. */
    @Query("SELECT * FROM nadadores_equipo WHERE idEquipo = :idEquipo")
    suspend fun obtenerPorEquipo(idEquipo: Int): List<NadadorEquipoEntity>

    /** Inserta o reemplaza una lista completa de fichas en la base de datos local. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(lista: List<NadadorEquipoEntity>)

    /** Elimina todas las fichas asociadas a un equipo concreto. */
    @Query("DELETE FROM nadadores_equipo WHERE idEquipo = :idEquipo")
    suspend fun eliminarPorEquipo(idEquipo: Int)
}

/**
 * DAO de acceso a la tabla "rutinas" en la base de datos local.
 */
@Dao
interface RutinaDao {
    /** Obtiene todas las rutinas asociadas a un usuario concreto. */
    @Query("SELECT * FROM rutinas WHERE idUsuario = :idUsuario")
    suspend fun obtenerPorUsuario(idUsuario: Int): List<RutinaEntity>

    /** Inserta o reemplaza una lista completa de rutinas en la base de datos local. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(lista: List<RutinaEntity>)

    /** Elimina todas las rutinas asociadas a un usuario concreto. */
    @Query("DELETE FROM rutinas WHERE idUsuario = :idUsuario")
    suspend fun eliminarPorUsuario(idUsuario: Int)
}

/**
 * DAO de acceso a la tabla "marcas_tiempo" en la base de datos local.
 */
@Dao
interface MarcaDeTiempoDao {
    /** Obtiene todas las marcas de tiempo asociadas a un NadadorEquipo concreto. */
    @Query("SELECT * FROM marcas_tiempo WHERE idNadadorEquipo = :idNadadorEquipo")
    suspend fun obtenerPorNadadorEquipo(idNadadorEquipo: Int): List<MarcaDeTiempoEntity>

    /** Inserta o reemplaza una lista completa de marcas en la base de datos local. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(lista: List<MarcaDeTiempoEntity>)

    /** Elimina todas las marcas asociadas a un NadadorEquipo concreto. */
    @Query("DELETE FROM marcas_tiempo WHERE idNadadorEquipo = :idNadadorEquipo")
    suspend fun eliminarPorNadadorEquipo(idNadadorEquipo: Int)
}