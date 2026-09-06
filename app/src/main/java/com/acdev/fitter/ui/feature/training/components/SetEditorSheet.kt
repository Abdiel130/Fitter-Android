package com.acdev.fitter.ui.feature.training.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.acdev.fitter.R
import com.acdev.fitter.domain.model.PlannedSet
import com.acdev.fitter.domain.model.SetType
import com.acdev.fitter.ui.components.FitterPrimaryButton
import com.acdev.fitter.ui.components.FitterTextButton
import com.acdev.fitter.ui.components.NumberStepper
import com.acdev.fitter.ui.components.SectionHeader
import com.acdev.fitter.ui.components.SegmentedSelector
import com.acdev.fitter.ui.theme.FitterTheme
import com.acdev.fitter.ui.util.Format

private const val MIN_REPS = 1
private const val MAX_REPS = 50
private const val MIN_RPE = 6.0
private const val MAX_RPE = 10.0
private const val RPE_STEP = 0.5
private const val REST_STEP_SECONDS = 15
private const val MAX_REST_SECONDS = 600

/**
 * Configuracion de una serie.
 *
 * Va en hoja y no en la propia fila porque la fila tiene que seguir siendo legible de un vistazo:
 * la lista muestra el plan, la hoja lo cambia. Todo se ajusta con pasos, sin abrir el teclado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetEditorSheet(
    set: PlannedSet,
    inheritedRestSeconds: Int,
    onSave: (PlannedSet) -> Unit,
    onApplyToAll: (PlannedSet) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var draft by remember(set.id) { mutableStateOf(set) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = FitterTheme.spacing.screenHorizontal)
                .padding(bottom = FitterTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.lg)
        ) {
            Text(
                text = stringResource(R.string.set_editor_title, set.setOrder),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)) {
                SectionHeader(title = stringResource(R.string.set_type_label))
                SegmentedSelector(
                    options = SetType.entries,
                    selected = draft.type,
                    labelFor = { type -> setTypeLabel(type) },
                    onSelect = { type -> draft = draft.copy(type = type) }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)) {
                SectionHeader(title = stringResource(R.string.set_reps_label))
                NumberStepper(
                    label = stringResource(R.string.set_rep_min),
                    value = draft.repIni.toString(),
                    canDecrease = draft.repIni > MIN_REPS,
                    canIncrease = draft.repIni < draft.repEnd,
                    onDecrease = { draft = draft.copy(repIni = draft.repIni - 1) },
                    onIncrease = { draft = draft.copy(repIni = draft.repIni + 1) }
                )
                NumberStepper(
                    label = stringResource(R.string.set_rep_max),
                    value = draft.repEnd.toString(),
                    canDecrease = draft.repEnd > draft.repIni,
                    canIncrease = draft.repEnd < MAX_REPS,
                    onDecrease = { draft = draft.copy(repEnd = draft.repEnd - 1) },
                    onIncrease = { draft = draft.copy(repEnd = draft.repEnd + 1) }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)) {
                SectionHeader(title = stringResource(R.string.set_rpe_label))
                NumberStepper(
                    label = stringResource(R.string.set_rpe_label),
                    value = draft.targetRpe?.let(Format::oneDecimal)
                        ?: stringResource(R.string.set_rpe_none),
                    canDecrease = draft.targetRpe != null,
                    canIncrease = (draft.targetRpe ?: (MIN_RPE - RPE_STEP)) < MAX_RPE,
                    onDecrease = { draft = draft.copy(targetRpe = draft.targetRpe.decreased()) },
                    onIncrease = { draft = draft.copy(targetRpe = draft.targetRpe.increased()) }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)) {
                SectionHeader(title = stringResource(R.string.set_rest_label))
                NumberStepper(
                    label = stringResource(R.string.set_rest_label),
                    value = draft.restSeconds
                        ?.let { stringResource(R.string.set_rest_value, it) }
                        ?: stringResource(R.string.set_rest_inherited),
                    canDecrease = draft.restSeconds != null,
                    canIncrease = (draft.restSeconds ?: 0) < MAX_REST_SECONDS,
                    onDecrease = {
                        draft = draft.copy(restSeconds = draft.restSeconds.restDecreased())
                    },
                    onIncrease = {
                        draft = draft.copy(
                            restSeconds = draft.restSeconds.restIncreased(inheritedRestSeconds)
                        )
                    }
                )
            }

            FitterPrimaryButton(
                text = stringResource(R.string.action_save),
                onClick = { onSave(draft) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FitterTextButton(
                    text = stringResource(R.string.set_apply_all),
                    onClick = { onApplyToAll(draft) }
                )
                FitterTextButton(
                    text = stringResource(R.string.set_delete),
                    onClick = onDelete
                )
            }
        }
    }
}

/** Bajar desde el minimo deja la serie sin RPE: no todo el mundo lo usa. */
private fun Double?.decreased(): Double? = when {
    this == null -> null
    this <= MIN_RPE -> null
    else -> this - RPE_STEP
}

private fun Double?.increased(): Double? = when {
    this == null -> MIN_RPE
    else -> (this + RPE_STEP).coerceAtMost(MAX_RPE)
}

/** Bajar desde el minimo devuelve la serie al descanso del ejercicio. */
private fun Int?.restDecreased(): Int? = when {
    this == null -> null
    this <= REST_STEP_SECONDS -> null
    else -> this - REST_STEP_SECONDS
}

private fun Int?.restIncreased(inherited: Int): Int = when {
    this == null -> inherited + REST_STEP_SECONDS
    else -> (this + REST_STEP_SECONDS).coerceAtMost(MAX_REST_SECONDS)
}
