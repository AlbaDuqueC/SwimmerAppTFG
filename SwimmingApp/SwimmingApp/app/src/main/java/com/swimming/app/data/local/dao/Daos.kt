package com.swimming.app.data.local.dao

import androidx.room.*
import com.swimming.app.data.local.entity.*

@Dao
interface NadadorDao {
    @Query("SELECT * FROM nadadores WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): NadadorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(nadador: NadadorEntity)

    @Query("DELETE FROM nadadores WHERE id = :id")
    suspend fun eliminar(id: Int)
}

@Dao
interface EntrenadorDao {
    @Query("SELECT * FROM entrenadores WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): EntrenadorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(entrenador: EntrenadorEntity)

    @Query("DELETE FROM entrenadores WHERE id = :id")
    suspend fun eliminar(id: Int)
}

@Dao
interface EquipoDao {
    @Query("SELECT * FROM equipos WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): EquipoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(equipo: EquipoEntity)
}

@Dao
interface NadadorEquipoDao {
    @Query("SELECT * FROM nadadores_equipo WHERE idEquipo = :idEquipo")
    suspend fun obtenerPorEquipo(idEquipo: Int): List<NadadorEquipoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(lista: List<NadadorEquipoEntity>)

    @Query("DELETE FROM nadadores_equipo WHERE idEquipo = :idEquipo")
    suspend fun eliminarPorEquipo(idEquipo: Int)
}

@Dao
interface RutinaDao {
    @Query("SELECT * FROM rutinas WHERE idUsuario = :idUsuario")
    suspend fun obtenerPorUsuario(idUsuario: Int): List<RutinaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(lista: List<RutinaEntity>)

    @Query("DELETE FROM rutinas WHERE idUsuario = :idUsuario")
    suspend fun eliminarPorUsuario(idUsuario: Int)
}

@Dao
interface MarcaDeTiempoDao {
    @Query("SELECT * FROM marcas_tiempo WHERE idNadadorEquipo = :idNadadorEquipo")
    suspend fun obtenerPorNadadorEquipo(idNadadorEquipo: Int): List<MarcaDeTiempoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(lista: List<MarcaDeTiempoEntity>)

    @Query("DELETE FROM marcas_tiempo WHERE idNadadorEquipo = :idNadadorEquipo")
    suspend fun eliminarPorNadadorEquipo(idNadadorEquipo: Int)
}
