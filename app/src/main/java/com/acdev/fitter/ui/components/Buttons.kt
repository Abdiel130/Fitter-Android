package com.acdev.fitter.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.acdev.fitter.ui.theme.FitterTheme

/**
 * Accion principal. Una por pantalla como maximo: si hay dos igual de fuertes, es que la pantalla
 * esta pidiendo dos decisiones distintas.
 */
@Composable
fun FitterPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }

    Button(
        onClick = onClick,
        modifier = modifier
            .height(FitterTheme.sizes.minTouchTarget)
            .pressScale(interactionSource),
        enabled = enabled,
        shape = RoundedCornerShape(FitterTheme.radius.pill),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = PaddingValues(horizontal = FitterTheme.spacing.xxl),
        interactionSource = interactionSource
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Accion secundaria, siempre subordinada a la principal. */
@Composable
fun FitterTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(FitterTheme.sizes.minTouchTarget),
        enabled = enabled,
        shape = RoundedCornerShape(FitterTheme.radius.pill)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
