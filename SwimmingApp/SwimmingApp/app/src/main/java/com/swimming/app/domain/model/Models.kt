package com.swimming.app.domain.model

/**
 * Modelo de dominio que representa a un nadador.
 * Es un objeto puro de Kotlin sin acoplarse a Retrofit ni a Room,
 * por lo que es la representación que circula por la lógica de la app.
 */
data class Nadador(
    val id: Int,
    val idNadador: Int,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val idEquipo: Int?,
    val idNadadorEquipo: Int?
)

/**
 * Modelo de dominio que representa a un entrenador.
 */
data class Entrenador(
    val id: Int,
    val idEntrenador: Int,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val idEquipo: Int?,
    val idEquipoGestionado: Int?
)

/**
 * Modelo de dominio que representa un equipo de natación,
 * incluyendo el número total de nadadores que tiene asociados.
 */
data class Equipo(
    val id: Int,
    val idEquipo: Int,
    val nombre: String,
    val totalNadadores: Int
)

/**
 * Modelo de dominio que representa una ficha de nadador dentro de un equipo.
 * Incluye el código único de 6 dígitos que se usa para vincular a un usuario.
 */
data class NadadorEquipo(
    val id: Int,
    val idNadadorEquipo: Int,
    val nombre: String,
    val apellidos: String,
    val codigo: Int,
    val idEquipo: Int
)

/**
 * Modelo de dominio que representa una rutina de entrenamiento.
 * La fecha se mantiene como String porque ya viene formateada desde la API.
 */
data class Rutina(
    val id: Int,
    val idRutina: Int,
    val contenido: String,
    val fecha: String,
    val mostrar: Boolean,
    val idUsuario: Int
)

/**
 * Modelo de dominio que representa una marca de tiempo registrada en una prueba.
 * Si idNadador es nulo, significa que la marca la asignó el entrenador.
 */
data class MarcaDeTiempo(
    val id: Int,
    val idMarca: Int,
    val tiempo: String,
    val descripcion: String,
    val idNadadorEquipo: Int?,
    val idNadador: Int?
)