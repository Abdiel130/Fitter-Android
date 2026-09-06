package com.acdev.fitter.ui.feature.training.exercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.acdev.fitter.R
import com.acdev.fitter.domain.model.ExerciseIcon
import com.acdev.fitter.ui.components.ConfirmDialog
import com.acdev.fitter.ui.components.DetailHeader
import com.acdev.fitter.ui.components.FitterCard
import com.acdev.fitter.ui.components.FitterFilterChip
import com.acdev.fitter.ui.components.FitterIconButton
import com.acdev.fitter.ui.components.FitterPrimaryButton
import com.acdev.fitter.ui.components.FitterTextButton
import com.acdev.fitter.ui.components.FitterTextField
import com.acdev.fitter.ui.components.PulseLoader
import com.acdev.fitter.ui.components.SectionHeader
import com.acdev.fitter.ui.feature.training.components.ExerciseIconTile
import com.acdev.fitter.ui.icons.FitterIcons
import com.acdev.fitter.ui.theme.FitterTheme

/** Lo que el editor de ejercicio puede pedir. */
data class ExerciseEditorActions(
    val onBack: () -> Unit,
    val onNameChange: (String) -> Unit,
    val onIconChange: (ExerciseIcon) -> Unit,
    val onAddInstruction: () -> Unit,
    val onInstructionChange: (Int, String) -> Unit,
    val onRemoveInstruction: (Int) -> Unit,
    val onToggleBodyPart: (String) -> Unit,
    val onToggleTargetMuscle: (String) -> Unit,
    val onToggleSecondaryMuscle: (String) -> Unit,
    val onToggleEquipment: (String) -> Unit,
    val onSave: () -> Unit,
    val onDelete: () -> Unit
)

/**
 * Editor de un ejercicio del catalogo propio.
 *
 * Todo lo que clasifica un ejercicio se elige con etiquetas, y cualquier etiqueta que falte se
 * escribe en el momento: obligar a salir a otra pantalla para dar de alta un musculo romperia el
 * unico motivo por el que se abre esta.
 */
