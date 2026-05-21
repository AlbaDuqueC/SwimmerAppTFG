package com.swimming.app.utils

/**
 * Constantes globales de la aplicación.
 * Centraliza valores que se usan en varios sitios para evitar duplicarlos
 * y permitir cambiarlos fácilmente desde un único punto.
 */
object Constants {
    /** URL base de la API desplegada en Render. */
    const val BASE_URL = "https://swimming-api.onrender.com/api/"

    /** Identificador del rol nadador en la sesión local. */
    const val ROL_NADADOR = "NADADOR"

    /** Identificador del rol entrenador en la sesión local. */
    const val ROL_ENTRENADOR = "ENTRENADOR"
}