package com.acdev.fitter.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.acdev.fitter.R
import kotlinx.serialization.Serializable

/**
 * Destinos de la app, con rutas de tipo seguro.
 *
 * No hay cadenas de ruta: se navega con objetos, de modo que el compilador detecta un destino
 * inexistente o un argumento que falta.
 */
sealed interface FitterRoute {

    @Serializable
    data object Dashboard : FitterRoute

    @Serializable
    data object Training : FitterRoute

    @Serializable
    data object Nutrition : FitterRoute

    @Serializable
    data object Progress : FitterRoute

    @Serializable
    data object Profile : FitterRoute

    @Serializable
    data object Settings : FitterRoute

    /** Editor de una rutina concreta. */
    @Serializable
    data class RoutineEditor(val routineId: String) : FitterRoute

    /** Editor de una lista de ejercicios. */
    @Serializable
    data class WorkoutEditor(val workoutId: String) : FitterRoute

    /** Editor de un ejercicio propio. Sin id es un alta. */
    @Serializable
    data class ExerciseEditor(val exerciseId: String? = null) : FitterRoute
}

/**
 * Pestanas de la barra inferior.
 *
 * El orden del enum es el orden en pantalla; anadir una pestana es anadir una constante y nada
 * mas: ni la barra ni el grafo necesitan cambios adicionales.
 */
enum class TopLevelDestination(
    val route: FitterRoute,
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int
) {
    DASHBOARD(FitterRoute.Dashboard, R.string.nav_dashboard, R.drawable.ic_home),
    TRAINING(FitterRoute.Training, R.string.nav_training, R.drawable.ic_dumbbell),
    NUTRITION(FitterRoute.Nutrition, R.string.nav_nutrition, R.drawable.ic_nutrition),
    PROGRESS(FitterRoute.Progress, R.string.nav_progress, R.drawable.ic_chart),
    PROFILE(FitterRoute.Profile, R.string.nav_profile, R.drawable.ic_user)
}
