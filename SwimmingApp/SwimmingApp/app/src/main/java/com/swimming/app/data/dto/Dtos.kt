package com.swimming.app.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Envoltorio estándar de todas las respuestas de la API.
 * Coincide con la clase ApiResponse<T> del backend.
 * @param T tipo de los datos devueltos en la respuesta.
 */
data class ApiResponseDto<T>(
    @SerializedName("exito") val exito: Boolean,
    @SerializedName("mensaje") val mensaje: String,
    @SerializedName("datos") val datos: T?
)

/**
 * DTO de entrada para crear o actualizar un nadador.
 * Se envía al servidor en el cuerpo de la petición HTTP.
 */
data class NadadorRequestDto(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellidos") val apellidos: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

/**
 * DTO de salida con los datos públicos de un nadador
 * recibidos desde la API tras una consulta.
 */
data class NadadorResponseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("idNadador") val idNadador: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellidos") val apellidos: String,
    @SerializedName("email") val email: String,
    @SerializedName("idEquipo") val idEquipo: Int?,
    @SerializedName("idNadadorEquipo") val idNadadorEquipo: Int?
)

/**
 * DTO de entrada para crear o actualizar un entrenador.
 */
data class EntrenadorRequestDto(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellidos") val apellidos: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

/**
 * DTO de salida con los datos públicos de un entrenador
 * recibidos desde la API tras una consulta.
 */
data class EntrenadorResponseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("idEntrenador") val idEntrenador: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellidos") val apellidos: String,
    @SerializedName("email") val email: String,
    @SerializedName("idEquipo") val idEquipo: Int?,
    @SerializedName("idEquipoGestionado") val idEquipoGestionado: Int?
)

/**
 * DTO de entrada para crear o actualizar un equipo.
 * Si se incluye idEntrenador, el equipo queda vinculado al entrenador creador.
 */
data class EquipoRequestDto(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("idEntrenador") val idEntrenador: Int?
)

/**
 * DTO de salida con los datos de un equipo,
 * incluyendo el número total de nadadores que tiene.
 */
data class EquipoResponseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("idEquipo") val idEquipo: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("totalNadadores") val totalNadadores: Int
)

/**
 * DTO de entrada para crear un NadadorEquipo dentro de un equipo.
 * Solo lo puede crear el entrenador.
 */
data class NadadorEquipoRequestDto(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellidos") val apellidos: String,
    @SerializedName("idEquipo") val idEquipo: Int
)

/**
 * DTO de salida con los datos de un NadadorEquipo.
 * El campo `codigo` es el de 6 dígitos único que usa el nadador para vincularse.
 */
data class NadadorEquipoResponseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("idNadadorEquipo") val idNadadorEquipo: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellidos") val apellidos: String,
    @SerializedName("codigo") val codigo: Int,
    @SerializedName("idEquipo") val idEquipo: Int
)

/**
 * DTO de entrada para crear o actualizar una rutina.
 * La fecha se envía como String en formato ISO para que GSON la serialice correctamente.
 */
data class RutinaRequestDto(
    @SerializedName("contenido") val contenido: String,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("mostrar") val mostrar: Boolean,
    @SerializedName("idUsuario") val idUsuario: Int
)

/**
 * DTO de salida con los datos de una rutina.
 */
data class RutinaResponseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("idRutina") val idRutina: Int,
    @SerializedName("contenido") val contenido: String,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("mostrar") val mostrar: Boolean,
    @SerializedName("idUsuario") val idUsuario: Int
)

/**
 * DTO de entrada para registrar una marca de tiempo.
 * Si `idNadador` es nulo, la marca la ha asignado el entrenador.
 * Si `idNadador` tiene valor, la ha registrado el propio nadador.
 */
data class MarcaDeTiempoRequestDto(
    @SerializedName("tiempo") val tiempo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("idNadadorEquipo") val idNadadorEquipo: Int?,
    @SerializedName("idNadador") val idNadador: Int?
)

/**
 * DTO de salida con los datos de una marca de tiempo recibidos desde la API.
 */
data class MarcaDeTiempoResponseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("idMarca") val idMarca: Int,
    @SerializedName("tiempo") val tiempo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("idNadadorEquipo") val idNadadorEquipo: Int,
    @SerializedName("idNadador") val idNadador: Int?
)

/**
 * DTO de entrada para que un nadador se vincule a un equipo
 * introduciendo el código de 6 dígitos que le proporciona su entrenador.
 */
data class VincularCodigoRequestDto(
    @SerializedName("codigo") val codigo: Int
)