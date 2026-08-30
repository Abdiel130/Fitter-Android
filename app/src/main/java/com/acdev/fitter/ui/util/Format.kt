package com.acdev.fitter.ui.util

import java.util.Locale
import kotlin.math.roundToInt

/**
 * Formateo de cifras para la interfaz.
 *
 * Se centraliza aqui para que un peso o una duracion se lean igual en toda la app y para que el
 * separador decimal siga el idioma del dispositivo.
 */
object Format {

    /** Segundos a `m:ss`. Se usa en el cronometro de descanso y en la isla. */
    fun duration(totalSeconds: Int): String {
        val safeSeconds = totalSeconds.coerceAtLeast(0)
        return String.format(Locale.getDefault(), "%d:%02d", safeSeconds / 60, safeSeconds % 60)
    }

    /** Un decimal, para litros, horas de sueno o peso corporal. */
    fun oneDecimal(value: Double): String =
        String.format(Locale.getDefault(), "%.1f", value)

    /** Entero redondeado, para calorias, gramos de macro o series. */
    fun integer(value: Double): String = value.roundToInt().toString()

    /** Mililitros a litros con un decimal. */
    fun litres(millilitres: Int): String = oneDecimal(millilitres / 1000.0)
}
