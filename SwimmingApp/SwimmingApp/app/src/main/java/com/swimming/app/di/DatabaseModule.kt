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

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        val resultado = Room.databaseBuilder(context, AppDatabase::class.java, "swimming_db")
            .fallbackToDestructiveMigration()
            .build()
        return resultado
    }

    @Provides fun provideNadadorDao(db: AppDatabase): NadadorDao = db.nadadorDao()
    @Provides fun provideEntrenadorDao(db: AppDatabase): EntrenadorDao = db.entrenadorDao()
    @Provides fun provideEquipoDao(db: AppDatabase): EquipoDao = db.equipoDao()
    @Provides fun provideNadadorEquipoDao(db: AppDatabase): NadadorEquipoDao = db.nadadorEquipoDao()
    @Provides fun provideRutinaDao(db: AppDatabase): RutinaDao = db.rutinaDao()
    @Provides fun provideMarcaDeTiempoDao(db: AppDatabase): MarcaDeTiempoDao = db.marcaDeTiempoDao()
}
