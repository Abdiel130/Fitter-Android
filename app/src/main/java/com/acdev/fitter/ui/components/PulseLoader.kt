package com.acdev.fitter.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.acdev.fitter.ui.theme.FitterTheme

private const val DOT_COUNT = 3
private const val CYCLE_MILLIS = 1600
private const val STAGGER_MILLIS = 130

/**
 * Indicador de carga de Fitter: tres puntos que se estiran hasta capsula y vuelven.
 *
 * Es la isla en miniatura, de modo que la espera usa el mismo gesto que el resto de la app. Se usa
 * en lugar de `CircularProgressIndicator` en cualquier estado de carga.
 */
@Composable
fun PulseLoader(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    dotSize: Dp = 10.dp,
    expandedWidth: Dp = 28.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val transition = rememberInfiniteTransition(label = "pulseLoader")

    Row(
        modifier = modifier.semantics {
            contentDescription?.let { this.contentDescription = it }
        },
        horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(DOT_COUNT) { index ->
            val fraction by transition.animateFloat(
                initialValue = 0f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = CYCLE_MILLIS
                        0f at 0 using LinearEasing
                        1f at CYCLE_MILLIS * 45 / 100
                        0f at CYCLE_MILLIS
                    },
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = androidx.compose.animation.core.StartOffset(
                        offsetMillis = index * STAGGER_MILLIS
                    )
                ),
                label = "dot$index"
            )

            Box(
                Modifier
                    .width(dotSize + (expandedWidth - dotSize) * fraction)
                    .height(dotSize)
                    .background(
                        color = color.copy(alpha = 0.45f + 0.55f * fraction),
                        shape = RoundedCornerShape(FitterTheme.radius.pill)
                    )
            )
        }
    }
}
