package com.acdev.fitter.ui.feature.login

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.acdev.fitter.ui.theme.FitterMotion
import kotlin.math.cos
import kotlin.math.sin

private const val FULL_TURN_RADIANS = 6.2831855f
private const val PRIMARY_ALPHA = 0.30f
private const val TERTIARY_ALPHA = 0.20f
private const val PRIMARY_RADIUS_RATIO = 0.85f
private const val TERTIARY_RADIUS_RATIO = 0.70f
private const val ORBIT_RATIO = 0.16f
private const val ORBIT_PERIODS = 3

/**
 * Fondo vivo de la pantalla de acceso.
 *
 * Dos halos de color del tema que orbitan despacio con la duracion ambiental del sistema de
 * movimiento. Los colores salen del esquema con transparencia, asi que la paleta y el modo claro u
 * oscuro se respetan sin declarar un color nuevo.
 */
@Composable
fun AuroraBackdrop(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    val transition = rememberInfiniteTransition(label = "aurora")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = FULL_TURN_RADIANS,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = FitterMotion.Duration.AMBIENT * ORBIT_PERIODS,
                easing = FitterMotion.Easings.standard
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "auroraPhase"
    )

    Canvas(modifier = modifier) {
        val orbit = size.minDimension * ORBIT_RATIO
        val center = Offset(size.width / 2f, size.height / 3f)

        drawHalo(
            color = primary,
            alpha = PRIMARY_ALPHA,
            center = center + Offset(cos(phase) * orbit, sin(phase) * orbit),
            radius = size.minDimension * PRIMARY_RADIUS_RATIO
        )
        drawHalo(
            color = tertiary,
            alpha = TERTIARY_ALPHA,
            center = center - Offset(cos(phase) * orbit, sin(phase) * orbit),
            radius = size.minDimension * TERTIARY_RADIUS_RATIO
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHalo(
    color: Color,
    alpha: Float,
    center: Offset,
    radius: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = alpha), Color.Transparent),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}
