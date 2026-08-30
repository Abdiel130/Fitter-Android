package com.acdev.fitter.ui.icons

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.acdev.fitter.R

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

    @Composable
    private fun vector(@DrawableRes id: Int): ImageVector = ImageVector.vectorResource(id)
}
