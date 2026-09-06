package com.acdev.fitter.ui.feature.training.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acdev.fitter.domain.model.ExerciseDraft
import com.acdev.fitter.domain.model.ExerciseFacets
import com.acdev.fitter.domain.model.ExerciseIcon
import com.acdev.fitter.domain.repository.ExerciseRepository
import com.acdev.fitter.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Que se ha hecho con el ejercicio, para que la pantalla sepa cuando cerrarse. */
sealed interface ExerciseEditorEvent {
    /** El ejercicio se ha guardado. `id` sirve para devolverlo a quien lo pidio. */
    data class Saved(val id: String) : ExerciseEditorEvent

    data object Deleted : ExerciseEditorEvent
}

/** Estado del editor de un ejercicio. */
data class ExerciseEditorUiState(
    val isLoading: Boolean = true,
    val draft: ExerciseDraft = ExerciseDraft(),
    val facets: ExerciseFacets = ExerciseFacets(),
    /** Los ejercicios del catalogo comun se consultan, no se editan. */
    val isEditable: Boolean = true,
    val isNew: Boolean = true,
    val usageCount: Int = 0,
    val event: ExerciseEditorEvent? = null
) {
    val canSave: Boolean get() = isEditable && draft.name.isNotBlank()
}

/**
 * Editor de un ejercicio propio.
 *
 * Aqui el guardado es explicito, al reves que en rutinas y listas: un ejercicio se usa desde
 * muchas listas, y dejar a medias su clasificacion mientras se escribe seria un mal reparto.
 */
class ExerciseEditorViewModel(
    private val exerciseId: String?,
    private val userRepository: UserRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val draft = MutableStateFlow(ExerciseDraft())
    private val editable = MutableStateFlow(true)
    private val loading = MutableStateFlow(exerciseId != null)
    private val usages = MutableStateFlow(0)
    private val event = MutableStateFlow<ExerciseEditorEvent?>(null)

    val uiState: StateFlow<ExerciseEditorUiState> = combine(
        draft,
        exerciseRepository.observeFacets(),
        combine(editable, loading, ::Pair),
        usages,
        event
    ) { currentDraft, facets, flags, usageCount, currentEvent ->
        ExerciseEditorUiState(
            isLoading = flags.second,
            draft = currentDraft,
            facets = facets,
            isEditable = flags.first,
            isNew = exerciseId == null,
            usageCount = usageCount,
            event = currentEvent
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = ExerciseEditorUiState(isLoading = exerciseId != null)
    )

    init {
        if (exerciseId != null) loadExisting(exerciseId)
    }

    private fun loadExisting(id: String) {
        viewModelScope.launch {
            val detail = exerciseRepository.getDetail(id)
            if (detail != null) {
                draft.value = ExerciseDraft(
                    id = detail.id,
                    name = detail.name,
                    icon = detail.icon,
                    instructions = detail.instructions,
                    bodyParts = detail.bodyParts,
                    targetMuscles = detail.targetMuscles,
                    secondaryMuscles = detail.secondaryMuscles,
                    equipments = detail.equipments
                )
                editable.value = detail.isCustom
                usages.value = exerciseRepository.countUsages(id)
            }
            loading.value = false
        }
    }

    fun updateName(value: String) {
        draft.value = draft.value.copy(name = value)
    }

    fun updateIcon(icon: ExerciseIcon) {
        draft.value = draft.value.copy(icon = icon)
    }

    fun addInstruction() {
        draft.value = draft.value.copy(instructions = draft.value.instructions + "")
    }

    fun updateInstruction(index: Int, value: String) {
        val steps = draft.value.instructions.toMutableList()
        if (index !in steps.indices) return
        steps[index] = value
        draft.value = draft.value.copy(instructions = steps)
    }

    fun removeInstruction(index: Int) {
        val steps = draft.value.instructions.toMutableList()
        if (index !in steps.indices) return
        steps.removeAt(index)
        draft.value = draft.value.copy(instructions = steps)
    }

    fun toggleBodyPart(value: String) {
        draft.value = draft.value.copy(bodyParts = draft.value.bodyParts.toggling(value))
    }

    fun toggleTargetMuscle(value: String) {
        draft.value = draft.value.copy(targetMuscles = draft.value.targetMuscles.toggling(value))
    }

    fun toggleSecondaryMuscle(value: String) {
        draft.value = draft.value.copy(
            secondaryMuscles = draft.value.secondaryMuscles.toggling(value)
        )
    }

    fun toggleEquipment(value: String) {
        draft.value = draft.value.copy(equipments = draft.value.equipments.toggling(value))
    }

    fun save() {
        val current = draft.value
        if (current.name.isBlank() || !editable.value) return
        viewModelScope.launch {
            val userId = userRepository.currentUserId.first() ?: return@launch
            val savedId = exerciseRepository.saveCustomExercise(userId, current)
            event.value = ExerciseEditorEvent.Saved(savedId)
        }
    }

    fun delete() {
        val id = exerciseId ?: return
        viewModelScope.launch {
            exerciseRepository.deleteCustomExercise(id)
            event.value = ExerciseEditorEvent.Deleted
        }
    }

    fun consumeEvent() {
        event.value = null
    }

    /** Marcar y desmarcar es la misma pulsacion: los catalogos se editan con la misma etiqueta. */
    private fun List<String>.toggling(value: String): List<String> = when {
        contains(value) -> this - value
        else -> this + value
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