@Composable
fun ExerciseEditorScreen(
    state: ExerciseEditorUiState,
    actions: ExerciseEditorActions,
    modifier: Modifier = Modifier
) {
    var confirmingDelete by rememberSaveable { mutableStateOf(false) }

    val title = when {
        state.isNew -> stringResource(R.string.exercise_editor_new)
        else -> stringResource(R.string.exercise_editor_edit)
    }

    Column(modifier = modifier.fillMaxSize()) {
        DetailHeader(
            title = title,
            onBack = actions.onBack,
            modifier = Modifier.padding(horizontal = FitterTheme.spacing.screenHorizontal),
            trailing = {
                if (!state.isNew && state.isEditable) {
                    FitterIconButton(
                        icon = FitterIcons.Trash,
                        contentDescription = stringResource(R.string.exercise_delete),
                        onClick = { confirmingDelete = true },
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        when {
            state.isLoading -> LoadingRow()
            else -> EditorContent(state = state, actions = actions)
        }
    }

    if (confirmingDelete) {
        ConfirmDialog(
            title = state.draft.name,
            message = deleteMessage(state.usageCount),
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
private fun deleteMessage(usageCount: Int): String = when (usageCount) {
    0 -> stringResource(R.string.exercise_delete_confirm)
    else -> stringResource(
        R.string.workout_summary,
        pluralStringResource(R.plurals.count_exercise_usages, usageCount, usageCount),
        stringResource(R.string.exercise_delete_confirm)
    )
}

@Composable
private fun EditorContent(state: ExerciseEditorUiState, actions: ExerciseEditorActions) {
    val draft = state.draft

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = FitterTheme.spacing.screenHorizontal,
            end = FitterTheme.spacing.screenHorizontal,
            top = FitterTheme.spacing.md,
            bottom = FitterTheme.spacing.xxxl
        ),
        verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.lg)
    ) {
        if (!state.isEditable) {
            item(key = "readonly") {
                FitterCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.exercise_readonly),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item(key = "name") {
            FitterTextField(
                value = draft.name,
                onValueChange = actions.onNameChange,
                label = stringResource(R.string.exercise_name_label)
            )
        }

        item(key = "icon") {
            IconPicker(
                selected = draft.icon,
                enabled = state.isEditable,
                onSelect = actions.onIconChange
            )
        }

        item(key = "bodyParts") {
            TagSection(
                title = stringResource(R.string.exercise_body_parts_label),
                options = state.facets.bodyParts,
                selected = draft.bodyParts,
                enabled = state.isEditable,
                onToggle = actions.onToggleBodyPart
            )
        }

        item(key = "targetMuscles") {
            TagSection(
                title = stringResource(R.string.exercise_target_muscles_label),
                options = state.facets.muscles,
                selected = draft.targetMuscles,
                enabled = state.isEditable,
                onToggle = actions.onToggleTargetMuscle
            )
        }

        item(key = "secondaryMuscles") {
            TagSection(
                title = stringResource(R.string.exercise_secondary_muscles_label),
                options = state.facets.muscles,
                selected = draft.secondaryMuscles,
                enabled = state.isEditable,
                onToggle = actions.onToggleSecondaryMuscle
            )
        }

        item(key = "equipment") {
            TagSection(
                title = stringResource(R.string.exercise_equipment_label),
                options = state.facets.equipments,
                selected = draft.equipments,
                enabled = state.isEditable,
                onToggle = actions.onToggleEquipment
            )
        }

        item(key = "instructionsHeader") {
            SectionHeader(title = stringResource(R.string.exercise_instructions_label))
        }

        items(
            count = draft.instructions.size,
            key = { index -> "step$index" }
        ) { index ->
            InstructionRow(
                index = index,
                text = draft.instructions[index],
                enabled = state.isEditable,
                onChange = actions.onInstructionChange,
                onRemove = actions.onRemoveInstruction
            )
        }

        if (state.isEditable) {
            item(key = "instructionsAdd") {
                FitterTextButton(
                    text = stringResource(R.string.exercise_add_instruction),
                    onClick = actions.onAddInstruction
                )
            }

            item(key = "save") {
                FitterPrimaryButton(
                    text = stringResource(R.string.action_save),
                    onClick = actions.onSave,
                    enabled = state.canSave,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun IconPicker(
    selected: ExerciseIcon,
    enabled: Boolean,
    onSelect: (ExerciseIcon) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)) {
        SectionHeader(title = stringResource(R.string.exercise_icon_label))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)) {
            items(ExerciseIcon.entries, key = ExerciseIcon::name) { icon ->
                FitterCard(
                    onClick = when {
                        enabled -> ({ onSelect(icon) })
                        else -> null
                    },
                    selected = icon == selected,
                    contentPadding = PaddingValues(FitterTheme.spacing.xs)
                ) {
                    ExerciseIconTile(
                        icon = icon,
                        accent = when {
                            icon == selected -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

/**
 * Bloque de etiquetas de un catalogo abierto.
 *
 * Las opciones conocidas se muestran como chips y las nuevas se escriben debajo: al confirmar,
 * la etiqueta queda marcada y pasa a formar parte del vocabulario.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagSection(
    title: String,
    options: List<String>,
    selected: List<String>,
    enabled: Boolean,
    onToggle: (String) -> Unit
) {
    // Lo que el usuario acaba de escribir aun no esta en las facetas, asi que se muestra aparte.
    val extra = selected.filterNot(options::contains)
    var newTag by rememberSaveable(title) { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)) {
        SectionHeader(title = title)

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)
        ) {
            (options + extra).forEach { option ->
                FitterFilterChip(
                    label = option,
                    selected = selected.contains(option),
                    onClick = {
                        if (enabled) onToggle(option)
                    }
                )
            }
        }

        if (enabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FitterTextField(
                    value = newTag,
                    onValueChange = { newTag = it },
                    label = stringResource(R.string.exercise_new_tag_hint),
                    modifier = Modifier.weight(1f)
                )
                FitterIconButton(
                    icon = FitterIcons.Check,
                    contentDescription = stringResource(R.string.exercise_new_tag_label),
                    onClick = {
                        val value = newTag.trim()
                        newTag = ""
                        if (value.isNotEmpty() && !selected.contains(value)) onToggle(value)
                    },
                    enabled = newTag.isNotBlank()
                )
            }
        }
    }
}

@Composable
private fun InstructionRow(
    index: Int,
    text: String,
    enabled: Boolean,
    onChange: (Int, String) -> Unit,
    onRemove: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FitterTextField(
            value = text,
            onValueChange = { value -> onChange(index, value) },
            label = stringResource(R.string.exercise_instruction_step, index + 1),
            modifier = Modifier.weight(1f),
            singleLine = false
        )
        if (enabled) {
            FitterIconButton(
                icon = FitterIcons.Close,
                contentDescription = stringResource(R.string.cd_remove),
                onClick = { onRemove(index) }
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
