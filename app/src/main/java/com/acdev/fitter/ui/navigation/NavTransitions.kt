package com.acdev.fitter.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import com.acdev.fitter.ui.theme.FitterMotion

/**
 * Transiciones de navegacion de Fitter.
 *
 * Dos gramaticas y ninguna mas:
 * - entre pestanas, fundido con un micro-zoom, porque son destinos hermanos y no hay jerarquia;
 * - hacia una pantalla de detalle, desplazamiento lateral, porque si la hay.
 *
 * Se aplican en el `NavHost`, nunca pantalla por pantalla.
 */
object FitterTransitions {

    private const val SLIDE_FRACTION = 6

    /** Fundido entre destinos hermanos. */
    val tabEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(tween(FitterMotion.Duration.STANDARD, easing = FitterMotion.Easings.standard)) +
            scaleIn(
                initialScale = 0.98f,
                animationSpec = tween(FitterMotion.Duration.STANDARD, easing = FitterMotion.Easings.standard)
            )
    }

    val tabExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(tween(FitterMotion.Duration.QUICK, easing = FitterMotion.Easings.standard)) +
            scaleOut(
                targetScale = 0.98f,
                animationSpec = tween(FitterMotion.Duration.QUICK, easing = FitterMotion.Easings.standard)
            )
    }

    /** Entrada de una pantalla de detalle. */
    val forwardEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInHorizontally(
            animationSpec = tween(FitterMotion.Duration.EMPHASIZED, easing = FitterMotion.Easings.emphasized),
            initialOffsetX = { it / SLIDE_FRACTION }
        ) + fadeIn(tween(FitterMotion.Duration.STANDARD))
    }

    val forwardExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutHorizontally(
            animationSpec = tween(FitterMotion.Duration.EMPHASIZED, easing = FitterMotion.Easings.emphasized),
            targetOffsetX = { -it / SLIDE_FRACTION }
        ) + fadeOut(tween(FitterMotion.Duration.QUICK))
    }

    val backEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInHorizontally(
            animationSpec = tween(FitterMotion.Duration.EMPHASIZED, easing = FitterMotion.Easings.emphasized),
            initialOffsetX = { -it / SLIDE_FRACTION }
        ) + fadeIn(tween(FitterMotion.Duration.STANDARD))
    }

    val backExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutHorizontally(
            animationSpec = tween(FitterMotion.Duration.EMPHASIZED, easing = FitterMotion.Easings.emphasized),
            targetOffsetX = { it / SLIDE_FRACTION }
        ) + fadeOut(tween(FitterMotion.Duration.QUICK))
    }
}
