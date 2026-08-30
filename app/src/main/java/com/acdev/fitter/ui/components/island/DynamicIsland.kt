package com.acdev.fitter.ui.components.island

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.acdev.fitter.R
import com.acdev.fitter.domain.model.SyncStatus
import com.acdev.fitter.ui.theme.FitterMotion
import com.acdev.fitter.ui.theme.FitterTextStyles
import com.acdev.fitter.ui.theme.FitterTheme
import com.acdev.fitter.ui.util.Format

/**
 * Isla dinamica: la firma de la direccion Pulse.
 *
 * Una capsula permanente en la cabecera que absorbe todo lo que ocurre en segundo plano. Crece,
 * cambia de contenido y vuelve a su tamano minimo. Es tambien el unico indicador de
 * sincronizacion de la app: no hay iconos de nube repartidos por las pantallas.
 *
 * Se coloca en el `Scaffold` raiz, de modo que cualquier pantalla la hereda sin declararla.
 */
@Composable
fun DynamicIsland(
    state: IslandState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onSkipRest: (() -> Unit)? = null
) {
    val expanded = state is IslandState.Rest || state is IslandState.Record
    val cornerRadius by animateDpAsState(
        targetValue = if (expanded) 26.dp else 18.dp,
        animationSpec = FitterMotion.standardTween(),
        label = "islandCorner"
    )
    val borderColor = FitterTheme.colors.islandBorder
    val description = stringResource(R.string.island_cd)

    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        Surface(
            color = FitterTheme.colors.islandBackground,
            contentColor = FitterTheme.colors.islandContent,
            shape = RoundedCornerShape(cornerRadius),
            border = if (borderColor.alpha > 0f) {
                BorderStroke(FitterTheme.sizes.hairline, borderColor)
            } else {
                null
            },
            modifier = Modifier
                .heightIn(min = FitterTheme.sizes.islandCollapsedHeight)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .semantics { contentDescription = description }
        ) {
            AnimatedContent(
                targetState = state,
                contentKey = { it.kind },
                transitionSpec = {
                    val enter = fadeIn(tween(FitterMotion.Duration.STANDARD, delayMillis = 60)) +
                        scaleIn(
                            initialScale = 0.92f,
                            animationSpec = tween(FitterMotion.Duration.STANDARD, delayMillis = 60)
                        )
                    val exit = fadeOut(tween(FitterMotion.Duration.INSTANT)) +
                        scaleOut(targetScale = 0.94f, animationSpec = tween(FitterMotion.Duration.INSTANT))
                    enter togetherWith exit using SizeTransform(clip = false) { _, _ ->
                        FitterMotion.islandSpring()
                    }
                },
                label = "islandContent"
            ) { target ->
                when (target) {
                    is IslandState.Idle -> IdleContent(target.sync)
                    is IslandState.Syncing -> SyncingContent(target.pendingCount)
                    is IslandState.Rest -> RestContent(target, onSkipRest)
                    is IslandState.Record -> RecordContent(target)
                }
            }
        }
    }
}

@Composable
private fun IdleContent(sync: SyncStatus) {
    val colors = FitterTheme.colors
    val dotColor = when {
        !sync.isOnline -> colors.syncOffline
        sync.failedCount > 0 || sync.conflictCount > 0 -> colors.syncError
        sync.pendingCount > 0 -> colors.syncPending
        else -> colors.syncSynced
    }
    val label = when {
        !sync.isOnline -> stringResource(R.string.island_sync_offline)
        sync.failedCount > 0 || sync.conflictCount > 0 -> stringResource(R.string.island_sync_error)
        sync.pendingCount > 0 -> stringResource(R.string.island_sync_pending, sync.pendingCount)
        else -> stringResource(R.string.island_idle_label)
    }

    CollapsedRow {
        PulsingDot(color = dotColor, animate = sync.pendingCount > 0 || sync.isSyncing)
        IslandLabel(label)
    }
}

@Composable
private fun SyncingContent(pendingCount: Int) {
    CollapsedRow {
        PulsingDot(color = FitterTheme.colors.syncPending, animate = true)
        IslandLabel(stringResource(R.string.island_sync_uploading, pendingCount))
    }
}

