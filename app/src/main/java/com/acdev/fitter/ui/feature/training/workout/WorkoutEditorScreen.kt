package com.acdev.fitter.ui.feature.training.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.acdev.fitter.R
import com.acdev.fitter.domain.model.PlannedSet
import com.acdev.fitter.domain.model.WorkoutExerciseItem
import com.acdev.fitter.ui.components.AddRowCard
import com.acdev.fitter.ui.components.ConfirmDialog
import com.acdev.fitter.ui.components.DetailHeader
import com.acdev.fitter.ui.components.EmptyState
import com.acdev.fitter.ui.components.FitterCard
import com.acdev.fitter.ui.components.FitterIconButton
import com.acdev.fitter.ui.components.FitterTextField
import com.acdev.fitter.ui.components.NumberStepper
import com.acdev.fitter.ui.components.OrderBadge
import com.acdev.fitter.ui.components.PulseLoader
import com.acdev.fitter.ui.components.SectionHeader
import com.acdev.fitter.ui.feature.training.components.ExercisePickerSheet
import com.acdev.fitter.ui.feature.training.components.ExerciseRow
import com.acdev.fitter.ui.feature.training.components.SetEditorSheet
import com.acdev.fitter.ui.feature.training.components.TagBadge
import com.acdev.fitter.ui.feature.training.components.setTypeLabel
import com.acdev.fitter.ui.icons.FitterIcons
import com.acdev.fitter.ui.theme.FitterMotion
import com.acdev.fitter.ui.theme.FitterTheme
import com.acdev.fitter.ui.util.Format
import kotlinx.coroutines.delay

private const val REST_STEP_SECONDS = 15
private const val MIN_REST_SECONDS = 15
private const val MAX_REST_SECONDS = 600
private const val NOTES_PERSIST_DELAY_MILLIS = 400L
private const val SEPARATOR = " · "

/** Lo que el editor de listas puede pedir. */
data class WorkoutEditorActions(
    val onBack: () -> Unit,
    val onNameChange: (String) -> Unit,
    val onNotesChange: (String) -> Unit,
    val onSearch: (String) -> Unit,
    val onFilterBodyPart: (String?) -> Unit,
    val onCreateExercise: () -> Unit,
    val onAddExercises: (List<String>) -> Unit,
    val onRemoveExercise: (String) -> Unit,
    val onMoveExercise: (String, Int) -> Unit,
    val onRestChange: (String, Int) -> Unit,
    val onExerciseNotesChange: (String, String) -> Unit,
    val onAddSet: (String) -> Unit,
    val onUpdateSet: (String, PlannedSet) -> Unit,
    val onApplySetToAll: (String, PlannedSet) -> Unit,
    val onRemoveSet: (String, String) -> Unit,
    val onDelete: () -> Unit
)

/** Serie que se esta editando, junto al ejercicio al que pertenece. */
private data class SetEdit(
    val workoutExerciseId: String,
    val inheritedRestSeconds: Int,
    val set: PlannedSet
)

/**
 * Editor de una lista de ejercicios.
 *
 * La lista se lee cerrada: cada ejercicio ocupa una fila con su plan resumido. Solo el ejercicio
 * abierto muestra descanso, notas y series, de modo que configurar diez ejercicios no obliga a
 * desplazarse por cien controles.
 */
