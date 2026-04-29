package com.swimming.app.domain.model

data class Nadador(val id: Int, val idNadador: Int, val nombre: String, val apellidos: String, val email: String, val idEquipo: Int?, val idNadadorEquipo: Int?)
data class Entrenador(val id: Int, val idEntrenador: Int, val nombre: String, val apellidos: String, val email: String, val idEquipo: Int?, val idEquipoGestionado: Int?)
data class Equipo(val id: Int, val idEquipo: Int, val nombre: String, val totalNadadores: Int)
data class NadadorEquipo(val id: Int, val idNadadorEquipo: Int, val nombre: String, val apellidos: String, val codigo: Int, val idEquipo: Int)
data class Rutina(val id: Int, val idRutina: Int, val contenido: String, val fecha: String, val mostrar: Boolean, val idUsuario: Int)
data class MarcaDeTiempo(val id: Int, val idMarca: Int, val tiempo: String, val descripcion: String, val idNadadorEquipo: Int?, val idNadador: Int?)
