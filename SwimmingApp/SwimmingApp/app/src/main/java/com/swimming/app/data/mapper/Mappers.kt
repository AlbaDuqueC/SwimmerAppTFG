package com.swimming.app.data.mapper

import com.swimming.app.data.dto.*
import com.swimming.app.data.local.entity.*
import com.swimming.app.domain.model.*

/*
 * Funciones de extensión para convertir objetos entre las tres capas de datos.
 * Centralizar los mapeos aquí evita repetir la lógica de conversión
 * en cada repositorio.
 */

// ─── Nadador ──────────────────────────────────────────────────────────────────

/** Convierte el DTO recibido de la API en un modelo de dominio. */
fun NadadorResponseDto.toDomain() = Nadador(id, idNadador, nombre, apellidos, email, idEquipo, idNadadorEquipo)

/** Convierte el DTO recibido de la API en una entidad para guardar en Room. */
fun NadadorResponseDto.toEntity() = NadadorEntity(id, idNadador, nombre, apellidos, email, idEquipo, idNadadorEquipo)

/** Convierte la entidad de Room en un modelo de dominio. */
fun NadadorEntity.toDomain() = Nadador(id, idNadador, nombre, apellidos, email, idEquipo, idNadadorEquipo)

// ─── Entrenador ───────────────────────────────────────────────────────────────

/** Convierte el DTO recibido de la API en un modelo de dominio. */
fun EntrenadorResponseDto.toDomain() = Entrenador(id, idEntrenador, nombre, apellidos, email, idEquipo, idEquipoGestionado)

/** Convierte el DTO recibido de la API en una entidad para guardar en Room. */
fun EntrenadorResponseDto.toEntity() = EntrenadorEntity(id, idEntrenador, nombre, apellidos, email, idEquipo, idEquipoGestionado)

/** Convierte la entidad de Room en un modelo de dominio. */
fun EntrenadorEntity.toDomain() = Entrenador(id, idEntrenador, nombre, apellidos, email, idEquipo, idEquipoGestionado)

// ─── Equipo ───────────────────────────────────────────────────────────────────

/** Convierte el DTO recibido de la API en un modelo de dominio. */
fun EquipoResponseDto.toDomain() = Equipo(id, idEquipo, nombre, totalNadadores)

/** Convierte el DTO recibido de la API en una entidad para guardar en Room. */
fun EquipoResponseDto.toEntity() = EquipoEntity(id, idEquipo, nombre, totalNadadores)

/** Convierte la entidad de Room en un modelo de dominio. */
fun EquipoEntity.toDomain() = Equipo(id, idEquipo, nombre, totalNadadores)

// ─── NadadorEquipo ────────────────────────────────────────────────────────────

/** Convierte el DTO recibido de la API en un modelo de dominio. */
fun NadadorEquipoResponseDto.toDomain() = NadadorEquipo(id, idNadadorEquipo, nombre, apellidos, codigo, idEquipo)

/** Convierte el DTO recibido de la API en una entidad para guardar en Room. */
fun NadadorEquipoResponseDto.toEntity() = NadadorEquipoEntity(id, idNadadorEquipo, nombre, apellidos, codigo, idEquipo)

/** Convierte la entidad de Room en un modelo de dominio. */
fun NadadorEquipoEntity.toDomain() = NadadorEquipo(id, idNadadorEquipo, nombre, apellidos, codigo, idEquipo)

// ─── Rutina ───────────────────────────────────────────────────────────────────

/** Convierte el DTO recibido de la API en un modelo de dominio. */
fun RutinaResponseDto.toDomain() = Rutina(id, idRutina, contenido, fecha, mostrar, idUsuario)

/** Convierte el DTO recibido de la API en una entidad para guardar en Room. */
fun RutinaResponseDto.toEntity() = RutinaEntity(id, idRutina, contenido, fecha, mostrar, idUsuario)

/** Convierte la entidad de Room en un modelo de dominio. */
fun RutinaEntity.toDomain() = Rutina(id, idRutina, contenido, fecha, mostrar, idUsuario)

// ─── MarcaDeTiempo ────────────────────────────────────────────────────────────

/**
 * Convierte el DTO recibido de la API en un modelo de dominio.
 * Si `idNadadorEquipo` viene nulo desde la API se sustituye por 0 para evitar
 * que el modelo de dominio tenga que aceptar nulos en este campo.
 */
fun MarcaDeTiempoResponseDto.toDomain() = MarcaDeTiempo(
    id = id,
    idMarca = idMarca,
    tiempo = tiempo,
    descripcion = descripcion,
    idNadadorEquipo = idNadadorEquipo ?: 0,
    idNadador = idNadador
)

/** Convierte el DTO recibido de la API en una entidad para guardar en Room. */
fun MarcaDeTiempoResponseDto.toEntity() = MarcaDeTiempoEntity(id, idMarca, tiempo, descripcion, idNadadorEquipo, idNadador)

/** Convierte la entidad de Room en un modelo de dominio. */
fun MarcaDeTiempoEntity.toDomain() = MarcaDeTiempo(id, idMarca, tiempo, descripcion, idNadadorEquipo, idNadador)