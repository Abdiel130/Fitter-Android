package com.acdev.fitter.data.repository

import com.acdev.fitter.core.time.AppClock
import com.acdev.fitter.core.util.IdGenerator
import com.acdev.fitter.data.local.dao.TrainingDao
import com.acdev.fitter.data.local.dao.WorkoutExerciseRow
import com.acdev.fitter.data.local.dao.WorkoutSetRow
import com.acdev.fitter.data.local.dao.WorkoutSummaryRow
import com.acdev.fitter.data.local.entity.SyncMetadata
import com.acdev.fitter.data.local.entity.WorkoutEntity
import com.acdev.fitter.data.local.entity.WorkoutExerciseEntity
import com.acdev.fitter.data.local.entity.WorkoutSetEntity
import com.acdev.fitter.domain.model.ExerciseIcon
import com.acdev.fitter.domain.model.PlannedSet
import com.acdev.fitter.domain.model.SetType
import com.acdev.fitter.domain.model.WorkoutDetail
import com.acdev.fitter.domain.model.WorkoutExerciseItem
import com.acdev.fitter.domain.model.WorkoutSummary
import com.acdev.fitter.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant

/** Series con las que nace un ejercicio recien anadido: utilizable sin configurar nada. */
private const val DEFAULT_SET_COUNT = 3
private const val DEFAULT_REP_INI = 8
private const val DEFAULT_REP_END = 12
private const val DEFAULT_REST_SECONDS = 90
private const val FIRST_ORDER = 1

/** Cuantos nombres de ejercicio se muestran en la tarjeta de una lista. */
private const val PREVIEW_LIMIT = 3

