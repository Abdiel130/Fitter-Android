package com.acdev.fitter.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.acdev.fitter.domain.model.FitterPalette
import com.acdev.fitter.domain.model.ThemeMode

private val LocalFitterExtendedColors = staticCompositionLocalOf<FitterExtendedColors> {
    error("FitterExtendedColors no disponible: envuelve la vista en FitterTheme.")
}

/**
 * Acceso a los tokens del tema desde cualquier composable.
 *
 * `MaterialTheme` sigue siendo la fuente de los roles estandar (primary, surface, ...);
 * `FitterTheme` anade lo que Material no cubre y los tokens de espaciado y movimiento.
 */
object FitterTheme {

    val colors: FitterExtendedColors
        @Composable @ReadOnlyComposable get() = LocalFitterExtendedColors.current

    val spacing: FitterSpacing get() = FitterSpacing

    val radius: FitterRadius get() = FitterRadius

    val sizes: FitterSizes get() = FitterSizes

    val motion: FitterMotion get() = FitterMotion
}

private data class ThemeColors(
    val scheme: ColorScheme,
    val extended: FitterExtendedColors
)

private fun resolveColors(palette: FitterPalette, dark: Boolean): ThemeColors = when {
    palette == FitterPalette.MIDNIGHT && dark -> ThemeColors(MidnightDarkScheme, MidnightDarkExtended)
    palette == FitterPalette.MIDNIGHT -> ThemeColors(MidnightLightScheme, MidnightLightExtended)
    dark -> ThemeColors(VoidDarkScheme, VoidDarkExtended)
    else -> ThemeColors(VoidLightScheme, VoidLightExtended)
}

@Composable
private fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.DARK -> true
    ThemeMode.LIGHT -> false
}

/**
 * Tema raiz de la app. Todo composable de Fitter vive dentro de este.
 *
 * @param palette paleta elegida por el usuario en el asistente o en Ajustes.
 * @param themeMode como resolver claro/oscuro.
 */
@Composable
fun FitterTheme(
    palette: FitterPalette = FitterPalette.MIDNIGHT,
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val dark = themeMode.isDark()
    val colors = resolveColors(palette, dark)
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }

    CompositionLocalProvider(LocalFitterExtendedColors provides colors.extended) {
        MaterialTheme(
            colorScheme = colors.scheme,
            typography = FitterTypography,
            shapes = FitterShapes,
            content = content
        )
    }
}
