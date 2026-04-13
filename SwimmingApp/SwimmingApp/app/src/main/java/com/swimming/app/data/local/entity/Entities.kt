package com.swimming.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nadadores")
data class NadadorEntity(@PrimaryKey val id: Int, val idNadador: Int, val nombre: String, val apellidos: String, val email: String, val idEquipo: Int?, val idNadadorEquipo: Int?)

@Entity(tableName = "entrenadores")
data class EntrenadorEntity(@PrimaryKey val id: Int, val idEntrenador: Int, val nombre: String, val apellidos: String, val email: String, val idEquipo: Int?, val idEquipoGestionado: Int?)

@Entity(tableName = "equipos")
data class EquipoEntity(@PrimaryKey val id: Int, val idEquipo: Int, val nombre: String, val totalNadadores: Int)

@Entity(tableName = "nadadores_equipo")
data class NadadorEquipoEntity(@PrimaryKey val id: Int, val idNadadorEquipo: Int, val nombre: String, val apellidos: String, val codigo: Int, val idEquipo: Int)

@Entity(tableName = "rutinas")
data class RutinaEntity(@PrimaryKey val id: Int, val idRutina: Int, val contenido: String, val fecha: String, val mostrar: Boolean, val idUsuario: Int)

@Entity(tableName = "marcas_tiempo")
data class MarcaDeTiempoEntity(@PrimaryKey val id: Int, val idMarca: Int, val tiempo: String, val descripcion: String, val idNadadorEquipo: Int, val idNadador: Int?)
