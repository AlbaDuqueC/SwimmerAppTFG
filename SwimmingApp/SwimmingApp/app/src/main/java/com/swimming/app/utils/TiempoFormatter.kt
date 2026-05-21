package com.swimming.app.utils

/**
 * Utilidad para formatear los tiempos de natación entre el formato de la API
 * y el formato visual que se muestra al usuario.
 *
 * La API devuelve los tiempos como "00:HH:MM:SS.fffffff" (TimeSpan de .NET),
 * pero al usuario le interesa verlos en formato corto "MM:SS.cc" con centésimas.
 */
object TiempoFormatter {

    /**
     * Convierte un tiempo recibido del API al formato corto "MM:SS.cc".
     * Ejemplo: "00:01:28.8500000" → "01:28.85".
     * Si la entrada está vacía devuelve "00:00.00".
     */
    fun aFormatoCorto(tiempoApi: String): String {
        if (tiempoApi.isBlank()) return "00:00.00"

        // Si las horas son 00, se eliminan del formato para acortarlo.
        val sinHoras = if (tiempoApi.startsWith("00:") && tiempoApi.count { it == ':' } >= 2) {
            tiempoApi.removePrefix("00:")
        } else tiempoApi

        // Separar minutos de la parte segundos.decimal.
        val partes = sinHoras.split(":")
        val minutos = partes.getOrNull(0)?.padStart(2, '0') ?: "00"
        val segYDec = partes.getOrNull(1) ?: "00"

        // Recortar las décimas/centésimas a 2 dígitos (ignorando ceros sobrantes).
        val (seg, dec) = if (segYDec.contains(".")) {
            val (s, d) = segYDec.split(".")
            s to d.padEnd(2, '0').take(2)
        } else {
            segYDec to "00"
        }

        return "%s:%s.%s".format(minutos, seg.padStart(2, '0'), dec)
    }
}