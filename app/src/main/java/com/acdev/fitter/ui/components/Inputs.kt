package com.acdev.fitter.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import com.acdev.fitter.ui.icons.FitterIcons
import com.acdev.fitter.ui.theme.FitterMotion
import com.acdev.fitter.ui.theme.FitterTheme

/**
 * Campo de texto de Fitter. Envuelve el de Material para fijar forma, colores y teclado en un
 * unico sitio: ninguna pantalla configura un `OutlinedTextField` por su cuenta.
 */
@Composable
fun FitterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, style = MaterialTheme.typography.bodyMedium) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        shape = RoundedCornerShape(FitterTheme.radius.md),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = FitterTheme.colors.hairline,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Opcion seleccionable en formato tarjeta, con titulo, descripcion y marca de seleccion.
 *
 * Es el control con el que el asistente pregunta tema y unidades, y sirve igual para cualquier
 * ajuste de una sola eleccion.
 */
@Composable
fun SelectableOptionCard(
    title: String,
    description: String?,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    swatch: Color? = null
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            FitterTheme.colors.hairline
        },
        animationSpec = FitterMotion.standardTween(),
        label = "optionBorder"
    )

    Surface(
        onClick = onSelect,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(FitterTheme.radius.lg),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(FitterTheme.sizes.hairline, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(FitterTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (swatch != null) {
                Surface(
                    shape = RoundedCornerShape(FitterTheme.radius.sm),
                    color = swatch,
                    modifier = Modifier.size(FitterTheme.sizes.actionButton)
                ) {}
            }

            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (selected) {
                Icon(
                    imageVector = FitterIcons.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(FitterTheme.sizes.iconMedium)
                )
            }
        }
    }
}

/**
 * Selector segmentado para elecciones cortas y excluyentes, como el modo claro/oscuro.
 */
@Composable
fun <T> SegmentedSelector(
    options: List<T>,
    selected: T,
    labelFor: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(FitterTheme.radius.pill),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(FitterTheme.sizes.hairline, FitterTheme.colors.hairline)
    ) {
        Row(
            modifier = Modifier.padding(FitterTheme.spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.xs)
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                val background by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    },
                    animationSpec = FitterMotion.standardTween(),
                    label = "segmentBackground"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    animationSpec = FitterMotion.standardTween(),
                    label = "segmentText"
                )

                Surface(
                    onClick = { onSelect(option) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(FitterTheme.radius.pill),
                    color = background
                ) {
                    Text(
                        text = labelFor(option),
                        style = MaterialTheme.typography.labelLarge,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(
                            horizontal = FitterTheme.spacing.md,
                            vertical = FitterTheme.spacing.md
                        )
                    )
                }
            }
        }
    }
}
