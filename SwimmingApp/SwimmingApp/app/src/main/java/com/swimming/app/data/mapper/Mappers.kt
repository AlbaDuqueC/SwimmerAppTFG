package com.swimming.app.data.mapper

import com.swimming.app.data.dto.*
import com.swimming.app.data.local.entity.*
import com.swimming.app.domain.model.*

fun NadadorResponseDto.toDomain() = Nadador(id, idNadador, nombre, apellidos, email, idEquipo, idNadadorEquipo)
fun NadadorResponseDto.toEntity() = NadadorEntity(id, idNadador, nombre, apellidos, email, idEquipo, idNadadorEquipo)
fun NadadorEntity.toDomain() = Nadador(id, idNadador, nombre, apellidos, email, idEquipo, idNadadorEquipo)

fun EntrenadorResponseDto.toDomain() = Entrenador(id, idEntrenador, nombre, apellidos, email, idEquipo, idEquipoGestionado)
fun EntrenadorResponseDto.toEntity() = EntrenadorEntity(id, idEntrenador, nombre, apellidos, email, idEquipo, idEquipoGestionado)
fun EntrenadorEntity.toDomain() = Entrenador(id, idEntrenador, nombre, apellidos, email, idEquipo, idEquipoGestionado)

fun EquipoResponseDto.toDomain() = Equipo(id, idEquipo, nombre, totalNadadores)
fun EquipoResponseDto.toEntity() = EquipoEntity(id, idEquipo, nombre, totalNadadores)
fun EquipoEntity.toDomain() = Equipo(id, idEquipo, nombre, totalNadadores)

fun NadadorEquipoResponseDto.toDomain() = NadadorEquipo(id, idNadadorEquipo, nombre, apellidos, codigo, idEquipo)
fun NadadorEquipoResponseDto.toEntity() = NadadorEquipoEntity(id, idNadadorEquipo, nombre, apellidos, codigo, idEquipo)
fun NadadorEquipoEntity.toDomain() = NadadorEquipo(id, idNadadorEquipo, nombre, apellidos, codigo, idEquipo)

fun RutinaResponseDto.toDomain() = Rutina(id, idRutina, contenido, fecha, mostrar, idUsuario)
fun RutinaResponseDto.toEntity() = RutinaEntity(id, idRutina, contenido, fecha, mostrar, idUsuario)
fun RutinaEntity.toDomain() = Rutina(id, idRutina, contenido, fecha, mostrar, idUsuario)

fun MarcaDeTiempoResponseDto.toDomain() = MarcaDeTiempo(
    id = id,
    idMarca = idMarca,
    tiempo = tiempo,
    descripcion = descripcion,
    idNadadorEquipo = idNadadorEquipo ?: 0,   // ← ojo aquí
    idNadador = idNadador
)
fun MarcaDeTiempoResponseDto.toEntity() = MarcaDeTiempoEntity(id, idMarca, tiempo, descripcion, idNadadorEquipo, idNadador)
fun MarcaDeTiempoEntity.toDomain() = MarcaDeTiempo(id, idMarca, tiempo, descripcion, idNadadorEquipo, idNadador)