class WorkoutRepositoryImpl(
    private val trainingDao: TrainingDao,
    private val clock: AppClock,
    private val idGenerator: IdGenerator
) : WorkoutRepository {

    override fun observeWorkouts(userId: String): Flow<List<WorkoutSummary>> =
        trainingDao.observeWorkouts(userId).map { rows -> rows.map(WorkoutSummaryRow::toDomain) }

    override fun observeWorkout(workoutId: String): Flow<WorkoutDetail?> = combine(
        trainingDao.observeWorkout(workoutId),
        trainingDao.observeWorkoutExercises(workoutId),
        trainingDao.observeWorkoutSets(workoutId)
    ) { workout, exercises, sets ->
        workout?.let {
            val setsByExercise = sets.groupBy(WorkoutSetRow::workoutExerciseId)
            WorkoutDetail(
                id = it.id,
                name = it.name,
                notes = it.notes,
                exercises = exercises.map { row ->
                    row.toDomain(setsByExercise[row.id].orEmpty().map(WorkoutSetRow::toDomain))
                }
            )
        }
    }

    override suspend fun createWorkout(userId: String, name: String): String {
        val now = clock.now()
        val workoutId = idGenerator.newId()
        trainingDao.upsertWorkout(
            WorkoutEntity(
                id = workoutId,
                userId = userId,
                name = name,
                createdAt = now,
                sync = SyncMetadata(updatedAt = now)
            )
        )
        return workoutId
    }

    override suspend fun updateWorkout(workoutId: String, name: String, notes: String?) {
        val workout = trainingDao.getWorkout(workoutId) ?: return
        val now = clock.now()
        trainingDao.upsertWorkout(
            workout.copy(
                name = name,
                notes = notes?.takeIf(String::isNotBlank),
                sync = workout.sync.pending(now)
            )
        )
    }

    /** Borrar una lista la retira tambien de las rutinas que la usaban. */
    override suspend fun deleteWorkout(workoutId: String) {
        val now = clock.now()
        trainingDao.softDeleteRoutineWorkoutsOf(workoutId, now)
        trainingDao.softDeleteWorkout(workoutId, now)
    }

    override suspend fun duplicateWorkout(
        userId: String,
        workoutId: String,
        name: String
    ): String {
        val source = trainingDao.getWorkout(workoutId) ?: return workoutId
        val now = clock.now()
        val copyId = idGenerator.newId()

        trainingDao.upsertWorkout(
            WorkoutEntity(
                id = copyId,
                userId = userId,
                name = name,
                notes = source.notes,
                createdAt = now,
                sync = SyncMetadata(updatedAt = now)
            )
        )

        val copiedExercises = trainingDao.getWorkoutExercises(workoutId).map { exercise ->
            exercise to exercise.copy(
                id = idGenerator.newId(),
                workoutId = copyId,
                sync = SyncMetadata(updatedAt = now)
            )
        }
        trainingDao.upsertWorkoutExercises(copiedExercises.map { it.second })

        val copiedSets = copiedExercises.flatMap { (original, copy) ->
            trainingDao.getSets(original.id).map { set ->
                set.copy(
                    id = idGenerator.newId(),
                    workoutExerciseId = copy.id,
                    sync = SyncMetadata(updatedAt = now)
                )
            }
        }
        trainingDao.upsertWorkoutSets(copiedSets)
        return copyId
    }

    override suspend fun addExercises(workoutId: String, exerciseIds: List<String>) {
        if (exerciseIds.isEmpty()) return
        val now = clock.now()
        val nextOrder = trainingDao.getWorkoutExercises(workoutId)
            .maxOfOrNull { it.orderInRoutine }
            ?.plus(1)
            ?: FIRST_ORDER

        val added = exerciseIds.mapIndexed { offset, exerciseId ->
            WorkoutExerciseEntity(
                id = idGenerator.newId(),
                workoutId = workoutId,
                exerciseId = exerciseId,
                orderInRoutine = nextOrder + offset,
                restSeconds = DEFAULT_REST_SECONDS,
                sync = SyncMetadata(updatedAt = now)
            )
        }
        trainingDao.upsertWorkoutExercises(added)
        trainingDao.upsertWorkoutSets(
            added.flatMap { exercise ->
                (FIRST_ORDER..DEFAULT_SET_COUNT).map { order ->
                    defaultSet(exercise.id, order, now)
                }
            }
        )
    }

    override suspend fun removeExercise(workoutId: String, workoutExerciseId: String) {
        val now = clock.now()
        trainingDao.softDeleteWorkoutExercise(workoutExerciseId, now)
        trainingDao.upsertWorkoutExercises(
            trainingDao.getWorkoutExercises(workoutId).mapIndexed { index, exercise ->
                exercise.copy(
                    orderInRoutine = index + FIRST_ORDER,
                    sync = exercise.sync.pending(now)
                )
            }
        )
    }

    override suspend fun moveExercise(
        workoutId: String,
        workoutExerciseId: String,
        offset: Int
    ) {
        val exercises = trainingDao.getWorkoutExercises(workoutId)
        val from = exercises.indexOfFirst { it.id == workoutExerciseId }
        val to = from + offset
        if (from < 0 || to !in exercises.indices) return

        val now = clock.now()
        val reordered = exercises.toMutableList().apply { add(to, removeAt(from)) }
        trainingDao.upsertWorkoutExercises(
            reordered.mapIndexed { index, exercise ->
                exercise.copy(
                    orderInRoutine = index + FIRST_ORDER,
                    sync = exercise.sync.pending(now)
                )
            }
        )
    }

    override suspend fun updateExerciseRest(workoutExerciseId: String, restSeconds: Int) {
        val exercise = trainingDao.getWorkoutExercise(workoutExerciseId) ?: return
        val now = clock.now()
        trainingDao.upsertWorkoutExercises(
            listOf(
                exercise.copy(
                    restSeconds = restSeconds.coerceAtLeast(0),
                    sync = exercise.sync.pending(now)
                )
            )
        )
    }

    override suspend fun updateExerciseNotes(workoutExerciseId: String, notes: String?) {
        val exercise = trainingDao.getWorkoutExercise(workoutExerciseId) ?: return
        val now = clock.now()
        trainingDao.upsertWorkoutExercises(
            listOf(
                exercise.copy(
                    notes = notes?.takeIf(String::isNotBlank),
                    sync = exercise.sync.pending(now)
                )
            )
        )
    }

    /** La serie nueva copia la ultima: anadir la cuarta serie de un 8-12 no deberia reconfigurarse. */
    override suspend fun addSet(workoutExerciseId: String) {
        val now = clock.now()
        val existing = trainingDao.getSets(workoutExerciseId)
        val last = existing.lastOrNull()
        val order = (last?.setOrder ?: 0) + 1

        val set = when (last) {
            null -> defaultSet(workoutExerciseId, order, now)
            else -> last.copy(
                id = idGenerator.newId(),
                setOrder = order,
                sync = SyncMetadata(updatedAt = now)
            )
        }
        trainingDao.upsertWorkoutSets(listOf(set))
    }

    override suspend fun updateSet(workoutExerciseId: String, set: PlannedSet) {
        val stored = trainingDao.getSet(set.id) ?: return
        val now = clock.now()
        trainingDao.upsertWorkoutSets(listOf(stored.applying(set, now)))
    }

    override suspend fun applySetToAll(workoutExerciseId: String, template: PlannedSet) {
        val now = clock.now()
        trainingDao.upsertWorkoutSets(
            trainingDao.getSets(workoutExerciseId).map { stored -> stored.applying(template, now) }
        )
    }

    override suspend fun removeSet(workoutExerciseId: String, setId: String) {
        val now = clock.now()
        trainingDao.softDeleteSet(setId, now)
        trainingDao.upsertWorkoutSets(
            trainingDao.getSets(workoutExerciseId).mapIndexed { index, set ->
                set.copy(setOrder = index + FIRST_ORDER, sync = set.sync.pending(now))
            }
        )
    }

    private fun defaultSet(workoutExerciseId: String, order: Int, now: Instant) = WorkoutSetEntity(
        id = idGenerator.newId(),
        workoutExerciseId = workoutExerciseId,
        setOrder = order,
        type = SetType.NORMAL,
        rangeRepIni = DEFAULT_REP_INI,
        rangeRepEnd = DEFAULT_REP_END,
        sync = SyncMetadata(updatedAt = now)
    )
}

