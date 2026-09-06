package com.acdev.fitter.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Escala de espaciado. Ningun composable escribe un `dp` suelto para margenes o separaciones:
 * se toma de aqui para que el ritmo vertical sea el mismo en toda la app.
 */
object FitterSpacing {
    val none: Dp = 0.dp
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val xxxl: Dp = 32.dp
    val huge: Dp = 48.dp

    /** Margen horizontal de pantalla. Todas las pantallas usan el mismo. */
    val screenHorizontal: Dp = 20.dp

    /** Separacion vertical entre bloques de una pantalla. */
    val sectionGap: Dp = 16.dp
}

/** Radios de esquina. Pulse es una direccion de formas blandas: nada por debajo de 12 dp. */
object FitterRadius {
    val sm: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 20.dp
    val xl: Dp = 24.dp
    val pill: Dp = 999.dp
}

/** Tamanos recurrentes de elementos interactivos y graficos. */
object FitterSizes {
    val minTouchTarget: Dp = 48.dp
    val iconSmall: Dp = 16.dp
    val iconMedium: Dp = 20.dp
    val iconLarge: Dp = 24.dp
    val actionButton: Dp = 44.dp
    val ringSmall: Dp = 46.dp
    val ringMedium: Dp = 56.dp
    val hairline: Dp = 1.dp

    /** Ancho reservado al valor de un `NumberStepper`, para que no salte al cambiar de cifra. */
    val stepperValueWidth: Dp = 44.dp

    /** Circulo con el numero de posicion en una secuencia. */
    val orderBadge: Dp = 26.dp

    /** Cuadro del icono de un ejercicio en listas y selectores. */
    val exerciseIconTile: Dp = 40.dp
    val islandCollapsedHeight: Dp = 34.dp
    val islandExpandedHeight: Dp = 96.dp
    val islandCollapsedWidth: Dp = 116.dp
    val islandStageHeight: Dp = 52.dp
}

val FitterShapes = Shapes(
    extraSmall = RoundedCornerShape(FitterRadius.sm),
    small = RoundedCornerShape(FitterRadius.md),
    medium = RoundedCornerShape(FitterRadius.lg),
    large = RoundedCornerShape(FitterRadius.xl),
    extraLarge = RoundedCornerShape(FitterRadius.xl)
)
