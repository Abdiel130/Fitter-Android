package com.acdev.fitter.ui.feature.training.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acdev.fitter.domain.model.ExerciseSummary
import com.acdev.fitter.domain.model.PlannedSet
import com.acdev.fitter.domain.model.WorkoutDetail
import com.acdev.fitter.domain.repository.ExerciseRepository
import com.acdev.fitter.domain.repository.UserRepository
import com.acdev.fitter.domain.repository.WorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Texto que el usuario esta escribiendo, antes de que llegue a la base. */
data class WorkoutDraft(
    val name: String = "",
    val notes: String = ""
)

/** Filtro del selector de ejercicios, que vive en la pantalla y no en el catalogo. */
data class ExercisePickerState(
    val query: String = "",
    val bodyPart: String? = null,
    val bodyParts: List<String> = emptyList(),
    val catalog: List<ExerciseSummary> = emptyList()
) {
    /**
     * Catalogo ya filtrado. Se calcula aqui y no en SQL porque la busqueda cambia con cada tecla
     * y el catalogo cabe de sobra en memoria.
     */
    val visible: List<ExerciseSummary>
        get() = catalog.filter { exercise ->
            val matchesQuery = query.isBlank() || exercise.name.contains(query, ignoreCase = true)
            val matchesPart = bodyPart == null || exercise.bodyParts.contains(bodyPart)
            matchesQuery && matchesPart
        }
}

/** Estado del editor de una lista de ejercicios. */
data class WorkoutEditorUiState(
    val isLoading: Boolean = true,
    val workout: WorkoutDetail? = null,
    val draft: WorkoutDraft = WorkoutDraft(),
    val picker: ExercisePickerState = ExercisePickerState()
)

/**
 * Editor de una lista de ejercicios.
 *
 * Es la pantalla con mas acciones de la app, asi que todas siguen la misma forma: la pantalla
 * solo dice que ha pasado y el repositorio decide como queda el orden y las series.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class WorkoutEditorViewModel(
    private val workoutId: String,
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val draft = MutableStateFlow(WorkoutDraft())
    private var draftLoaded = false

    private val query = MutableStateFlow("")
    private val bodyPart = MutableStateFlow<String?>(null)

    private val picker: StateFlow<ExercisePickerState> = combine(
        userRepository.currentUserId.flatMapLatest { userId ->
            when (userId) {
                null -> flowOf(emptyList())
                else -> exerciseRepository.observeCatalog(userId)
            }
        },
        exerciseRepository.observeFacets(),
        query,
        bodyPart
    ) { catalog, facets, currentQuery, currentPart ->
        ExercisePickerState(
            query = currentQuery,
            bodyPart = currentPart,
            bodyParts = facets.bodyParts,
            catalog = catalog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = ExercisePickerState()
    )

    val uiState: StateFlow<WorkoutEditorUiState> = combine(
        workoutRepository.observeWorkout(workoutId).onEach(::seedDraft),
        draft,
        picker
    ) { workout, currentDraft, pickerState ->
        WorkoutEditorUiState(
            isLoading = false,
            workout = workout,
            draft = currentDraft,
            picker = pickerState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = WorkoutEditorUiState()
    )

    init {
        draft
            .drop(1)
            .debounce(PERSIST_DELAY_MILLIS)
            .distinctUntilChanged()
            .onEach { value ->
                workoutRepository.updateWorkout(workoutId, value.name, value.notes)
            }
            .launchIn(viewModelScope)
    }

    /** El borrador se rellena una sola vez: despues manda lo que el usuario escribe. */
    private fun seedDraft(workout: WorkoutDetail?) {
        if (draftLoaded || workout == null) return
        draftLoaded = true
        draft.value = WorkoutDraft(workout.name, workout.notes.orEmpty())
    }

    fun updateName(value: String) {
        draft.value = draft.value.copy(name = value)
    }

    fun updateNotes(value: String) {
        draft.value = draft.value.copy(notes = value)
    }

    fun search(value: String) {
        query.value = value
    }

    /** Volver a pulsar la misma zona quita el filtro: es el gesto que se espera de un chip. */
    fun filterByBodyPart(value: String?) {
        bodyPart.value = when (value) {
            bodyPart.value -> null
            else -> value
        }
    }

    fun addExercises(exerciseIds: List<String>) {
        viewModelScope.launch { workoutRepository.addExercises(workoutId, exerciseIds) }
    }

    fun removeExercise(workoutExerciseId: String) {
        viewModelScope.launch { workoutRepository.removeExercise(workoutId, workoutExerciseId) }
    }

    fun moveExercise(workoutExerciseId: String, offset: Int) {
        viewModelScope.launch {
            workoutRepository.moveExercise(workoutId, workoutExerciseId, offset)
        }
    }

    fun updateRest(workoutExerciseId: String, restSeconds: Int) {
        viewModelScope.launch {
            workoutRepository.updateExerciseRest(workoutExerciseId, restSeconds)
        }
    }

    fun updateExerciseNotes(workoutExerciseId: String, notes: String) {
        viewModelScope.launch {
            workoutRepository.updateExerciseNotes(workoutExerciseId, notes.ifBlank { null })
        }
    }

    fun addSet(workoutExerciseId: String) {
        viewModelScope.launch { workoutRepository.addSet(workoutExerciseId) }
    }

    fun updateSet(workoutExerciseId: String, set: PlannedSet) {
        viewModelScope.launch { workoutRepository.updateSet(workoutExerciseId, set) }
    }

    fun applySetToAll(workoutExerciseId: String, template: PlannedSet) {
        viewModelScope.launch { workoutRepository.applySetToAll(workoutExerciseId, template) }
    }

    fun removeSet(workoutExerciseId: String, setId: String) {
        viewModelScope.launch { workoutRepository.removeSet(workoutExerciseId, setId) }
    }

    fun delete() {
        viewModelScope.launch { workoutRepository.deleteWorkout(workoutId) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val PERSIST_DELAY_MILLIS = 400L
    }
}
