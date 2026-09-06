package com.acdev.fitter.ui.feature.training.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acdev.fitter.domain.model.ActiveRoutineLock
import com.acdev.fitter.domain.model.RoutineDetail
import com.acdev.fitter.domain.model.WorkoutSummary
import com.acdev.fitter.domain.repository.RoutineRepository
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Texto que el usuario esta escribiendo, antes de que llegue a la base. */
data class RoutineDraft(
    val title: String = "",
    val description: String = ""
)

/** Estado del editor de rutina. */
data class RoutineEditorUiState(
    val isLoading: Boolean = true,
    val routine: RoutineDetail? = null,
    val draft: RoutineDraft = RoutineDraft(),
    val lock: ActiveRoutineLock = ActiveRoutineLock(),
    val availableWorkouts: List<WorkoutSummary> = emptyList()
)

/**
 * Editor de una rutina: nombre, secuencia de listas y activacion.
 *
 * El texto se guarda solo, con una pausa: escribir un titulo no debe provocar una escritura por
 * pulsacion, pero tampoco obligar a buscar un boton de guardar.
 */
@OptIn(FlowPreview::class)
class RoutineEditorViewModel(
    private val routineId: String,
    private val userRepository: UserRepository,
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val draft = MutableStateFlow(RoutineDraft())
    private var draftLoaded = false

    private val currentUserId: StateFlow<String?> = userRepository.currentUserId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<RoutineEditorUiState> = combine(
        routineRepository.observeRoutine(routineId).onEach(::seedDraft),
        draft,
        userRepository.currentUserId.flatMapLatest { userId ->
            when (userId) {
                null -> flowOf(ActiveRoutineLock())
                else -> routineRepository.observeActiveRoutineLock(userId)
            }
        },
        userRepository.currentUserId.flatMapLatest { userId ->
            when (userId) {
                null -> flowOf(emptyList())
                else -> workoutRepository.observeWorkouts(userId)
            }
        }
    ) { routine, currentDraft, lock, workouts ->
        RoutineEditorUiState(
            isLoading = false,
            routine = routine,
            draft = currentDraft,
            lock = lock,
            availableWorkouts = workouts
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = RoutineEditorUiState()
    )

    init {
        draft
            .drop(1)
            .debounce(PERSIST_DELAY_MILLIS)
            .distinctUntilChanged()
            .onEach { value ->
                routineRepository.updateRoutine(routineId, value.title, value.description)
            }
            .launchIn(viewModelScope)
    }

    /** El borrador se rellena una sola vez: despues manda lo que el usuario escribe. */
    private fun seedDraft(routine: RoutineDetail?) {
        if (draftLoaded || routine == null) return
        draftLoaded = true
        draft.value = RoutineDraft(routine.title, routine.description.orEmpty())
    }

    fun updateTitle(value: String) {
        draft.value = draft.value.copy(title = value)
    }

    fun updateDescription(value: String) {
        draft.value = draft.value.copy(description = value)
    }

    fun activate() {
        val userId = currentUserId.value ?: return
        viewModelScope.launch { routineRepository.activateRoutine(userId, routineId) }
    }

    fun addWorkouts(workoutIds: List<String>) {
        viewModelScope.launch { routineRepository.addWorkoutsToRoutine(routineId, workoutIds) }
    }

    fun removeEntry(entryId: String) {
        viewModelScope.launch { routineRepository.removeRoutineEntry(routineId, entryId) }
    }

    fun moveEntry(entryId: String, offset: Int) {
        viewModelScope.launch { routineRepository.moveRoutineEntry(routineId, entryId, offset) }
    }

    fun delete() {
        viewModelScope.launch { routineRepository.deleteRoutine(routineId) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val PERSIST_DELAY_MILLIS = 400L
    }
}
