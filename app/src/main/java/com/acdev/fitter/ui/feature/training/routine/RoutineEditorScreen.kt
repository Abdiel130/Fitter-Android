package com.acdev.fitter.ui.feature.training.routine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.acdev.fitter.R
import com.acdev.fitter.domain.model.RoutineWorkoutItem
import com.acdev.fitter.ui.components.AddRowCard
import com.acdev.fitter.ui.components.ConfirmDialog
import com.acdev.fitter.ui.components.DetailHeader
import com.acdev.fitter.ui.components.EmptyState
import com.acdev.fitter.ui.components.FitterCard
import com.acdev.fitter.ui.components.FitterIconButton
import com.acdev.fitter.ui.components.FitterPrimaryButton
import com.acdev.fitter.ui.components.FitterTextField
import com.acdev.fitter.ui.components.OrderBadge
import com.acdev.fitter.ui.components.PulseLoader
import com.acdev.fitter.ui.components.SectionHeader
import com.acdev.fitter.ui.feature.training.components.TagBadge
import com.acdev.fitter.ui.feature.training.components.WorkoutPickerSheet
import com.acdev.fitter.ui.icons.FitterIcons
import com.acdev.fitter.ui.theme.FitterTheme
import com.acdev.fitter.ui.util.Format

/** Lo que el editor de rutina puede pedir. */
data class RoutineEditorActions(
    val onBack: () -> Unit,
    val onTitleChange: (String) -> Unit,
    val onDescriptionChange: (String) -> Unit,
    val onActivate: () -> Unit,
    val onAddWorkouts: (List<String>) -> Unit,
    val onRemoveEntry: (String) -> Unit,
    val onMoveEntry: (String, Int) -> Unit,
    val onDelete: () -> Unit,
    val onOpenWorkout: (String) -> Unit
)

/**
 * Editor de rutina.
 *
 * La secuencia se lee como una escalera numerada: la posicion la da el orden en pantalla, no un
 * campo que el usuario tenga que cuadrar a mano.
 */
@Composable
fun RoutineEditorScreen(
    state: RoutineEditorUiState,
    actions: RoutineEditorActions,
    modifier: Modifier = Modifier
) {
    var pickingWorkouts by rememberSaveable { mutableStateOf(false) }
    var confirmingDelete by rememberSaveable { mutableStateOf(false) }

    val routine = state.routine

    Column(modifier = modifier.fillMaxSize()) {
        DetailHeader(
            title = routine?.title.orEmpty(),
            onBack = actions.onBack,
            modifier = Modifier.padding(horizontal = FitterTheme.spacing.screenHorizontal),
            trailing = {
                if (routine != null) {
                    FitterIconButton(
                        icon = FitterIcons.Trash,
                        contentDescription = stringResource(R.string.routine_delete),
                        onClick = { confirmingDelete = true },
                        enabled = !(routine.isActive && state.lock.isLocked),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        when {
            state.isLoading -> LoadingRow()
            routine == null -> MissingRoutine()
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = FitterTheme.spacing.screenHorizontal,
                    end = FitterTheme.spacing.screenHorizontal,
                    top = FitterTheme.spacing.md,
                    bottom = FitterTheme.spacing.xxxl
                ),
                verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)
            ) {
                item(key = "identity") {
                    Column(verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)) {
                        FitterTextField(
                            value = state.draft.title,
                            onValueChange = actions.onTitleChange,
                            label = stringResource(R.string.routine_title_label)
                        )
                        FitterTextField(
                            value = state.draft.description,
                            onValueChange = actions.onDescriptionChange,
                            label = stringResource(R.string.routine_description_label),
                            singleLine = false
                        )
                    }
                }

                item(key = "activation") {
                    ActivationCard(state = state, onActivate = actions.onActivate)
                }

                item(key = "sequenceHeader") {
                    Column(verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.xxs)) {
                        SectionHeader(title = stringResource(R.string.routine_sequence_section))
                        Text(
                            text = stringResource(R.string.routine_sequence_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (routine.workouts.isEmpty()) {
                    item(key = "sequenceEmpty") {
                        EmptyState(
                            icon = FitterIcons.List,
                            title = stringResource(R.string.state_empty_title),
                            message = stringResource(R.string.routine_empty_workouts)
                        )
                    }
                }

                itemsIndexed(
                    items = routine.workouts,
                    key = { _, entry -> entry.entryId }
                ) { index, entry ->
                    SequenceRow(
                        position = index + 1,
                        entry = entry,
                        canMoveUp = index > 0,
                        canMoveDown = index < routine.workouts.lastIndex,
                        actions = actions
                    )
                }

                item(key = "sequenceAdd") {
                    AddRowCard(
                        label = stringResource(R.string.routine_add_workout),
                        onClick = { pickingWorkouts = true }
                    )
                }
            }
        }
    }

    if (pickingWorkouts) {
        WorkoutPickerSheet(
            workouts = state.availableWorkouts,
            onConfirm = { ids ->
                pickingWorkouts = false
                actions.onAddWorkouts(ids)
            },
            onDismiss = { pickingWorkouts = false }
        )
    }

    if (confirmingDelete && routine != null) {
        ConfirmDialog(
            title = routine.title,
            message = stringResource(R.string.routine_delete_confirm),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = {
                confirmingDelete = false
                actions.onDelete()
            },
            onDismiss = { confirmingDelete = false }
        )
    }
}

@Composable
private fun ActivationCard(state: RoutineEditorUiState, onActivate: () -> Unit) {
    val routine = state.routine ?: return
    val until = state.lock.unlocksOn?.let(Format::mediumDate)

    FitterCard(modifier = Modifier.fillMaxWidth(), selected = routine.isActive) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.dashboard_next_session),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = when {
                        routine.isActive -> stringResource(R.string.routine_active_badge)
                        state.lock.isLocked && until != null ->
                            stringResource(R.string.routine_locked_hint, until)

                        else -> stringResource(R.string.routine_activate)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = FitterTheme.spacing.xxs)
                )
                if (state.lock.isLocked && until != null) {
                    Text(
                        text = stringResource(R.string.routine_locked_body, until),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = FitterTheme.spacing.xxs)
                    )
                }
            }

            when {
                routine.isActive -> Icon(
                    imageVector = FitterIcons.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(FitterTheme.sizes.iconLarge)
                )

                state.lock.isLocked -> Icon(
                    imageVector = FitterIcons.Lock,
                    contentDescription = null,
                    tint = FitterTheme.colors.warning,
                    modifier = Modifier.size(FitterTheme.sizes.iconLarge)
                )

                else -> FitterPrimaryButton(
                    text = stringResource(R.string.routine_activate),
                    onClick = onActivate
                )
            }
        }
    }
}

