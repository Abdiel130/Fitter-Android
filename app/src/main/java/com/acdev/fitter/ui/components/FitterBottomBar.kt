package com.acdev.fitter.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.acdev.fitter.R
import com.acdev.fitter.ui.navigation.TopLevelDestination
import com.acdev.fitter.ui.theme.FitterMotion
import com.acdev.fitter.ui.theme.FitterTheme

/**
 * Barra inferior de Fitter.
 *
 * No usa `NavigationBar` de Material porque la direccion Pulse pide una pastilla que envuelve solo
 * la pestana activa, sin etiquetas permanentes. El comportamiento accesible se conserva: cada
 * destino es un boton con descripcion y area tactil completa.
 */
@Composable
fun FitterBottomBar(
    destinations: List<TopLevelDestination>,
    selected: TopLevelDestination,
    onSelect: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = FitterTheme.spacing.sm,
                    vertical = FitterTheme.spacing.sm
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEach { destination ->
                BottomBarItem(
                    destination = destination,
                    isSelected = destination == selected,
                    onClick = { onSelect(destination) }
                )
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    destination: TopLevelDestination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val label = stringResource(destination.labelRes)
    val icon: ImageVector = ImageVector.vectorResource(destination.iconRes)

    val background by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = FitterMotion.standardTween(),
        label = "bottomBarBackground"
    )
    val tint by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = FitterMotion.standardTween(),
        label = "bottomBarTint"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(FitterTheme.radius.pill),
        color = background
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(R.string.cd_navigate_to, label),
            tint = tint,
            modifier = Modifier
                .padding(
                    horizontal = FitterTheme.spacing.lg,
                    vertical = FitterTheme.spacing.md
                )
                .size(FitterTheme.sizes.iconLarge)
        )
    }
}
