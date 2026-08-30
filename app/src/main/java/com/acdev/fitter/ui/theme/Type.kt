package com.acdev.fitter.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/**
 * Punto unico de cambio tipografico.
 *
 * La direccion Pulse pide Manrope (display) + Inter Tight (cuerpo). Mientras los ficheros no
 * esten en `res/font`, ambos roles caen en la familia del sistema, que en Android es Roboto y
 * soporta los mismos pesos. Para adoptarlas basta con sustituir estas dos constantes: ningun
 * composable referencia una FontFamily directamente.
 */
val FitterDisplayFamily: FontFamily = FontFamily.Default
val FitterBodyFamily: FontFamily = FontFamily.Default

private fun display(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Bold,
    letterSpacing: Double
) = TextStyle(
    fontFamily = FitterDisplayFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp
)

private fun body(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Normal,
    letterSpacing: Double = 0.0
) = TextStyle(
    fontFamily = FitterBodyFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp
)

val FitterTypography = Typography(
    displayLarge = display(44, 46, letterSpacing = -1.4),
    displayMedium = display(36, 40, letterSpacing = -1.1),
    displaySmall = display(30, 34, letterSpacing = -0.9),
    headlineLarge = display(28, 32, letterSpacing = -0.8),
    headlineMedium = display(24, 28, letterSpacing = -0.6),
    headlineSmall = display(20, 25, letterSpacing = -0.4),
    titleLarge = display(18, 23, letterSpacing = -0.3),
    titleMedium = body(16, 21, FontWeight.SemiBold, -0.1),
    titleSmall = body(14, 19, FontWeight.SemiBold),
    bodyLarge = body(16, 24),
    bodyMedium = body(14, 21),
    bodySmall = body(12, 17),
    labelLarge = body(14, 18, FontWeight.SemiBold),
    labelMedium = body(12, 16, FontWeight.SemiBold, 0.2),
    labelSmall = body(10, 14, FontWeight.Bold, 1.0)
)

/** Estilos propios de Fitter que no encajan en la escala de Material. */
object FitterTextStyles {

    /** Cifras grandes de la isla y de los contadores: siempre tabulares. */
    val Metric: TextStyle = display(28, 30, letterSpacing = -0.8).copy(
        textAlign = TextAlign.Start
    )

    /** Etiqueta corta en mayusculas sobre una cifra. */
    val MetricLabel: TextStyle = body(10, 14, FontWeight.Bold, 1.0)
}
