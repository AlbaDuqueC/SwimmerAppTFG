package com.swimming.app.di

import android.content.Context
import androidx.room.Room
import com.swimming.app.data.local.AppDatabase
import com.swimming.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt que proporciona la base de datos Room y sus DAOs
 * al resto de la aplicación.
 *
 * Usa @Provides en lugar de @Binds porque hay que construir manualmente
 * la instancia de la base de datos con Room.databaseBuilder().
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Crea y proporciona la instancia única de la base de datos Room.
     *
     * @Singleton garantiza que solo exista una instancia en toda la app.
     * @ApplicationContext inyecta el contexto a nivel de aplicación (no de Activity)
     * para evitar fugas de memoria.
     *
     * fallbackToDestructiveMigration: si cambia la versión del esquema,
     * destruye la base de datos y la vuelve a crear desde cero
     * (válido para una app local de caché, no se pierden datos críticos).
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        val resultado = Room.databaseBuilder(context, AppDatabase::class.java, "swimming_db")
            .fallbackToDestructiveMigration()
            .build()
        return resultado
    }

    /** Proporciona el DAO para acceder a la tabla "nadadores". */
    @Provides fun provideNadadorDao(db: AppDatabase): NadadorDao = db.nadadorDao()

    /** Proporciona el DAO para acceder a la tabla "entrenadores". */
    @Provides fun provideEntrenadorDao(db: AppDatabase): EntrenadorDao = db.entrenadorDao()

    /** Proporciona el DAO para acceder a la tabla "equipos". */
    @Provides fun provideEquipoDao(db: AppDatabase): EquipoDao = db.equipoDao()

    /** Proporciona el DAO para acceder a la tabla "nadadores_equipo". */
    @Provides fun provideNadadorEquipoDao(db: AppDatabase): NadadorEquipoDao = db.nadadorEquipoDao()

    /** Proporciona el DAO para acceder a la tabla "rutinas". */
    @Provides fun provideRutinaDao(db: AppDatabase): RutinaDao = db.rutinaDao()

    /** Proporciona el DAO para acceder a la tabla "marcas_tiempo". */
    @Provides fun provideMarcaDeTiempoDao(db: AppDatabase): MarcaDeTiempoDao = db.marcaDeTiempoDao()
}