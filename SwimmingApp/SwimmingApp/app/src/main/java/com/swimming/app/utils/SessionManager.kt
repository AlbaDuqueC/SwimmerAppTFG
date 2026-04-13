package com.swimming.app.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("swimming_prefs", Context.MODE_PRIVATE)

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

    fun getUserId(): Int = prefs.getInt("userId", -1)
    fun getUserEmail(): String = prefs.getString("userEmail", "") ?: ""
    fun getUserRol(): String = prefs.getString("userRol", "") ?: ""
    fun getUserNombre(): String = prefs.getString("userName", "") ?: ""
    fun getUserApellidos(): String = prefs.getString("userApellidos", "") ?: ""
    fun getEquipoId(): Int? = prefs.getInt("userEquipoId", -1).takeIf { it != -1 }
    fun haySession(): Boolean = getUserId() != -1
    fun esEntrenador(): Boolean = getUserRol() == Constants.ROL_ENTRENADOR

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }
}
