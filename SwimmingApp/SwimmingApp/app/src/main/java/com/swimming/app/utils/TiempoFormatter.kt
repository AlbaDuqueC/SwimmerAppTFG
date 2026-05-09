package com.swimming.app.utils

/**
 * Formatea un tiempo recibido del API ("00:HH:MM:SS.fffffff") al formato visual "MM:SS.cc"
 * (centésimas, dos decimales). Si la entrada ya está en formato corto, la respeta.
 */
object TiempoFormatter {

    /** Convierte "00:01:28.8500000" → "01:28.85". */
    fun aFormatoCorto(tiempoApi: String): String {
        if (tiempoApi.isBlank()) return "00:00.00"

        // Quitar las horas si vienen y son 00
        val sinHoras = if (tiempoApi.startsWith("00:") && tiempoApi.count { it == ':' } >= 2) {
            tiempoApi.removePrefix("00:")
        } else tiempoApi

        // Separar parte de segundos.decimal
        val partes = sinHoras.split(":")
        val minutos = partes.getOrNull(0)?.padStart(2, '0') ?: "00"
        val segYDec = partes.getOrNull(1) ?: "00"

        // Recortar las décimas/centésimas a 2 dígitos
        val (seg, dec) = if (segYDec.contains(".")) {
            val (s, d) = segYDec.split(".")
            s to d.padEnd(2, '0').take(2)
        } else {
            segYDec to "00"
        }

        return "%s:%s.%s".format(minutos, seg.padStart(2, '0'), dec)
    }
}