package com.acdev.fitter.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * Vocabulario de movimiento de Pulse. Ninguna animacion de la app declara duraciones o curvas
 * propias: se elige una de estas.
 *
 * Idea rectora: nada aparece de golpe, todo crece desde su origen. Los muelles se reservan para
 * lo que cambia de tamano; las curvas, para lo que cambia de opacidad o posicion.
 */
object FitterMotion {

    /** Duraciones en milisegundos. */
    object Duration {
        const val INSTANT = 90
        const val QUICK = 180
        const val STANDARD = 280
        const val EMPHASIZED = 420
        const val ISLAND = 480
        const val AMBIENT = 1400
    }

    /** Curvas. */
    object Easings {
        /** Movimiento general de entrada y salida. */
        val standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

        /** Transiciones destacadas entre pantallas. */
        val emphasized: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

        /** Solo para elementos que se expanden: incluye un ligero sobrepaso. */
        val overshoot: Easing = CubicBezierEasing(0.34f, 1.32f, 0.44f, 1f)
    }

    /** Muelle de la isla y de todo contenedor que cambia de tamano. */
    fun <T> islandSpring(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 0.72f, stiffness = 380f)

    /** Muelle contenido, sin rebote perceptible: listas, tarjetas, chips. */
    fun <T> gentleSpring(): FiniteAnimationSpec<T> =
        spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)

    /** Curva estandar como spec reutilizable. */
    fun <T> standardTween(durationMillis: Int = Duration.STANDARD): FiniteAnimationSpec<T> =
        tween(durationMillis = durationMillis, easing = Easings.standard)

    /** Curva destacada, para cambios de pantalla. */
    fun <T> emphasizedTween(durationMillis: Int = Duration.EMPHASIZED): FiniteAnimationSpec<T> =
        tween(durationMillis = durationMillis, easing = Easings.emphasized)
}
