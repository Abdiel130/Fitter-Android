package com.acdev.fitter.ui.feature.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acdev.fitter.domain.model.ActiveRoutineLock
import com.acdev.fitter.domain.model.ExerciseFacets
import com.acdev.fitter.domain.model.ExerciseSummary
import com.acdev.fitter.domain.model.RoutineSummary
import com.acdev.fitter.domain.model.WorkoutSummary
import com.acdev.fitter.domain.repository.ExerciseRepository
import com.acdev.fitter.domain.repository.RoutineRepository
import com.acdev.fitter.domain.repository.UserRepository
import com.acdev.fitter.domain.repository.WorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Las tres cosas que se configuran en Entreno, de lo general a lo concreto. */
enum class TrainingTab { ROUTINES, WORKOUTS, EXERCISES }

/** Adonde tiene que ir la navegacion despues de crear algo. */
sealed interface TrainingEvent {
    data class OpenRoutine(val routineId: String) : TrainingEvent
    data class OpenWorkout(val workoutId: String) : TrainingEvent
}

/** Estado del catalogo de ejercicios: lo que se ve tras buscar y filtrar. */
data class ExerciseCatalogState(
    val all: List<ExerciseSummary> = emptyList(),
    val facets: ExerciseFacets = ExerciseFacets(),
    val query: String = "",
    val bodyPart: String? = null
) {
    val visible: List<ExerciseSummary>
        get() = all.filter { it.matches(query) && it.belongsTo(bodyPart) }
}

/** Estado de la pantalla de entreno. */
data class TrainingUiState(
    val isLoading: Boolean = true,
    val tab: TrainingTab = TrainingTab.ROUTINES,
    val routines: List<RoutineSummary> = emptyList(),
    val lock: ActiveRoutineLock = ActiveRoutineLock(),
    val workouts: List<WorkoutSummary> = emptyList(),
    val catalog: ExerciseCatalogState = ExerciseCatalogState()
)

/**
 * Configuracion del entrenamiento: rutinas, listas y catalogo.
 *
 * Las tres pestanas comparten ViewModel porque comparten usuario, bloqueo semanal y catalogo: se
 * pintan siempre juntas y separarlas obligaria a repetir la suscripcion tres veces.
 */
class TrainingViewModel(
    private val userRepository: UserRepository,
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val tab = MutableStateFlow(TrainingTab.ROUTINES)
    private val query = MutableStateFlow("")
    private val bodyPart = MutableStateFlow<String?>(null)

    private val _events = MutableStateFlow<TrainingEvent?>(null)
    val events: StateFlow<TrainingEvent?> = _events.asStateFlow()

    private val currentUserId: StateFlow<String?> = userRepository.currentUserId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TrainingUiState> = userRepository.currentUserId
        .flatMapLatest { userId ->
            when (userId) {
                null -> flowOf(TrainingUiState(isLoading = false))
                else -> stateFor(userId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = TrainingUiState()
        )

    private fun stateFor(userId: String) = combine(
        combine(
            routineRepository.observeRoutines(userId),
            routineRepository.observeActiveRoutineLock(userId),
            workoutRepository.observeWorkouts(userId)
        ) { routines, lock, workouts -> Triple(routines, lock, workouts) },
        combine(
            exerciseRepository.observeCatalog(userId),
            exerciseRepository.observeFacets(),
            query,
            bodyPart
        ) { exercises, facets, text, part ->
            ExerciseCatalogState(all = exercises, facets = facets, query = text, bodyPart = part)
        },
        tab
    ) { (routines, lock, workouts), catalog, selectedTab ->
        TrainingUiState(
            isLoading = false,
            tab = selectedTab,
            routines = routines,
            lock = lock,
            workouts = workouts,
            catalog = catalog
        )
    }

    fun selectTab(value: TrainingTab) {
        tab.value = value
    }

    fun search(text: String) {
        query.value = text
    }

    /** Volver a tocar la zona activa la deselecciona: el filtro es un interruptor, no un modo. */
    fun filterByBodyPart(name: String?) {
        bodyPart.update { current -> name.takeIf { it != current } }
    }

    fun createRoutine(title: String) {
        val userId = currentUserId.value ?: return
        viewModelScope.launch {
            _events.value = TrainingEvent.OpenRoutine(
                routineRepository.createRoutine(userId, title)
            )
        }
    }

    fun duplicateRoutine(routineId: String, title: String) {
        val userId = currentUserId.value ?: return
        viewModelScope.launch {
            _events.value = TrainingEvent.OpenRoutine(
                routineRepository.duplicateRoutine(userId, routineId, title)
            )
        }
    }

    fun activateRoutine(routineId: String) {
        val userId = currentUserId.value ?: return
        viewModelScope.launch { routineRepository.activateRoutine(userId, routineId) }
    }

    fun deleteRoutine(routineId: String) {
        viewModelScope.launch { routineRepository.deleteRoutine(routineId) }
    }

    fun createWorkout(name: String) {
        val userId = currentUserId.value ?: return
        viewModelScope.launch {
            _events.value = TrainingEvent.OpenWorkout(
                workoutRepository.createWorkout(userId, name)
            )
        }
    }

    fun duplicateWorkout(workoutId: String, name: String) {
        val userId = currentUserId.value ?: return
        viewModelScope.launch {
            _events.value = TrainingEvent.OpenWorkout(
                workoutRepository.duplicateWorkout(userId, workoutId, name)
            )
        }
    }

    fun deleteWorkout(workoutId: String) {
        viewModelScope.launch { workoutRepository.deleteWorkout(workoutId) }
    }

    fun consumeEvent() {
        _events.value = null
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

private fun ExerciseSummary.matches(query: String): Boolean {
    if (query.isBlank()) return true
    val needle = query.trim()
    return name.contains(needle, ignoreCase = true) ||
        targetMuscles.any { it.contains(needle, ignoreCase = true) } ||
        equipments.any { it.contains(needle, ignoreCase = true) }
}

private fun ExerciseSummary.belongsTo(bodyPart: String?): Boolean =
    bodyPart == null || bodyParts.contains(bodyPart)
