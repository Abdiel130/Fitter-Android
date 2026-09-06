package com.acdev.fitter.ui.feature.training

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.acdev.fitter.domain.model.ActiveRoutineLock
import com.acdev.fitter.domain.model.ExerciseSummary
import com.acdev.fitter.domain.model.RoutineSummary
import com.acdev.fitter.domain.model.WorkoutSummary
import com.acdev.fitter.ui.components.AddRowCard
import com.acdev.fitter.ui.components.ConfirmDialog
import com.acdev.fitter.ui.components.EmptyState
import com.acdev.fitter.ui.components.FitterCard
import com.acdev.fitter.ui.components.FitterIconButton
import com.acdev.fitter.ui.components.FitterTextButton
import com.acdev.fitter.ui.components.FitterTextField
import com.acdev.fitter.ui.components.PulseLoader
import com.acdev.fitter.ui.components.SegmentedSelector
import com.acdev.fitter.ui.feature.training.components.BodyPartFilters
import com.acdev.fitter.ui.feature.training.components.ExerciseRow
import com.acdev.fitter.ui.feature.training.components.TagBadge
import com.acdev.fitter.ui.feature.training.components.contextLine
import com.acdev.fitter.ui.icons.FitterIcons
import com.acdev.fitter.ui.theme.FitterMotion
import com.acdev.fitter.ui.theme.FitterTheme
import com.acdev.fitter.ui.util.Format

/** Lo que la pantalla de entreno puede pedirle a su ViewModel y a la navegacion. */
data class TrainingActions(
    val onSelectTab: (TrainingTab) -> Unit,
    val onSearch: (String) -> Unit,
    val onFilterBodyPart: (String?) -> Unit,
    val onCreateRoutine: (String) -> Unit,
    val onOpenRoutine: (String) -> Unit,
    val onActivateRoutine: (String) -> Unit,
    val onDuplicateRoutine: (String, String) -> Unit,
    val onDeleteRoutine: (String) -> Unit,
    val onCreateWorkout: (String) -> Unit,
    val onOpenWorkout: (String) -> Unit,
    val onDuplicateWorkout: (String, String) -> Unit,
    val onDeleteWorkout: (String) -> Unit,
    val onCreateExercise: () -> Unit,
    val onOpenExercise: (String) -> Unit
)

/**
 * Entreno.
 *
 * Tres pestanas de lo general a lo concreto: la rutina decide que semana entrenas, la lista que
 * ejercicios hace un dia y el catalogo que ejercicios existen. La cabecera no se desplaza para
 * que cambiar de nivel sea siempre un toque.
 */
@Composable
fun TrainingScreen(
    state: TrainingUiState,
    actions: TrainingActions,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(horizontal = FitterTheme.spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.md)
        ) {
            Text(
                text = stringResource(R.string.nav_training),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            SegmentedSelector(
                options = TrainingTab.entries,
                selected = state.tab,
                labelFor = { tab -> tabLabel(tab) },
                onSelect = actions.onSelectTab
            )
        }

        if (state.isLoading) {
            LoadingRow()
            return@Column
        }

        AnimatedContent(
            targetState = state.tab,
            transitionSpec = {
                fadeIn(FitterMotion.standardTween()) togetherWith
                    fadeOut(FitterMotion.standardTween(FitterMotion.Duration.QUICK))
            },
            label = "trainingTab"
        ) { tab ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = FitterTheme.spacing.screenHorizontal,
                    end = FitterTheme.spacing.screenHorizontal,
                    top = FitterTheme.spacing.lg,
                    bottom = FitterTheme.spacing.xxxl
                ),
                verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)
            ) {
                when (tab) {
                    TrainingTab.ROUTINES -> routinesTab(state, actions)
                    TrainingTab.WORKOUTS -> workoutsTab(state, actions)
                    TrainingTab.EXERCISES -> exercisesTab(state, actions)
                }
            }
        }
    }
}

