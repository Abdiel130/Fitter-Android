package com.acdev.fitter.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.acdev.fitter.ui.theme.FitterTheme

/**
 * Armazon de pantalla.
 *
 * Fija el margen lateral, el ritmo vertical y el desplazamiento, de modo que montar una pantalla
 * nueva sea escribir su contenido y nada mas. La cabecera es opcional: el dashboard trae la suya.
 */
@Composable
fun FitterScreen(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    scrollable: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(bottom = FitterTheme.spacing.xxxl),
    verticalSpacing: androidx.compose.ui.unit.Dp = FitterTheme.spacing.sectionGap,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollModifier = when {
        scrollable -> Modifier.verticalScroll(rememberScrollState())
        else -> Modifier
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .then(scrollModifier)
            .padding(horizontal = FitterTheme.spacing.screenHorizontal)
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        if (title != null) {
            Column(verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.xs)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        content()
    }
}

/**
 * Pantalla aun no construida.
 *
 * Existe para que la navegacion sea real desde el primer dia: la pestana responde, mantiene el
 * tema y explica que va a vivir ahi. Al implementar la pantalla de verdad, se sustituye la
 * llamada y se borra nada mas.
 */
@Composable
fun PlaceholderScreen(
    title: String,
    message: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    FitterScreen(modifier = modifier, title = title) {
        FitterCard(modifier = Modifier.fillMaxWidth()) {
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
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}
