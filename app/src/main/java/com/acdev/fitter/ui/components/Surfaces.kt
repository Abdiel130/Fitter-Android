package com.acdev.fitter.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import com.acdev.fitter.ui.theme.FitterMotion
import com.acdev.fitter.ui.theme.FitterTheme

/**
 * Tarjeta base de Fitter. Toda superficie elevada de la app sale de aqui, con o sin pulsacion,
 * para que el radio, el borde y el relleno no se reinventen en cada pantalla.
 */
@Composable
fun FitterCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
    shape: Shape = RoundedCornerShape(FitterTheme.radius.xl),
    contentPadding: PaddingValues = PaddingValues(
        horizontal = FitterTheme.spacing.lg,
        vertical = FitterTheme.spacing.lg
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    // El borde de acento es como se senala lo elegido en toda la app: rutina activa, opcion marcada.
    val borderColor by animateColorAsState(
        targetValue = when {
            selected -> MaterialTheme.colorScheme.primary
            else -> FitterTheme.colors.hairline
        },
        animationSpec = FitterMotion.standardTween(),
        label = "cardBorder"
    )
    val border = BorderStroke(FitterTheme.sizes.hairline, borderColor)

    val surfaceModifier = when (onClick) {
        null -> modifier
        else -> modifier.pressScale(interactionSource)
    }

    if (onClick == null) {
        Surface(
            modifier = surfaceModifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = border
        ) {
            Column(Modifier.padding(contentPadding), content = content)
        }
    } else {
        Surface(
            onClick = onClick,
            modifier = surfaceModifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = border,
            interactionSource = interactionSource
        ) {
            Column(Modifier.padding(contentPadding), content = content)
        }
    }
}

/**
 * Encabezado de seccion: rotulo corto en mayusculas y, opcionalmente, una accion a la derecha.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        trailing?.invoke()
    }
}