@Composable
private fun tabLabel(tab: TrainingTab): String = when (tab) {
    TrainingTab.ROUTINES -> stringResource(R.string.training_tab_routines)
    TrainingTab.WORKOUTS -> stringResource(R.string.training_tab_workouts)
    TrainingTab.EXERCISES -> stringResource(R.string.training_tab_exercises)
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

// ---------------------------------------------------------------------------
// Rutinas
// ---------------------------------------------------------------------------

private fun LazyListScope.routinesTab(state: TrainingUiState, actions: TrainingActions) {
    if (state.lock.isLocked) {
        item(key = "routineLock") { LockNotice(state.lock) }
    }

    if (state.routines.isEmpty()) {
        item(key = "routinesEmpty") {
            EmptyState(
                icon = FitterIcons.Layers,
                title = stringResource(R.string.routines_empty_title),
                message = stringResource(R.string.routines_empty_body)
            )
        }
    }

    items(state.routines, key = RoutineSummary::id) { routine ->
        RoutineCard(routine = routine, lock = state.lock, actions = actions)
    }

    item(key = "routineAdd") {
        val title = stringResource(R.string.routines_default_title)
        AddRowCard(
            label = stringResource(R.string.routines_new),
            onClick = { actions.onCreateRoutine(title) }
        )
    }
}

@Composable
private fun LockNotice(lock: ActiveRoutineLock) {
    val until = lock.unlocksOn?.let(Format::mediumDate) ?: return

    FitterCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = FitterIcons.Lock,
                contentDescription = null,
                tint = FitterTheme.colors.warning,
                modifier = Modifier.size(FitterTheme.sizes.iconLarge)
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.routine_locked_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.routine_locked_body, until),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RoutineCard(
    routine: RoutineSummary,
    lock: ActiveRoutineLock,
    actions: TrainingActions
) {
    var confirmingDelete by rememberSaveable(routine.id) { mutableStateOf(false) }
    val copyName = stringResource(R.string.routine_copy_name, routine.title)
    // Con la semana empezada no se cambia de rutina, y borrar la activa seria cambiarla.
    val canSwitch = !lock.isLocked

    FitterCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { actions.onOpenRoutine(routine.id) },
        selected = routine.isActive
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = routine.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (routine.isActive) {
                TagBadge(text = stringResource(R.string.routine_active_badge))
            }
        }

        Text(
            text = stringResource(
                R.string.routine_summary,
                pluralStringResource(
                    R.plurals.count_workouts,
                    routine.workoutCount,
                    routine.workoutCount
                ),
                pluralStringResource(
                    R.plurals.count_exercises,
                    routine.exerciseCount,
                    routine.exerciseCount
                ),
                pluralStringResource(R.plurals.count_sets, routine.setCount, routine.setCount)
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = FitterTheme.spacing.xxs)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = FitterTheme.spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!routine.isActive) {
                FitterTextButton(
                    text = stringResource(R.string.routine_activate),
                    onClick = { actions.onActivateRoutine(routine.id) },
                    enabled = canSwitch
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            FitterIconButton(
                icon = FitterIcons.Copy,
                contentDescription = stringResource(R.string.action_duplicate),
                onClick = { actions.onDuplicateRoutine(routine.id, copyName) }
            )
            FitterIconButton(
                icon = FitterIcons.Trash,
                contentDescription = stringResource(R.string.action_delete),
                onClick = { confirmingDelete = true },
                enabled = canSwitch || !routine.isActive,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }

    if (confirmingDelete) {
        ConfirmDialog(
            title = routine.title,
            message = stringResource(R.string.routine_delete_confirm),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = {
                confirmingDelete = false
                actions.onDeleteRoutine(routine.id)
            },
            onDismiss = { confirmingDelete = false }
        )
    }
}

// ---------------------------------------------------------------------------
// Listas de ejercicios
// ---------------------------------------------------------------------------

private fun LazyListScope.workoutsTab(state: TrainingUiState, actions: TrainingActions) {
    if (state.workouts.isEmpty()) {
        item(key = "workoutsEmpty") {
            EmptyState(
                icon = FitterIcons.List,
                title = stringResource(R.string.workouts_empty_title),
                message = stringResource(R.string.workouts_empty_body)
            )
        }
    }

    items(state.workouts, key = WorkoutSummary::id) { workout ->
        WorkoutCard(workout = workout, actions = actions)
    }

    item(key = "workoutAdd") {
        val name = stringResource(R.string.workouts_default_name)
        AddRowCard(
            label = stringResource(R.string.workouts_new),
            onClick = { actions.onCreateWorkout(name) }
        )
    }
}

@Composable
private fun WorkoutCard(workout: WorkoutSummary, actions: TrainingActions) {
    var confirmingDelete by rememberSaveable(workout.id) { mutableStateOf(false) }
    val copyName = stringResource(R.string.workout_copy_name, workout.name)

    FitterCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { actions.onOpenWorkout(workout.id) }
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = workout.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (workout.routineCount > 0) {
                TagBadge(
                    text = pluralStringResource(
                        R.plurals.count_routines_using,
                        workout.routineCount,
                        workout.routineCount
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Text(
            text = stringResource(
                R.string.workout_summary,
                pluralStringResource(
                    R.plurals.count_exercises,
                    workout.exerciseCount,
                    workout.exerciseCount
                ),
                pluralStringResource(R.plurals.count_sets, workout.setCount, workout.setCount)
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = FitterTheme.spacing.xxs)
        )

        if (workout.previewExercises.isNotEmpty()) {
            Text(
                text = workout.previewExercises.joinToString(separator = PREVIEW_SEPARATOR),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = FitterTheme.spacing.xxs),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = FitterTheme.spacing.xs),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FitterIconButton(
                icon = FitterIcons.Copy,
                contentDescription = stringResource(R.string.action_duplicate),
                onClick = { actions.onDuplicateWorkout(workout.id, copyName) }
            )
            FitterIconButton(
                icon = FitterIcons.Trash,
                contentDescription = stringResource(R.string.action_delete),
                onClick = { confirmingDelete = true },
                tint = MaterialTheme.colorScheme.error
            )
        }
    }

    if (confirmingDelete) {
        ConfirmDialog(
            title = workout.name,
            message = stringResource(R.string.workout_delete_confirm),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = {
                confirmingDelete = false
                actions.onDeleteWorkout(workout.id)
            },
            onDismiss = { confirmingDelete = false }
        )
    }
}

// ---------------------------------------------------------------------------
// Catalogo de ejercicios
// ---------------------------------------------------------------------------

private fun LazyListScope.exercisesTab(state: TrainingUiState, actions: TrainingActions) {
    item(key = "exerciseSearch") {
        Column(verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.md)) {
            FitterTextField(
                value = state.catalog.query,
                onValueChange = actions.onSearch,
                label = stringResource(R.string.picker_search)
            )
            BodyPartFilters(
                bodyParts = state.catalog.facets.bodyParts,
                selected = state.catalog.bodyPart,
                onSelect = actions.onFilterBodyPart
            )
        }
    }

    item(key = "exerciseAdd") {
        AddRowCard(
            label = stringResource(R.string.exercises_new),
            onClick = actions.onCreateExercise
        )
    }

    if (state.catalog.visible.isEmpty()) {
        item(key = "exercisesEmpty") {
            EmptyState(
                icon = FitterIcons.Search,
                title = stringResource(R.string.exercises_empty_title),
                message = stringResource(R.string.picker_empty)
            )
        }
    }

    items(state.catalog.visible, key = ExerciseSummary::id) { exercise ->
        FitterCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { actions.onOpenExercise(exercise.id) },
            contentPadding = PaddingValues(FitterTheme.spacing.md)
        ) {
            ExerciseRow(
                icon = exercise.icon,
                name = exercise.name,
                subtitle = exercise.contextLine(),
                trailing = {
                    when {
                        exercise.isCustom -> TagBadge(
                            text = stringResource(R.string.exercise_custom_badge)
                        )

                        else -> Icon(
                            imageVector = FitterIcons.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(FitterTheme.sizes.iconSmall)
                        )
                    }
                }
            )
        }
    }
}

private const val PREVIEW_SEPARATOR = " · "