@Composable
fun WorkoutEditorScreen(
    state: WorkoutEditorUiState,
    actions: WorkoutEditorActions,
    modifier: Modifier = Modifier
) {
    var expandedId by rememberSaveable { mutableStateOf<String?>(null) }
    var pickingExercises by rememberSaveable { mutableStateOf(false) }
    var confirmingDelete by rememberSaveable { mutableStateOf(false) }
    var editingSet by remember { mutableStateOf<SetEdit?>(null) }

    val workout = state.workout

    Column(modifier = modifier.fillMaxSize()) {
        DetailHeader(
            title = workout?.name.orEmpty(),
            onBack = actions.onBack,
            modifier = Modifier.padding(horizontal = FitterTheme.spacing.screenHorizontal),
            trailing = {
                if (workout != null) {
                    FitterIconButton(
                        icon = FitterIcons.Trash,
                        contentDescription = stringResource(R.string.workout_delete),
                        onClick = { confirmingDelete = true },
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        when {
            state.isLoading -> LoadingRow()
            workout == null -> MissingWorkout()
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
                            value = state.draft.name,
                            onValueChange = actions.onNameChange,
                            label = stringResource(R.string.workout_name_label)
                        )
                        FitterTextField(
                            value = state.draft.notes,
                            onValueChange = actions.onNotesChange,
                            label = stringResource(R.string.workout_notes_label),
                            singleLine = false
                        )
                    }
                }

                item(key = "exercisesHeader") {
                    SectionHeader(title = stringResource(R.string.workout_exercises_section))
                }

                if (workout.exercises.isEmpty()) {
                    item(key = "exercisesEmpty") {
                        EmptyState(
                            icon = FitterIcons.Dumbbell,
                            title = stringResource(R.string.state_empty_title),
                            message = stringResource(R.string.workout_empty_exercises)
                        )
                    }
                }

                itemsIndexed(
                    items = workout.exercises,
                    key = { _, item -> item.id }
                ) { index, item ->
                    ExerciseCard(
                        position = index + 1,
                        item = item,
                        expanded = expandedId == item.id,
                        canMoveUp = index > 0,
                        canMoveDown = index < workout.exercises.lastIndex,
                        actions = actions,
                        onToggle = {
                            expandedId = when (expandedId) {
                                item.id -> null
                                else -> item.id
                            }
                        },
                        onEditSet = { set ->
                            editingSet = SetEdit(item.id, item.restSeconds, set)
                        }
                    )
                }

                item(key = "exercisesAdd") {
                    AddRowCard(
                        label = stringResource(R.string.workout_add_exercise),
                        onClick = { pickingExercises = true }
                    )
                }
            }
        }
    }

    if (pickingExercises) {
        ExercisePickerSheet(
            exercises = state.picker.visible,
            bodyParts = state.picker.bodyParts,
            query = state.picker.query,
            selectedBodyPart = state.picker.bodyPart,
            onQueryChange = actions.onSearch,
            onBodyPartChange = actions.onFilterBodyPart,
            onCreateExercise = {
                pickingExercises = false
                actions.onCreateExercise()
            },
            onConfirm = { ids ->
                pickingExercises = false
                actions.onAddExercises(ids)
            },
            onDismiss = { pickingExercises = false }
        )
    }

    editingSet?.let { edit ->
        SetEditorSheet(
            set = edit.set,
            inheritedRestSeconds = edit.inheritedRestSeconds,
            onSave = { updated ->
                editingSet = null
                actions.onUpdateSet(edit.workoutExerciseId, updated)
            },
            onApplyToAll = { template ->
                editingSet = null
                actions.onApplySetToAll(edit.workoutExerciseId, template)
            },
            onDelete = {
                editingSet = null
                actions.onRemoveSet(edit.workoutExerciseId, edit.set.id)
            },
            onDismiss = { editingSet = null }
        )
    }

    if (confirmingDelete && workout != null) {
        ConfirmDialog(
            title = workout.name,
            message = stringResource(R.string.workout_delete_confirm),
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
private fun ExerciseCard(
    position: Int,
    item: WorkoutExerciseItem,
    expanded: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    actions: WorkoutEditorActions,
    onToggle: () -> Unit,
    onEditSet: (PlannedSet) -> Unit
) {
    FitterCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onToggle,
        selected = expanded,
        contentPadding = PaddingValues(
            start = FitterTheme.spacing.md,
            end = FitterTheme.spacing.md,
            top = FitterTheme.spacing.md,
            bottom = FitterTheme.spacing.md
        )
    ) {
        ExerciseRow(
            icon = item.icon,
            name = item.name,
            subtitle = item.planSummary(),
            leading = { OrderBadge(position = position) },
            trailing = {
                Icon(
                    imageVector = FitterIcons.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(FitterTheme.sizes.iconMedium)
                )
            }
        )

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(FitterMotion.standardTween()) +
                fadeIn(FitterMotion.standardTween()),
            exit = shrinkVertically(FitterMotion.standardTween()) +
                fadeOut(FitterMotion.standardTween())
        ) {
            ExerciseDetails(
                item = item,
                canMoveUp = canMoveUp,
                canMoveDown = canMoveDown,
                actions = actions,
                onEditSet = onEditSet
            )
        }
    }
}

@Composable
private fun ExerciseDetails(
    item: WorkoutExerciseItem,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    actions: WorkoutEditorActions,
    onEditSet: (PlannedSet) -> Unit
) {
    Column(
        modifier = Modifier.padding(top = FitterTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.md)
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        NumberStepper(
            label = stringResource(R.string.workout_rest_label),
            value = stringResource(R.string.set_rest_value, item.restSeconds),
            canDecrease = item.restSeconds > MIN_REST_SECONDS,
            canIncrease = item.restSeconds < MAX_REST_SECONDS,
            onDecrease = {
                actions.onRestChange(item.id, item.restSeconds - REST_STEP_SECONDS)
            },
            onIncrease = {
                actions.onRestChange(item.id, item.restSeconds + REST_STEP_SECONDS)
            }
        )

        ExerciseNotesField(
            workoutExerciseId = item.id,
            initialNotes = item.notes.orEmpty(),
            onPersist = actions.onExerciseNotesChange
        )

        SectionHeader(title = stringResource(R.string.workout_sets_section))

        item.sets.forEach { set ->
            SetRow(set = set, onClick = { onEditSet(set) })
        }

        AddRowCard(
            label = stringResource(R.string.workout_add_set),
            onClick = { actions.onAddSet(item.id) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FitterIconButton(
                icon = FitterIcons.ArrowUp,
                contentDescription = stringResource(R.string.cd_move_up),
                onClick = { actions.onMoveExercise(item.id, -1) },
                enabled = canMoveUp
            )
            FitterIconButton(
                icon = FitterIcons.ArrowDown,
                contentDescription = stringResource(R.string.cd_move_down),
                onClick = { actions.onMoveExercise(item.id, 1) },
                enabled = canMoveDown
            )
            Spacer(modifier = Modifier.weight(1f))
            FitterIconButton(
                icon = FitterIcons.Trash,
                contentDescription = stringResource(R.string.workout_remove_exercise),
                onClick = { actions.onRemoveExercise(item.id) },
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Nota de un ejercicio.
 *
 * Guarda sola tras una pausa: el campo es local para que el cursor no salte, y la escritura se
 * retrasa para no tocar la base en cada tecla.
 */
@Composable
private fun ExerciseNotesField(
    workoutExerciseId: String,
    initialNotes: String,
    onPersist: (String, String) -> Unit
) {
    var text by remember(workoutExerciseId) { mutableStateOf(initialNotes) }

    LaunchedEffect(text) {
        if (text == initialNotes) return@LaunchedEffect
        delay(NOTES_PERSIST_DELAY_MILLIS)
        onPersist(workoutExerciseId, text)
    }

    FitterTextField(
        value = text,
        onValueChange = { text = it },
        label = stringResource(R.string.workout_exercise_notes_label),
        singleLine = false
    )
}

@Composable
private fun SetRow(set: PlannedSet, onClick: () -> Unit) {
    FitterCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(
            horizontal = FitterTheme.spacing.md,
            vertical = FitterTheme.spacing.sm
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OrderBadge(position = set.setOrder)
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        R.string.set_reps_range,
                        set.repIni,
                        set.repEnd
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = set.detailLine(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            TagBadge(text = setTypeLabel(set.type))
        }
    }
}

/** Resumen del plan de un ejercicio: cuantas series y en que rango de repeticiones. */
@Composable
private fun WorkoutExerciseItem.planSummary(): String {
    val setsLabel = pluralStringResource(R.plurals.count_sets, sets.size, sets.size)
    val first = sets.firstOrNull() ?: return setsLabel
    val reps = stringResource(R.string.set_reps_range, first.repIni, first.repEnd)
    return stringResource(R.string.workout_summary, setsLabel, reps)
}

/** Lo que distingue a una serie de la anterior: RPE y descanso propio, si los tiene. */
@Composable
private fun PlannedSet.detailLine(): String {
    val rpe = targetRpe?.let { stringResource(R.string.set_rpe_value, Format.oneDecimal(it)) }
    val rest = restSeconds?.let { stringResource(R.string.set_rest_value, it) }
    val parts = listOfNotNull(rpe, rest)
    return when {
        parts.isEmpty() -> stringResource(R.string.set_rest_inherited)
        else -> parts.joinToString(separator = SEPARATOR)
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
private fun MissingWorkout() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(FitterTheme.spacing.screenHorizontal)
    ) {
        EmptyState(
            icon = FitterIcons.List,
            title = stringResource(R.string.state_empty_title),
            message = stringResource(R.string.workout_not_found)
        )
    }
}