@Composable
private fun SequenceRow(
    position: Int,
    entry: RoutineWorkoutItem,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    actions: RoutineEditorActions
) {
    FitterCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { actions.onOpenWorkout(entry.workoutId) },
        contentPadding = PaddingValues(
            start = FitterTheme.spacing.md,
            end = FitterTheme.spacing.xs,
            top = FitterTheme.spacing.sm,
            bottom = FitterTheme.spacing.sm
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OrderBadge(position = position)

            Column(Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        R.string.workout_summary,
                        pluralStringResource(
                            R.plurals.count_exercises,
                            entry.exerciseCount,
                            entry.exerciseCount
                        ),
                        pluralStringResource(
                            R.plurals.count_sets,
                            entry.setCount,
                            entry.setCount
                        )
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FitterIconButton(
                icon = FitterIcons.ArrowUp,
                contentDescription = stringResource(R.string.cd_move_up),
                onClick = { actions.onMoveEntry(entry.entryId, -1) },
                enabled = canMoveUp
            )
            FitterIconButton(
                icon = FitterIcons.ArrowDown,
                contentDescription = stringResource(R.string.cd_move_down),
                onClick = { actions.onMoveEntry(entry.entryId, 1) },
                enabled = canMoveDown
            )
            FitterIconButton(
                icon = FitterIcons.Close,
                contentDescription = stringResource(R.string.cd_remove),
                onClick = { actions.onRemoveEntry(entry.entryId) }
            )
        }
    }
}

@Composable
private fun LoadingRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = FitterTheme.spacing.huge),
        horizontalArrangement = Arrangement.Center
    ) {
        PulseLoader(contentDescription = stringResource(R.string.state_loading))
    }
}

@Composable
private fun MissingRoutine() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(FitterTheme.spacing.screenHorizontal)
    ) {
        EmptyState(
            icon = FitterIcons.Layers,
            title = stringResource(R.string.state_empty_title),
            message = stringResource(R.string.routine_not_found)
        )
        Spacer(Modifier.size(FitterTheme.spacing.md))
        TagBadge(text = stringResource(R.string.action_back))
    }
}
