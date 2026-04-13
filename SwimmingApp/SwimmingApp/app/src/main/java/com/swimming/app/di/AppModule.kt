package com.swimming.app.di

import com.swimming.app.data.repository.*
import com.swimming.app.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds @Singleton abstract fun bindNadadorRepository(impl: NadadorRepositoryImpl): NadadorRepository
    @Binds @Singleton abstract fun bindEntrenadorRepository(impl: EntrenadorRepositoryImpl): EntrenadorRepository
    @Binds @Singleton abstract fun bindEquipoRepository(impl: EquipoRepositoryImpl): EquipoRepository
    @Binds @Singleton abstract fun bindNadadorEquipoRepository(impl: NadadorEquipoRepositoryImpl): NadadorEquipoRepository
    @Binds @Singleton abstract fun bindRutinaRepository(impl: RutinaRepositoryImpl): RutinaRepository
    @Binds @Singleton abstract fun bindMarcaDeTiempoRepository(impl: MarcaDeTiempoRepositoryImpl): MarcaDeTiempoRepository
}
