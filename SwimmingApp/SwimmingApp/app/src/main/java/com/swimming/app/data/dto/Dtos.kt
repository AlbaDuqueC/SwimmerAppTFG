package com.swimming.app.data.dto

import com.google.gson.annotations.SerializedName

data class ApiResponseDto<T>(@SerializedName("exito") val exito: Boolean, @SerializedName("mensaje") val mensaje: String, @SerializedName("datos") val datos: T?)
data class NadadorRequestDto(@SerializedName("nombre") val nombre: String, @SerializedName("apellidos") val apellidos: String, @SerializedName("email") val email: String, @SerializedName("password") val password: String)
data class NadadorResponseDto(@SerializedName("id") val id: Int, @SerializedName("idNadador") val idNadador: Int, @SerializedName("nombre") val nombre: String, @SerializedName("apellidos") val apellidos: String, @SerializedName("email") val email: String, @SerializedName("idEquipo") val idEquipo: Int?, @SerializedName("idNadadorEquipo") val idNadadorEquipo: Int?)
data class EntrenadorRequestDto(@SerializedName("nombre") val nombre: String, @SerializedName("apellidos") val apellidos: String, @SerializedName("email") val email: String, @SerializedName("password") val password: String)
data class EntrenadorResponseDto(@SerializedName("id") val id: Int, @SerializedName("idEntrenador") val idEntrenador: Int, @SerializedName("nombre") val nombre: String, @SerializedName("apellidos") val apellidos: String, @SerializedName("email") val email: String, @SerializedName("idEquipo") val idEquipo: Int?, @SerializedName("idEquipoGestionado") val idEquipoGestionado: Int?)
data class EquipoRequestDto(@SerializedName("nombre") val nombre: String)
data class EquipoResponseDto(@SerializedName("id") val id: Int, @SerializedName("idEquipo") val idEquipo: Int, @SerializedName("nombre") val nombre: String, @SerializedName("totalNadadores") val totalNadadores: Int)
data class NadadorEquipoRequestDto(@SerializedName("nombre") val nombre: String, @SerializedName("apellidos") val apellidos: String, @SerializedName("idEquipo") val idEquipo: Int)
data class NadadorEquipoResponseDto(@SerializedName("id") val id: Int, @SerializedName("idNadadorEquipo") val idNadadorEquipo: Int, @SerializedName("nombre") val nombre: String, @SerializedName("apellidos") val apellidos: String, @SerializedName("codigo") val codigo: Int, @SerializedName("idEquipo") val idEquipo: Int)
data class RutinaRequestDto(@SerializedName("contenido") val contenido: String, @SerializedName("fecha") val fecha: String, @SerializedName("mostrar") val mostrar: Boolean, @SerializedName("idUsuario") val idUsuario: Int)
data class RutinaResponseDto(@SerializedName("id") val id: Int, @SerializedName("idRutina") val idRutina: Int, @SerializedName("contenido") val contenido: String, @SerializedName("fecha") val fecha: String, @SerializedName("mostrar") val mostrar: Boolean, @SerializedName("idUsuario") val idUsuario: Int)
data class MarcaDeTiempoRequestDto(@SerializedName("tiempo") val tiempo: String, @SerializedName("descripcion") val descripcion: String, @SerializedName("idNadadorEquipo") val idNadadorEquipo: Int, @SerializedName("idNadador") val idNadador: Int?)
data class MarcaDeTiempoResponseDto(@SerializedName("id") val id: Int, @SerializedName("idMarca") val idMarca: Int, @SerializedName("tiempo") val tiempo: String, @SerializedName("descripcion") val descripcion: String, @SerializedName("idNadadorEquipo") val idNadadorEquipo: Int, @SerializedName("idNadador") val idNadador: Int?)
