package com.acdev.fitter.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import com.acdev.fitter.ui.theme.FitterMotion

/**
 * Hundido al pulsar. Es la respuesta tactil estandar de Fitter: todo lo que se puede tocar la
 * tiene, y nada la exagera por encima del 4 %.
 */
@Composable
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.96f
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = FitterMotion.gentleSpring(),
        label = "pressScale"
    )
    return this.scale(scale)
}
