package com.swimming.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.swimming.app.data.local.dao.*
import com.swimming.app.data.local.entity.*

/**
 * Base de datos local de la aplicación gestionada con Room.
 * Actúa como caché en SQLite para que la app pueda mostrar datos
 * incluso cuando no hay conexión a internet.
 *
 * Cada entidad declarada en `entities` genera una tabla SQL.
 * Los DAOs proporcionan los métodos para consultarlas y modificarlas.
 */
@Database(
    entities = [
        NadadorEntity::class,
        EntrenadorEntity::class,
        EquipoEntity::class,
        NadadorEquipoEntity::class,
        RutinaEntity::class,
        MarcaDeTiempoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** Acceso a las operaciones de la tabla "nadadores". */
    abstract fun nadadorDao(): NadadorDao

    /** Acceso a las operaciones de la tabla "entrenadores". */
    abstract fun entrenadorDao(): EntrenadorDao

    /** Acceso a las operaciones de la tabla "equipos". */
    abstract fun equipoDao(): EquipoDao

    /** Acceso a las operaciones de la tabla "nadadores_equipo". */
    abstract fun nadadorEquipoDao(): NadadorEquipoDao

    /** Acceso a las operaciones de la tabla "rutinas". */
    abstract fun rutinaDao(): RutinaDao

    /** Acceso a las operaciones de la tabla "marcas_tiempo". */
    abstract fun marcaDeTiempoDao(): MarcaDeTiempoDao
}