/** Copia la configuracion de una serie sobre otra sin tocar su identidad ni su posicion. */
private fun WorkoutSetEntity.applying(source: PlannedSet, now: Instant): WorkoutSetEntity = copy(
    type = source.type,
    rangeRepIni = source.repIni,
    rangeRepEnd = source.repEnd,
    targetRpe = source.targetRpe,
    restSeconds = source.restSeconds,
    sync = sync.pending(now)
)

private fun WorkoutSummaryRow.toDomain(): WorkoutSummary = WorkoutSummary(
    id = id,
    name = name,
    exerciseCount = exerciseCount,
    setCount = setCount,
    routineCount = routineCount,
    previewExercises = preview
        ?.split(',')
        ?.map(String::trim)
        ?.filter(String::isNotEmpty)
        ?.take(PREVIEW_LIMIT)
        .orEmpty()
)

private fun WorkoutExerciseRow.toDomain(sets: List<PlannedSet>): WorkoutExerciseItem =
    WorkoutExerciseItem(
        id = id,
        exerciseId = exerciseId,
        name = name,
        icon = ExerciseIcon.fromKey(iconKey),
        targetMuscle = targetMuscle,
        orderInRoutine = orderInRoutine,
        restSeconds = restSeconds,
        notes = notes,
        sets = sets
    )

private fun WorkoutSetRow.toDomain(): PlannedSet = PlannedSet(
    id = id,
    setOrder = setOrder,
    type = type,
    repIni = rangeRepIni,
    repEnd = rangeRepEnd,
    targetRpe = targetRpe,
    restSeconds = restSeconds
)
