package com.acdev.fitter.ui.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import com.acdev.fitter.R
import com.acdev.fitter.domain.model.AppPreferences
import com.acdev.fitter.domain.model.FitterPalette
import com.acdev.fitter.domain.model.ThemeMode
import com.acdev.fitter.domain.model.WeightUnit
import com.acdev.fitter.ui.components.AddRowCard
import com.acdev.fitter.ui.components.ConfirmDialog
import com.acdev.fitter.ui.components.FitterCard
import com.acdev.fitter.ui.components.FitterScreen
import com.acdev.fitter.ui.components.SectionHeader
import com.acdev.fitter.ui.components.SegmentedSelector
import com.acdev.fitter.ui.components.SelectableOptionCard
import com.acdev.fitter.ui.icons.FitterIcons
import com.acdev.fitter.ui.theme.FitterTheme
import com.acdev.fitter.ui.theme.MidnightDarkScheme
import com.acdev.fitter.ui.theme.VoidDarkScheme

/**
 * Ajustes de apariencia, unidades y cuenta.
 *
 * Es la pantalla que hace conmutables las dos paletas: cambiar aqui reescribe la preferencia y el
 * tema se recompone entero, sin reiniciar la app.
 */
@Composable
fun SettingsScreen(
    preferences: AppPreferences,
    accountEmail: String,
    onPaletteChange: (FitterPalette) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onWeightUnitChange: (WeightUnit) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    FitterScreen(modifier = modifier, title = stringResource(R.string.settings_title)) {
        Column(verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)) {
            SectionHeader(title = stringResource(R.string.settings_palette))
            SelectableOptionCard(
                title = stringResource(R.string.wizard_theme_midnight),
                description = stringResource(R.string.wizard_theme_midnight_desc),
                selected = preferences.palette == FitterPalette.MIDNIGHT,
                onSelect = { onPaletteChange(FitterPalette.MIDNIGHT) },
                swatch = MidnightDarkScheme.surface
            )
            SelectableOptionCard(
                title = stringResource(R.string.wizard_theme_void),
                description = stringResource(R.string.wizard_theme_void_desc),
                selected = preferences.palette == FitterPalette.VOID,
                onSelect = { onPaletteChange(FitterPalette.VOID) },
                swatch = VoidDarkScheme.surface
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)) {
            SectionHeader(title = stringResource(R.string.settings_mode))
            SegmentedSelector(
                options = ThemeMode.entries,
                selected = preferences.themeMode,
                labelFor = { mode ->
                    when (mode) {
                        ThemeMode.SYSTEM -> stringResource(R.string.settings_mode_system)
                        ThemeMode.LIGHT -> stringResource(R.string.settings_mode_light)
                        ThemeMode.DARK -> stringResource(R.string.settings_mode_dark)
                    }
                },
                onSelect = onThemeModeChange
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)) {
            SectionHeader(title = stringResource(R.string.settings_units))
            SegmentedSelector(
                options = WeightUnit.entries,
                selected = preferences.weightUnit,
                labelFor = { unit ->
                    when (unit) {
                        WeightUnit.KG -> stringResource(R.string.unit_kg)
                        WeightUnit.LBS -> stringResource(R.string.unit_lbs)
                    }
                },
                onSelect = onWeightUnitChange
            )
        }

        AccountSection(accountEmail = accountEmail, onSignOut = onSignOut)
    }
}

/**
 * Cuenta y cierre de sesion.
 *
 * Cerrar sesion pide confirmacion y deja claro lo importante: los entrenamientos se quedan en el
 * dispositivo, porque salir no borra nada de la base local.
 */
@Composable
private fun AccountSection(accountEmail: String, onSignOut: () -> Unit) {
    var confirming by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)) {
        SectionHeader(title = stringResource(R.string.auth_account_section))

        FitterCard {
            Text(
                text = accountEmail.ifBlank { stringResource(R.string.value_empty) },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        AddRowCard(
            label = stringResource(R.string.auth_sign_out),
            onClick = { confirming = true },
            icon = FitterIcons.LogOut
        )
    }

    if (!confirming) return

    ConfirmDialog(
        title = stringResource(R.string.auth_sign_out_confirm_title),
        message = stringResource(R.string.auth_sign_out_confirm_body),
        confirmLabel = stringResource(R.string.auth_sign_out),
        onConfirm = {
            confirming = false
            onSignOut()
        },
        onDismiss = { confirming = false }
    )
}
