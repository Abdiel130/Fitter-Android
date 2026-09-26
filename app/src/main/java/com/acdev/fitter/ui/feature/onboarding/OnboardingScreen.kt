package com.acdev.fitter.ui.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import com.acdev.fitter.R
import com.acdev.fitter.domain.model.FitterPalette
import com.acdev.fitter.domain.model.ThemeMode
import com.acdev.fitter.domain.model.WeightUnit
import com.acdev.fitter.ui.components.FitterCard
import com.acdev.fitter.ui.components.FitterPrimaryButton
import com.acdev.fitter.ui.components.FitterTextButton
import com.acdev.fitter.ui.components.FitterTextField
import com.acdev.fitter.ui.components.SegmentedSelector
import com.acdev.fitter.ui.components.SelectableOptionCard
import com.acdev.fitter.ui.icons.FitterIcons
import com.acdev.fitter.ui.theme.FitterMotion
import com.acdev.fitter.ui.theme.FitterTheme
import com.acdev.fitter.ui.theme.MidnightDarkScheme
import com.acdev.fitter.ui.theme.VoidDarkScheme

/**
 * Asistente de primer arranque.
 *
 * Cinco pasos, todos omitibles salvo la eleccion de unidad, y ninguno pide permisos. Se muestra
 * una sola vez: al terminar queda marcado en preferencias.
 *
 * El ultimo paso es un resumen de la cuenta con la que se inicio sesion, no un formulario: la
 * identidad la fija el servidor.
 */
@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    actions: OnboardingActions,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = FitterTheme.spacing.screenHorizontal)
    ) {
        StepIndicator(state)

        AnimatedContent(
            targetState = state.step,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                val offset = if (forward) 1 else -1
                (
                    slideInHorizontally(
                        animationSpec = tween(
                            FitterMotion.Duration.EMPHASIZED,
                            easing = FitterMotion.Easings.emphasized
                        ),
                        initialOffsetX = { it / 6 * offset }
                    ) + fadeIn(tween(FitterMotion.Duration.STANDARD))
                    ) togetherWith (
                    slideOutHorizontally(
                        animationSpec = tween(
                            FitterMotion.Duration.EMPHASIZED,
                            easing = FitterMotion.Easings.emphasized
                        ),
                        targetOffsetX = { -it / 6 * offset }
                    ) + fadeOut(tween(FitterMotion.Duration.QUICK))
                    )
            },
            label = "onboardingStep"
        ) { step ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.lg)
            ) {
                when (step) {
                    OnboardingStep.WELCOME -> WelcomeStep()
                    OnboardingStep.THEME -> ThemeStep(state, actions)
                    OnboardingStep.UNITS -> UnitsStep(state, actions)
                    OnboardingStep.FIRST_RECORD -> FirstRecordStep(state, actions)
                    OnboardingStep.ACCOUNT -> AccountStep(state)
                }
            }
        }

        NavigationRow(state, actions)
    }
}

/** Acciones del asistente, agrupadas para que la pantalla no dependa del ViewModel. */
data class OnboardingActions(
    val onNext: () -> Unit,
    val onBack: () -> Unit,
    val onFinish: () -> Unit,
    val onPaletteChange: (FitterPalette) -> Unit,
    val onThemeModeChange: (ThemeMode) -> Unit,
    val onWeightUnitChange: (WeightUnit) -> Unit,
    val onWeightChange: (String) -> Unit,
    val onWaistChange: (String) -> Unit,
    val onChestChange: (String) -> Unit,
)

@Composable
private fun StepIndicator(state: OnboardingUiState) {
    Text(
        text = stringResource(R.string.wizard_step_of, state.stepNumber, state.stepCount),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = FitterTheme.spacing.xl, bottom = FitterTheme.spacing.lg)
    )
}

