package com.swimming.app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio que comprueba si el dispositivo tiene conexión activa a internet.
 * Lo utilizan los repositorios para decidir si llamar a la API
 * o servir directamente desde la caché local de Room.
 */
@Singleton
class NetworkChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Devuelve true si hay una red activa con capacidad de acceso a internet.
     * No garantiza que internet funcione realmente, solo que el sistema
     * cree que hay una conexión disponible.
     */
    fun hayConexion(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        val resultado = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        return resultado
    }
}