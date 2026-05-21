package com.swimming.app.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de la sesión del usuario.
 * Persiste los datos del usuario logueado (ID, email, rol, nombre, equipo...)
 * en SharedPreferences para que sobrevivan al cierre de la aplicación.
 *
 * Se registra como Singleton para que toda la app comparta la misma instancia.
 */
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context
) {
    // Archivo SharedPreferences donde se guardan los datos de sesión.
    private val prefs: SharedPreferences =
        context.getSharedPreferences("swimming_prefs", Context.MODE_PRIVATE)

    /**
     * Guarda los datos básicos del usuario tras un login correcto.
     * Si equipoId es null, se almacena -1 como marcador de "sin equipo".
     */
    fun guardarSesion(id: Int, email: String, rol: String, nombre: String, apellidos: String, equipoId: Int?) {
        prefs.edit()
            .putInt("userId", id)
            .putString("userEmail", email)
            .putString("userRol", rol)
            .putString("userName", nombre)
            .putString("userApellidos", apellidos)
            .putInt("userEquipoId", equipoId ?: -1)
            .apply()
    }

    /** Devuelve el ID del usuario, o -1 si no hay sesión iniciada. */
    fun getUserId(): Int = prefs.getInt("userId", -1)

    /** Devuelve el email del usuario, o cadena vacía si no hay. */
    fun getUserEmail(): String = prefs.getString("userEmail", "") ?: ""

    /** Devuelve el rol del usuario (NADADOR o ENTRENADOR). */
    fun getUserRol(): String = prefs.getString("userRol", "") ?: ""

    /** Devuelve el nombre del usuario. */
    fun getUserNombre(): String = prefs.getString("userName", "") ?: ""

    /** Devuelve los apellidos del usuario. */
    fun getUserApellidos(): String = prefs.getString("userApellidos", "") ?: ""

    /** Devuelve el ID del equipo del usuario, o null si no tiene equipo asignado. */
    fun getEquipoId(): Int? = prefs.getInt("userEquipoId", -1).takeIf { it != -1 }

    /** Indica si hay una sesión activa en este momento. */
    fun haySession(): Boolean = getUserId() != -1

    /** Indica si el usuario logueado es entrenador. */
    fun esEntrenador(): Boolean = getUserRol() == Constants.ROL_ENTRENADOR

    /** Borra todos los datos de sesión al cerrar sesión. */
    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }

    /** Guarda el ID del NadadorEquipo (la ficha del nadador en su equipo). */
    fun guardarIdNadadorEquipo(idNadadorEquipo: Int) {
        prefs.edit().putInt("idNadadorEquipo", idNadadorEquipo).apply()
    }

    /** Guarda el ID del nadador (usuario). */
    fun guardarIdNadador(idNadador: Int?) {
        prefs.edit().putInt("idNadador", idNadador ?: -1).apply()
    }

    /** Devuelve el ID del NadadorEquipo o -1 si no está asignado. */
    fun getIdNadadorEquipo(): Int = prefs.getInt("idNadadorEquipo", -1)

    /** Devuelve el ID del nadador o -1 si no está asignado. */
    fun getIdNadador(): Int = prefs.getInt("idNadador", -1)

    /** Guarda el ID del equipo del usuario. */
    fun guardarEquipoId(equipoId: Int) {
        prefs.edit().putInt("userEquipoId", equipoId).apply()
    }

    /** Borra el equipoId de la sesión local (tras eliminar el equipo). */
    fun borrarEquipoId() {
        prefs.edit().putInt("userEquipoId", -1).apply()
    }

    /** Actualiza solo el nombre y apellidos en la sesión local (tras editar perfil). */
    fun actualizarNombreApellidos(nombre: String, apellidos: String) {
        prefs.edit()
            .putString("userName", nombre)
            .putString("userApellidos", apellidos)
            .apply()
    }
}