@Composable
private fun ColumnScope.StepHeader(title: String, body: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.onSurface
    )
    Text(
        text = body,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ColumnScope.WelcomeStep() {
    StepHeader(
        title = stringResource(R.string.wizard_welcome_title),
        body = stringResource(R.string.wizard_welcome_body)
    )
    FeatureLine(FitterIcons.Offline, stringResource(R.string.wizard_welcome_offline))
    FeatureLine(FitterIcons.Shield, stringResource(R.string.wizard_welcome_private))
    FeatureLine(FitterIcons.Bolt, stringResource(R.string.wizard_welcome_fast))
}

@Composable
private fun FeatureLine(icon: ImageVector, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(FitterTheme.sizes.iconMedium)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ColumnScope.ThemeStep(state: OnboardingUiState, actions: OnboardingActions) {
    StepHeader(
        title = stringResource(R.string.wizard_theme_title),
        body = stringResource(R.string.wizard_theme_body)
    )

    SelectableOptionCard(
        title = stringResource(R.string.wizard_theme_midnight),
        description = stringResource(R.string.wizard_theme_midnight_desc),
        selected = state.palette == FitterPalette.MIDNIGHT,
        onSelect = { actions.onPaletteChange(FitterPalette.MIDNIGHT) },
        swatch = MidnightDarkScheme.surface
    )
    SelectableOptionCard(
        title = stringResource(R.string.wizard_theme_void),
        description = stringResource(R.string.wizard_theme_void_desc),
        selected = state.palette == FitterPalette.VOID,
        onSelect = { actions.onPaletteChange(FitterPalette.VOID) },
        swatch = VoidDarkScheme.surface
    )

    Text(
        text = stringResource(R.string.wizard_theme_mode).uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    SegmentedSelector(
        options = ThemeMode.entries,
        selected = state.themeMode,
        labelFor = { mode ->
            when (mode) {
                ThemeMode.SYSTEM -> stringResource(R.string.settings_mode_system)
                ThemeMode.LIGHT -> stringResource(R.string.settings_mode_light)
                ThemeMode.DARK -> stringResource(R.string.settings_mode_dark)
            }
        },
        onSelect = actions.onThemeModeChange
    )
}

@Composable
private fun ColumnScope.UnitsStep(state: OnboardingUiState, actions: OnboardingActions) {
    StepHeader(
        title = stringResource(R.string.wizard_units_title),
        body = stringResource(R.string.wizard_units_body)
    )

    SelectableOptionCard(
        title = stringResource(R.string.unit_kg),
        description = null,
        selected = state.weightUnit == WeightUnit.KG,
        onSelect = { actions.onWeightUnitChange(WeightUnit.KG) }
    )
    SelectableOptionCard(
        title = stringResource(R.string.unit_lbs),
        description = null,
        selected = state.weightUnit == WeightUnit.LBS,
        onSelect = { actions.onWeightUnitChange(WeightUnit.LBS) }
    )
}

@Composable
private fun ColumnScope.FirstRecordStep(state: OnboardingUiState, actions: OnboardingActions) {
    val unitLabel = when (state.weightUnit) {
        WeightUnit.KG -> stringResource(R.string.unit_kg_short)
        WeightUnit.LBS -> stringResource(R.string.unit_lbs_short)
    }

    StepHeader(
        title = stringResource(R.string.wizard_record_title),
        body = stringResource(R.string.wizard_record_body)
    )

    FitterTextField(
        value = state.weightInput,
        onValueChange = actions.onWeightChange,
        label = stringResource(R.string.wizard_record_weight) + " (" + unitLabel + ")",
        keyboardType = KeyboardType.Decimal
    )
    FitterTextField(
        value = state.waistInput,
        onValueChange = actions.onWaistChange,
        label = stringResource(R.string.wizard_record_waist),
        keyboardType = KeyboardType.Decimal
    )
    FitterTextField(
        value = state.chestInput,
        onValueChange = actions.onChestChange,
        label = stringResource(R.string.wizard_record_chest),
        keyboardType = KeyboardType.Decimal
    )
}

@Composable
private fun ColumnScope.AccountStep(state: OnboardingUiState) {
    StepHeader(
        title = stringResource(R.string.wizard_account_title),
        body = stringResource(R.string.wizard_account_body)
    )

    FitterCard {
        AccountRow(stringResource(R.string.wizard_account_name), state.accountName)
        Spacer(Modifier.height(FitterTheme.spacing.md))
        AccountRow(stringResource(R.string.wizard_account_email), state.accountEmail)
    }
}

@Composable
private fun AccountRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.xxs)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value.ifBlank { stringResource(R.string.value_empty) },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun NavigationRow(state: OnboardingUiState, actions: OnboardingActions) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = FitterTheme.spacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            if (!state.isFirstStep) {
                FitterTextButton(
                    text = stringResource(R.string.action_back),
                    onClick = actions.onBack
                )
            }
        }

        FitterPrimaryButton(
            text = if (state.isLastStep) {
                stringResource(R.string.action_finish)
            } else {
                stringResource(R.string.action_continue)
            },
            onClick = if (state.isLastStep) actions.onFinish else actions.onNext,
            enabled = !state.isSubmitting
        )
    }
}
