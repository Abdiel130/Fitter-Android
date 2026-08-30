package com.acdev.fitter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.acdev.fitter.ui.theme.FitterTheme

/**
 * Dato compacto con icono. Se usa para las metricas de un vistazo del dashboard y sirve igual
 * para cualquier pantalla que necesite una fila de cifras.
 *
 * Si recibe `onClick`, se comporta como accion rapida: es asi como el chip de agua suma un vaso
 * sin salir del inicio.
 */
@Composable
fun MetricChip(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(FitterTheme.radius.pill)
    val border = BorderStroke(FitterTheme.sizes.hairline, FitterTheme.colors.hairline)

    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(
                horizontal = FitterTheme.spacing.md,
                vertical = FitterTheme.spacing.sm
            ),
            horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = accent,
                modifier = Modifier.size(FitterTheme.sizes.iconSmall)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    if (onClick == null) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = border,
            content = content
        )
    } else {
        Surface(
            onClick = onClick,
            modifier = modifier.pressScale(interactionSource),
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = border,
            interactionSource = interactionSource,
            content = content
        )
    }
}
