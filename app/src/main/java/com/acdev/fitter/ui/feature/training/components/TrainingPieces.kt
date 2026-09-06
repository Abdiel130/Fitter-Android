package com.acdev.fitter.ui.feature.training.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.acdev.fitter.R
import com.acdev.fitter.domain.model.ExerciseIcon
import com.acdev.fitter.domain.model.SetType
import com.acdev.fitter.ui.icons.FitterIcons
import com.acdev.fitter.ui.theme.FitterTheme

/** Cuadro con el icono de un ejercicio. Es la unica imagen que acompana a un ejercicio. */
@Composable
fun ExerciseIconTile(
    icon: ExerciseIcon,
    modifier: Modifier = Modifier,
    accent: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier.size(FitterTheme.sizes.exerciseIconTile),
        shape = RoundedCornerShape(FitterTheme.radius.sm),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = FitterIcons.forExercise(icon),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(FitterTheme.sizes.iconMedium)
            )
        }
    }
}

/**
 * Fila de ejercicio: icono, nombre y una linea de contexto.
 *
 * La usan el catalogo, el selector y el editor de listas, para que un ejercicio se reconozca
 * igual en los tres sitios.
 */
@Composable
fun ExerciseRow(
    icon: ExerciseIcon,
    name: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading?.invoke()
        ExerciseIconTile(icon)
        Column(Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        trailing?.invoke()
    }
}

/** Distintivo corto, para marcar lo propio del usuario o el estado de una rutina. */
@Composable
fun TagBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(FitterTheme.radius.pill),
        color = color.copy(alpha = BADGE_ALPHA)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(
                horizontal = FitterTheme.spacing.sm,
                vertical = FitterTheme.spacing.xxs
            )
        )
    }
}

/** Nombre visible de un tipo de serie. Vive aqui para no repetir el `when` en cada pantalla. */
@Composable
fun setTypeLabel(type: SetType): String = when (type) {
    SetType.NORMAL -> stringResource(R.string.set_type_normal)
    SetType.WARMUP -> stringResource(R.string.set_type_warmup)
    SetType.DROPSET -> stringResource(R.string.set_type_dropset)
    SetType.FAILURE -> stringResource(R.string.set_type_failure)
}

private const val BADGE_ALPHA = 0.16f
