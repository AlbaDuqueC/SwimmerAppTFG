package com.swimming.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa un nadador en la base de datos local del dispositivo.
 * Sirve como caché para que la app pueda mostrar datos cuando no hay conexión.
 */
@Entity(tableName = "nadadores")
data class NadadorEntity(
    @PrimaryKey val id: Int,
    val idNadador: Int,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val idEquipo: Int?,
    val idNadadorEquipo: Int?
)

/**
 * Entidad Room que representa un entrenador en la base de datos local.
 * Sirve como caché para mostrar datos sin conexión a internet.
 */
@Entity(tableName = "entrenadores")
data class EntrenadorEntity(
    @PrimaryKey val id: Int,
    val idEntrenador: Int,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val idEquipo: Int?,
    val idEquipoGestionado: Int?
)

/**
 * Entidad Room que representa un equipo en la base de datos local.
 * Incluye el número total de nadadores para mostrarlo sin recalcular.
 */
@Entity(tableName = "equipos")
data class EquipoEntity(
    @PrimaryKey val id: Int,
    val idEquipo: Int,
    val nombre: String,
    val totalNadadores: Int
)

/**
 * Entidad Room que representa un NadadorEquipo (ficha de un nadador en un equipo)
 * en la base de datos local. El código se almacena para futuras vinculaciones.
 */
@Entity(tableName = "nadadores_equipo")
data class NadadorEquipoEntity(
    @PrimaryKey val id: Int,
    val idNadadorEquipo: Int,
    val nombre: String,
    val apellidos: String,
    val codigo: Int,
    val idEquipo: Int
)

/**
 * Entidad Room que representa una rutina en la base de datos local.
 * La fecha se guarda como String en formato ISO para simplificar la persistencia.
 */
@Entity(tableName = "rutinas")
data class RutinaEntity(
    @PrimaryKey val id: Int,
    val idRutina: Int,
    val contenido: String,
    val fecha: String,
    val mostrar: Boolean,
    val idUsuario: Int
)

/**
 * Entidad Room que representa una marca de tiempo en la base de datos local.
 * Si idNadador es nulo, significa que la marca la asignó el entrenador.
 */
@Entity(tableName = "marcas_tiempo")
data class MarcaDeTiempoEntity(
    @PrimaryKey val id: Int,
    val idMarca: Int,
    val tiempo: String,
    val descripcion: String,
    val idNadadorEquipo: Int,
    val idNadador: Int?
)