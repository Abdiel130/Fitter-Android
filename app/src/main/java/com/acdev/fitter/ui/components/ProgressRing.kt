package com.acdev.fitter.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.acdev.fitter.ui.theme.FitterMotion
import com.acdev.fitter.ui.theme.FitterTheme

/**
 * Anillo de progreso de Fitter.
 *
 * Se dibuja en Canvas y no con `CircularProgressIndicator` porque necesita grosor, remate y pista
 * propios, y porque asi el mismo componente sirve para calorias, proteina o series.
 *
 * El progreso se anima siempre: un anillo que salta de golpe se lee como un error de pintado.
 */
@Composable
fun ProgressRing(
    progress: Float,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = FitterTheme.sizes.ringSmall,
    strokeWidth: Dp = 5.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val trackColor = FitterTheme.colors.ringTrack
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = FitterMotion.standardTween(FitterMotion.Duration.EMPHASIZED),
        label = "ringProgress"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)
    ) {
        Canvas(Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val inset = strokeWidth.toPx() / 2f
            val diameter = this.size.minDimension - strokeWidth.toPx()

            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(diameter, diameter),
                style = stroke
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(diameter, diameter),
                style = stroke
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
