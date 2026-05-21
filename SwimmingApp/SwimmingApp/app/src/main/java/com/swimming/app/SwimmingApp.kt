package com.swimming.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase principal de la aplicación.
 * Se declara en el AndroidManifest.xml como punto de entrada de Hilt
 * con la anotación @HiltAndroidApp, lo que permite que el framework
 * genere el contenedor de dependencias y las inyecte en toda la app.
 */
@HiltAndroidApp
class SwimmingApp : Application()