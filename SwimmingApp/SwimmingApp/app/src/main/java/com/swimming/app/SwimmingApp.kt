package com.swimming.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Clase principal de la aplicación. Necesaria para que Hilt funcione. */
@HiltAndroidApp
class SwimmingApp : Application()
