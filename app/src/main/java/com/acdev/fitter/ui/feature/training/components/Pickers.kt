package com.acdev.fitter.ui.feature.training.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.acdev.fitter.R
import com.acdev.fitter.domain.model.ExerciseSummary
import com.acdev.fitter.domain.model.WorkoutSummary
import com.acdev.fitter.ui.components.EmptyState
import com.acdev.fitter.ui.components.FitterCard
import com.acdev.fitter.ui.components.FitterFilterChip
import com.acdev.fitter.ui.components.FitterPrimaryButton
import com.acdev.fitter.ui.components.FitterTextButton
import com.acdev.fitter.ui.components.FitterTextField
import com.acdev.fitter.ui.components.SectionHeader
import com.acdev.fitter.ui.icons.FitterIcons
import com.acdev.fitter.ui.theme.FitterTheme

/** Altura maxima de la lista dentro de una hoja, para que el boton de confirmar no se escape. */
private val SheetListMaxHeight = 380.dp

/**
 * Seleccion multiple de ejercicios.
 *
 * Es multiple a proposito: montar un dia de entreno es elegir seis ejercicios seguidos, y cerrar
 * y reabrir la hoja seis veces seria el gesto mas repetido de la app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerSheet(
    exercises: List<ExerciseSummary>,
    bodyParts: List<String>,
    query: String,
    selectedBodyPart: String?,
    onQueryChange: (String) -> Unit,
    onBodyPartChange: (String?) -> Unit,
    onCreateExercise: () -> Unit,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf(emptyList<String>()) }

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
            verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.picker_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FitterTextButton(
                    text = stringResource(R.string.exercises_new),
                    onClick = onCreateExercise
                )
            }

            FitterTextField(
                value = query,
                onValueChange = onQueryChange,
                label = stringResource(R.string.picker_search)
            )

            BodyPartFilters(
                bodyParts = bodyParts,
                selected = selectedBodyPart,
                onSelect = onBodyPartChange
            )

            when {
                exercises.isEmpty() -> EmptyState(
                    icon = FitterIcons.Search,
                    title = stringResource(R.string.state_empty_title),
                    message = stringResource(R.string.picker_empty)
                )

                else -> LazyColumn(
                    modifier = Modifier.heightIn(max = SheetListMaxHeight),
                    verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)
                ) {
                    items(exercises, key = ExerciseSummary::id) { exercise ->
                        val isSelected = selected.contains(exercise.id)
                        FitterCard(
                            onClick = {
                                selected = when {
                                    isSelected -> selected - exercise.id
                                    else -> selected + exercise.id
                                }
                            },
                            selected = isSelected,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                FitterTheme.spacing.md
                            )
                        ) {
                            ExerciseRow(
                                icon = exercise.icon,
                                name = exercise.name,
                                subtitle = exercise.contextLine(),
                                trailing = {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = FitterIcons.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(FitterTheme.sizes.iconMedium)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            FitterPrimaryButton(
                text = stringResource(R.string.picker_add, selected.size),
                onClick = { onConfirm(selected) },
                enabled = selected.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Seleccion multiple de listas para armar una rutina. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPickerSheet(
    workouts: List<WorkoutSummary>,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf(emptyList<String>()) }

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
            verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.md)
        ) {
            Text(
                text = stringResource(R.string.picker_workouts_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            when {
                workouts.isEmpty() -> EmptyState(
                    icon = FitterIcons.List,
                    title = stringResource(R.string.workouts_empty_title),
                    message = stringResource(R.string.picker_workouts_empty)
                )

                else -> LazyColumn(
                    modifier = Modifier.heightIn(max = SheetListMaxHeight),
                    verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)
                ) {
                    items(workouts, key = WorkoutSummary::id) { workout ->
                        // Una lista puede repetirse en la misma rutina, asi que se cuenta, no se marca.
                        val times = selected.count { it == workout.id }
                        FitterCard(
                            onClick = { selected = selected + workout.id },
                            selected = times > 0,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                FitterTheme.spacing.md
                            )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.md),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = workout.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(
                                            R.string.workout_summary,
                                            pluralStringResource(
                                                R.plurals.count_exercises,
                                                workout.exerciseCount,
                                                workout.exerciseCount
                                            ),
                                            pluralStringResource(
                                                R.plurals.count_sets,
                                                workout.setCount,
                                                workout.setCount
                                            )
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (times > 0) {
                                    TagBadge(text = "x$times")
                                }
                            }
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)) {
                FitterTextButton(
                    text = stringResource(R.string.action_cancel),
                    onClick = { selected = emptyList() },
                    enabled = selected.isNotEmpty()
                )
                FitterPrimaryButton(
                    text = stringResource(R.string.picker_add, selected.size),
                    onClick = { onConfirm(selected) },
                    enabled = selected.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** Fila de zonas del cuerpo. Sin seleccion equivale a "todo". */
@Composable
fun BodyPartFilters(
    bodyParts: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (bodyParts.isEmpty()) return

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)
    ) {
        SectionHeader(title = stringResource(R.string.exercise_body_parts_label))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)) {
            item {
                FitterFilterChip(
                    label = stringResource(R.string.picker_filter_all),
                    selected = selected == null,
                    onClick = { onSelect(null) }
                )
            }
            items(bodyParts) { part ->
                FitterFilterChip(
                    label = part,
                    selected = selected == part,
                    onClick = { onSelect(part) }
                )
            }
        }
    }
}

/** Linea de contexto de un ejercicio: musculo objetivo y material, lo que hace falta para elegir. */
@Composable
fun ExerciseSummary.contextLine(): String {
    val muscle = targetMuscles.firstOrNull() ?: stringResource(R.string.exercise_no_muscle)
    val equipment = equipments.firstOrNull()
    return listOfNotNull(muscle, equipment).joinToString(separator = SEPARATOR)
}

private const val SEPARATOR = " · "