@Composable
private fun RestContent(state: IslandState.Rest, onSkipRest: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .defaultMinSize(minWidth = 224.dp)
            .padding(horizontal = FitterTheme.spacing.md, vertical = FitterTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RestRing(progress = state.progress)
        Column(Modifier.weight(1f)) {
            Text(
                text = Format.duration(state.remainingSeconds),
                style = FitterTextStyles.Metric,
                color = FitterTheme.colors.islandContent
            )
            Text(
                text = stringResource(R.string.island_rest_context, state.exerciseName),
                style = MaterialTheme.typography.bodySmall,
                color = FitterTheme.colors.islandContentMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (onSkipRest != null) {
            Surface(
                onClick = onSkipRest,
                shape = RoundedCornerShape(FitterTheme.radius.pill),
                color = FitterTheme.colors.islandButton,
                contentColor = FitterTheme.colors.islandContent
            ) {
                Text(
                    text = stringResource(R.string.action_skip_rest),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(
                        horizontal = FitterTheme.spacing.md,
                        vertical = FitterTheme.spacing.sm
                    )
                )
            }
        }
    }
}

@Composable
private fun RecordContent(state: IslandState.Record) {
    Row(
        modifier = Modifier
            .defaultMinSize(minWidth = 236.dp)
            .padding(horizontal = FitterTheme.spacing.lg, vertical = FitterTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(FitterTheme.sizes.iconLarge)
                .background(Color.Transparent)
        ) {
            TrophyGlyph()
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.island_record_title, state.weightLabel, state.reps),
                style = MaterialTheme.typography.titleLarge,
                color = FitterTheme.colors.islandContent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.island_record_context, state.exerciseName) +
                    " · " + state.deltaLabel,
                style = MaterialTheme.typography.bodySmall,
                color = FitterTheme.colors.islandContentMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CollapsedRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .defaultMinSize(minWidth = FitterTheme.sizes.islandCollapsedWidth)
            .padding(horizontal = FitterTheme.spacing.md, vertical = FitterTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

@Composable
private fun IslandLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = FitterTheme.colors.islandContentMuted,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/** Punto de estado. Solo late cuando hay algo vivo: en reposo permanece quieto. */
@Composable
private fun PulsingDot(color: Color, animate: Boolean) {
    val transition = rememberInfiniteTransition(label = "islandDot")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(FitterMotion.Duration.AMBIENT, easing = FitterMotion.Easings.standard),
            repeatMode = RepeatMode.Reverse
        ),
        label = "islandDotAlpha"
    )

    Box(
        Modifier
            .size(9.dp)
            .alpha(if (animate) alpha else 1f)
            .background(color = color, shape = RoundedCornerShape(FitterTheme.radius.pill))
    )
}

/** Anillo del cronometro de descanso: se vacia en sentido horario. */
@Composable
private fun RestRing(progress: Float) {
    val trackColor = FitterTheme.colors.islandTrack
    val arcColor = MaterialTheme.colorScheme.primary

    Canvas(Modifier.size(FitterTheme.sizes.ringMedium)) {
        val strokeWidth = 5.dp.toPx()
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        val inset = strokeWidth / 2f
        val diameter = size.minDimension - strokeWidth

        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = Size(diameter, diameter),
            style = stroke
        )
        drawArc(
            color = arcColor,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = Size(diameter, diameter),
            style = stroke
        )
    }
}

/** Trofeo dibujado con el color de record del tema. */
@Composable
private fun TrophyGlyph() {
    val recordColor = FitterTheme.colors.record
    Canvas(Modifier.size(FitterTheme.sizes.iconLarge)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val inset = size.minDimension * 0.18f
        drawArc(
            color = recordColor,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(inset, inset * 0.5f),
            size = Size(size.width - inset * 2, size.height - inset * 1.6f),
            style = stroke
        )
        drawLine(
            color = recordColor,
            start = Offset(size.width / 2f, size.height * 0.72f),
            end = Offset(size.width / 2f, size.height * 0.9f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = recordColor,
            start = Offset(size.width * 0.3f, size.height * 0.92f),
            end = Offset(size.width * 0.7f, size.height * 0.92f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
