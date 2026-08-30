package com.acdev.fitter.ui.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.acdev.fitter.R
import com.acdev.fitter.domain.model.AppPreferences
import com.acdev.fitter.domain.model.FitterPalette
import com.acdev.fitter.domain.model.ThemeMode
import com.acdev.fitter.domain.model.WeightUnit
import com.acdev.fitter.ui.components.FitterScreen
import com.acdev.fitter.ui.components.SectionHeader
import com.acdev.fitter.ui.components.SegmentedSelector
import com.acdev.fitter.ui.components.SelectableOptionCard
import com.acdev.fitter.ui.theme.FitterTheme
import com.acdev.fitter.ui.theme.MidnightDarkScheme
import com.acdev.fitter.ui.theme.VoidDarkScheme

/**
 * Ajustes de apariencia y unidades.
 *
 * Es la pantalla que hace conmutables las dos paletas: cambiar aqui reescribe la preferencia y el
 * tema se recompone entero, sin reiniciar la app.
 */
@Composable
fun SettingsScreen(
    preferences: AppPreferences,
    onPaletteChange: (FitterPalette) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onWeightUnitChange: (WeightUnit) -> Unit,
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
    }
}
