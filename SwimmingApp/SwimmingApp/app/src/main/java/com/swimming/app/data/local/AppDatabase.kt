package com.swimming.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.swimming.app.data.local.dao.*
import com.swimming.app.data.local.entity.*

@Database(
    entities = [NadadorEntity::class, EntrenadorEntity::class, EquipoEntity::class, NadadorEquipoEntity::class, RutinaEntity::class, MarcaDeTiempoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun nadadorDao(): NadadorDao
    abstract fun entrenadorDao(): EntrenadorDao
    abstract fun equipoDao(): EquipoDao
    abstract fun nadadorEquipoDao(): NadadorEquipoDao
    abstract fun rutinaDao(): RutinaDao
    abstract fun marcaDeTiempoDao(): MarcaDeTiempoDao
}
