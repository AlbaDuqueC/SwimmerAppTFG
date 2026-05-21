package com.swimming.app.di

import com.swimming.app.data.repository.*
import com.swimming.app.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt que conecta las interfaces de los repositorios (capa de dominio)
 * con sus implementaciones concretas (capa de datos).
 *
 * Usa @Binds en lugar de @Provides porque es más eficiente cuando solo se trata
 * de asociar una interfaz con su clase implementadora, sin lógica adicional.
 *
 * @InstallIn(SingletonComponent::class) indica que las dependencias proporcionadas
 * aquí estarán vivas durante toda la ejecución de la aplicación.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    /** Asocia la interfaz NadadorRepository con su implementación concreta. */
    @Binds @Singleton
    abstract fun bindNadadorRepository(impl: NadadorRepositoryImpl): NadadorRepository

    /** Asocia la interfaz EntrenadorRepository con su implementación concreta. */
    @Binds @Singleton
    abstract fun bindEntrenadorRepository(impl: EntrenadorRepositoryImpl): EntrenadorRepository

    /** Asocia la interfaz EquipoRepository con su implementación concreta. */
    @Binds @Singleton
    abstract fun bindEquipoRepository(impl: EquipoRepositoryImpl): EquipoRepository

    /** Asocia la interfaz NadadorEquipoRepository con su implementación concreta. */
    @Binds @Singleton
    abstract fun bindNadadorEquipoRepository(impl: NadadorEquipoRepositoryImpl): NadadorEquipoRepository

    /** Asocia la interfaz RutinaRepository con su implementación concreta. */
    @Binds @Singleton
    abstract fun bindRutinaRepository(impl: RutinaRepositoryImpl): RutinaRepository

    /** Asocia la interfaz MarcaDeTiempoRepository con su implementación concreta. */
    @Binds @Singleton
    abstract fun bindMarcaDeTiempoRepository(impl: MarcaDeTiempoRepositoryImpl): MarcaDeTiempoRepository
}