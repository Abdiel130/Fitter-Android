package com.acdev.fitter.ui.icons

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.acdev.fitter.R
import com.acdev.fitter.domain.model.ExerciseIcon

/**
 * Catalogo de iconografia de Fitter.
 *
 * Todos los iconos son vectores de 24 dp con trazo de 1.9 dp y remates redondos, dibujados a
 * medida para la direccion Pulse. La app no usa emoji en ningun sitio, ni la libreria
 * `material-icons-extended` (arrastra cientos de vectores que no se usan).
 *
 * Para anadir uno: crear `res/drawable/ic_<nombre>.xml` respetando el trazo y exponerlo aqui.
 */
object FitterIcons {

    val Home: ImageVector @Composable get() = vector(R.drawable.ic_home)
    val Dumbbell: ImageVector @Composable get() = vector(R.drawable.ic_dumbbell)
    val Nutrition: ImageVector @Composable get() = vector(R.drawable.ic_nutrition)
    val Chart: ImageVector @Composable get() = vector(R.drawable.ic_chart)
    val User: ImageVector @Composable get() = vector(R.drawable.ic_user)

    val Play: ImageVector @Composable get() = vector(R.drawable.ic_play)
    val Water: ImageVector @Composable get() = vector(R.drawable.ic_water)
    val Moon: ImageVector @Composable get() = vector(R.drawable.ic_moon)
    val Sun: ImageVector @Composable get() = vector(R.drawable.ic_sun)
    val Check: ImageVector @Composable get() = vector(R.drawable.ic_check)
    val Trophy: ImageVector @Composable get() = vector(R.drawable.ic_trophy)
    val Sync: ImageVector @Composable get() = vector(R.drawable.ic_sync)
    val Timer: ImageVector @Composable get() = vector(R.drawable.ic_timer)
    val Settings: ImageVector @Composable get() = vector(R.drawable.ic_settings)
    val ChevronRight: ImageVector @Composable get() = vector(R.drawable.ic_chevron_right)
    val ArrowBack: ImageVector @Composable get() = vector(R.drawable.ic_arrow_back)
    val Plus: ImageVector @Composable get() = vector(R.drawable.ic_plus)
    val Scale: ImageVector @Composable get() = vector(R.drawable.ic_scale)
    val Flame: ImageVector @Composable get() = vector(R.drawable.ic_flame)
    val Palette: ImageVector @Composable get() = vector(R.drawable.ic_palette)
    val Shield: ImageVector @Composable get() = vector(R.drawable.ic_shield)
    val Bolt: ImageVector @Composable get() = vector(R.drawable.ic_bolt)
    val Offline: ImageVector @Composable get() = vector(R.drawable.ic_offline)
    val Construction: ImageVector @Composable get() = vector(R.drawable.ic_construction)
    val Close: ImageVector @Composable get() = vector(R.drawable.ic_close)
    val ArrowUp: ImageVector @Composable get() = vector(R.drawable.ic_arrow_up)
    val ArrowDown: ImageVector @Composable get() = vector(R.drawable.ic_arrow_down)
    val Edit: ImageVector @Composable get() = vector(R.drawable.ic_edit)
    val Trash: ImageVector @Composable get() = vector(R.drawable.ic_trash)
    val Search: ImageVector @Composable get() = vector(R.drawable.ic_search)
    val Lock: ImageVector @Composable get() = vector(R.drawable.ic_lock)
    val Copy: ImageVector @Composable get() = vector(R.drawable.ic_copy)
    val Layers: ImageVector @Composable get() = vector(R.drawable.ic_layers)
    val List: ImageVector @Composable get() = vector(R.drawable.ic_list)
    val Filter: ImageVector @Composable get() = vector(R.drawable.ic_filter)

    /** Iconografia de ejercicio, en el mismo orden que `ExerciseIcon`. */
    val Barbell: ImageVector @Composable get() = vector(R.drawable.ic_barbell)
    val Machine: ImageVector @Composable get() = vector(R.drawable.ic_machine)
    val Cable: ImageVector @Composable get() = vector(R.drawable.ic_cable)
    val Kettlebell: ImageVector @Composable get() = vector(R.drawable.ic_kettlebell)
    val BodyWeight: ImageVector @Composable get() = vector(R.drawable.ic_bodyweight)
    val Legs: ImageVector @Composable get() = vector(R.drawable.ic_legs)
    val Core: ImageVector @Composable get() = vector(R.drawable.ic_core)
    val Cardio: ImageVector @Composable get() = vector(R.drawable.ic_cardio)
    val Stretch: ImageVector @Composable get() = vector(R.drawable.ic_stretch)

    /**
     * Traduce el icono elegido para un ejercicio a su vector.
     *
     * Vive aqui y no en el dominio porque `ExerciseIcon` no debe conocer recursos de Android.
     */
    @Composable
    fun forExercise(icon: ExerciseIcon): ImageVector = when (icon) {
        ExerciseIcon.DUMBBELL -> Dumbbell
        ExerciseIcon.BARBELL -> Barbell
        ExerciseIcon.MACHINE -> Machine
        ExerciseIcon.CABLE -> Cable
        ExerciseIcon.KETTLEBELL -> Kettlebell
        ExerciseIcon.BODYWEIGHT -> BodyWeight
        ExerciseIcon.LEGS -> Legs
        ExerciseIcon.CORE -> Core
        ExerciseIcon.CARDIO -> Cardio
        ExerciseIcon.STRETCH -> Stretch
    }

    @Composable
    private fun vector(@DrawableRes id: Int): ImageVector = ImageVector.vectorResource(id)
}
