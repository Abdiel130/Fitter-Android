package com.acdev.fitter.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.acdev.fitter.R
import com.acdev.fitter.ui.icons.FitterIcons
import com.acdev.fitter.ui.theme.FitterMotion
import com.acdev.fitter.ui.theme.FitterTheme

/**
 * Boton de icono de Fitter.
 *
 * Existe porque los editores estan llenos de acciones pequenas (subir, bajar, quitar) y todas
 * deben respetar el area tactil minima aunque el icono sea de 20 dp.
 */
@Composable
fun FitterIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val interactionSource = remember { MutableInteractionSource() }
    val resolvedTint = when {
        enabled -> tint
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DISABLED_ALPHA)
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .size(FitterTheme.sizes.minTouchTarget)
            .pressScale(interactionSource),
        enabled = enabled,
        shape = RoundedCornerShape(FitterTheme.radius.pill),
        color = Color.Transparent,
        interactionSource = interactionSource
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = resolvedTint,
                modifier = Modifier.size(FitterTheme.sizes.iconMedium)
            )
        }
    }
}

/**
 * Ajuste de un numero con dos toques.
 *
 * Se usa en lugar de un campo de texto porque configurar series es repetir microajustes: subir
 * una repeticion no deberia abrir el teclado.
 */
@Composable
fun NumberStepper(
    label: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    canDecrease: Boolean = true,
    canIncrease: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            FitterIconButton(
                icon = FitterIcons.ArrowDown,
                contentDescription = stringResource(R.string.cd_decrease, label),
                onClick = onDecrease,
                enabled = canDecrease
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .sizeIn(minWidth = FitterTheme.sizes.stepperValueWidth)
                    .padding(horizontal = FitterTheme.spacing.xs),
                maxLines = 1
            )
            FitterIconButton(
                icon = FitterIcons.ArrowUp,
                contentDescription = stringResource(R.string.cd_increase, label),
                onClick = onIncrease,
                enabled = canIncrease
            )
        }
    }
}

/** Etiqueta conmutable. Filtra el catalogo y clasifica un ejercicio con el mismo control. */
@Composable
fun FitterFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background by animateColorAsState(
        targetValue = when {
            selected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = FitterMotion.standardTween(),
        label = "chipBackground"
    )
    val textColor by animateColorAsState(
        targetValue = when {
            selected -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = FitterMotion.standardTween(),
        label = "chipText"
    )

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(FitterTheme.radius.pill),
        color = background,
        border = BorderStroke(FitterTheme.sizes.hairline, FitterTheme.colors.hairline)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(
                horizontal = FitterTheme.spacing.md,
                vertical = FitterTheme.spacing.sm
            )
        )
    }
}

/**
 * Fila de alta.
 *
 * Es la unica forma de crear cosas en los editores: siempre al final de la lista, con el mismo
 * gesto, de modo que anadir una rutina, una lista o un ejercicio se aprende una sola vez.
 */
@Composable
fun AddRowCard(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interactionSource),
        shape = RoundedCornerShape(FitterTheme.radius.xl),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(FitterTheme.sizes.hairline, MaterialTheme.colorScheme.primary),
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.padding(FitterTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon ?: FitterIcons.Plus,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(FitterTheme.sizes.iconMedium)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/** Hueco explicado: dice que falta y ofrece la accion que lo llena. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    FitterCard(modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(FitterTheme.radius.md),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(FitterTheme.spacing.md)
                        .size(FitterTheme.sizes.iconLarge)
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = FitterTheme.spacing.xxs)
                )
            }
        }
    }
}

/** Confirmacion de un borrado. Todo lo irreversible de los editores pasa por aqui. */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(FitterTheme.radius.xl),
        title = {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

/** Numero de posicion dentro de una secuencia: orden de la rutina y de las series. */
@Composable
fun OrderBadge(
    position: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier.size(FitterTheme.sizes.orderBadge),
        shape = RoundedCornerShape(FitterTheme.radius.pill),
        color = color.copy(alpha = BADGE_ALPHA)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = position.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

/** Cabecera de una pantalla de detalle: volver, titulo y las acciones de la pantalla. */
@Composable
fun DetailHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: RowScopeContent = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FitterIconButton(
            icon = FitterIcons.ArrowBack,
            contentDescription = stringResource(R.string.cd_go_back),
            onClick = onBack,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        trailing()
    }
}

/** Contenido suelto de la cabecera. Evita exponer `RowScope` a las pantallas. */
typealias RowScopeContent = @Composable () -> Unit

private const val DISABLED_ALPHA = 0.35f
private const val BADGE_ALPHA = 0.16f
