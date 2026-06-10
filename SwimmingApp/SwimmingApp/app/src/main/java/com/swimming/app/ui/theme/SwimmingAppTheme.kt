package com.swimming.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Color primario de la marca: azul marino oscuro.
private val AzulMarca = Color(0xFF0A2A3D)
// Color secundario: azul claro de acento.
private val AzulAcento = Color(0xFF29B6F6)
// Fondo general de la app.
private val FondoClaro = Color(0xFFEAF4FB)

/**
 * Esquema de colores de Material3 personalizado para DolphinSwimmer.
 * Se usa lightColorScheme como base y se sobreescriben solo los colores de marca.
 */
private val EsquemaColores = lightColorScheme(
    primary = AzulMarca,
    onPrimary = Color.White,
    secondary = AzulAcento,
    onSecondary = Color.White,
    background = FondoClaro,
    surface = Color.White,
    onBackground = AzulMarca,
    onSurface = AzulMarca
)

/**
 * Tema principal de la aplicación.
 * Envuelve el árbol de Compose con el esquema de colores, la tipografía
 * y las formas de Material3.
 */
@Composable
fun SwimmingAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaColores,
        content = content
    )
}
