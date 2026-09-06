package com.acdev.fitter.data.repository

import com.acdev.fitter.core.time.AppClock
import com.acdev.fitter.core.time.weekStart
import com.acdev.fitter.core.time.weekStartMillis
import com.acdev.fitter.core.util.IdGenerator
import com.acdev.fitter.data.local.dao.RoutineSummaryRow
import com.acdev.fitter.data.local.dao.RoutineWorkoutRow
import com.acdev.fitter.data.local.dao.TrainingDao
import com.acdev.fitter.data.local.dao.UserDao
import com.acdev.fitter.data.local.entity.RoutineEntity
import com.acdev.fitter.data.local.entity.RoutineWorkoutEntity
import com.acdev.fitter.data.local.entity.SyncMetadata
import com.acdev.fitter.domain.model.ActiveRoutineLock
import com.acdev.fitter.domain.model.RoutineDetail
import com.acdev.fitter.domain.model.RoutineSummary
import com.acdev.fitter.domain.model.RoutineWorkoutItem
import com.acdev.fitter.domain.model.SyncState
import com.acdev.fitter.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant

class RoutineRepositoryImpl(
    private val trainingDao: TrainingDao,
    private val userDao: UserDao,
    private val clock: AppClock,
    private val idGenerator: IdGenerator
) : RoutineRepository {

    override fun observeRoutines(userId: String): Flow<List<RoutineSummary>> =
        trainingDao.observeRoutines(userId).map { rows -> rows.map(RoutineSummaryRow::toDomain) }

    override fun observeRoutine(routineId: String): Flow<RoutineDetail?> = combine(
        trainingDao.observeRoutine(routineId),
        trainingDao.observeRoutineWorkouts(routineId)
    ) { routine, entries ->
        routine?.let {
            RoutineDetail(
                id = it.id,
                title = it.title,
                description = it.description,
                isActive = it.isActive,
                workouts = entries.map(RoutineWorkoutRow::toDomain)
            )
        }
    }

    /**
     * El limite de la semana se resuelve al suscribirse, no en cada emision: dentro de una misma
     * sesion de pantalla la referencia no debe moverse bajo los pies del usuario.
     */
    override fun observeActiveRoutineLock(userId: String): Flow<ActiveRoutineLock> {
        val weekStart = clock.weekStart()
        return trainingDao.observeActiveRoutineSessionsSince(userId, clock.weekStartMillis())
            .map { sessions ->
                ActiveRoutineLock(
                    isLocked = sessions > 0,
                    sessionsThisWeek = sessions,
                    unlocksOn = weekStart.plusWeeks(1).takeIf { sessions > 0 }
                )
            }
    }

    override suspend fun createRoutine(userId: String, title: String): String {
        val now = clock.now()
        val routineId = idGenerator.newId()
        trainingDao.upsertRoutine(
            RoutineEntity(
                id = routineId,
                userId = userId,
                title = title,
                isActive = false,
                createdAt = now,
                sync = SyncMetadata(updatedAt = now)
            )
        )
        // La primera rutina se activa sola: sin ella el dashboard no tiene siguiente sesion.
        if (trainingDao.observeActiveRoutine(userId).first() == null) {
            activateRoutine(userId, routineId)
        }
        return routineId
    }

    override suspend fun updateRoutine(routineId: String, title: String, description: String?) {
        val routine = trainingDao.getRoutine(routineId) ?: return
        val now = clock.now()
        trainingDao.upsertRoutine(
            routine.copy(
                title = title,
                description = description?.takeIf(String::isNotBlank),
                sync = routine.sync.pending(now)
            )
        )
    }

    override suspend fun deleteRoutine(routineId: String) {
        val routine = trainingDao.getRoutine(routineId) ?: return
        if (routine.isActive && isLocked(routine.userId)) return

        val now = clock.now()
        trainingDao.getRoutineWorkouts(routineId).forEach { entry ->
            trainingDao.softDeleteRoutineWorkout(entry.id, now)
        }
        trainingDao.softDeleteRoutine(routineId, now)

        if (routine.isActive) {
            userDao.updateActiveRoutine(routine.userId, routineId = null, sequenceIndex = 1)
        }
    }

    override suspend fun duplicateRoutine(
        userId: String,
        routineId: String,
        title: String
    ): String {
        val source = trainingDao.getRoutine(routineId) ?: return routineId
        val now = clock.now()
        val copyId = idGenerator.newId()

        trainingDao.upsertRoutine(
            RoutineEntity(
                id = copyId,
                userId = userId,
                title = title,
                description = source.description,
                isActive = false,
                createdAt = now,
                sync = SyncMetadata(updatedAt = now)
            )
        )
        trainingDao.upsertRoutineWorkouts(
            trainingDao.getRoutineWorkouts(routineId).map { entry ->
                entry.copy(
                    id = idGenerator.newId(),
                    routineId = copyId,
                    sync = SyncMetadata(updatedAt = now)
                )
            }
        )
        return copyId
    }

    /**
     * La regla semanal se comprueba aqui y no en la pantalla: cualquier otra via de entrada
     * (un atajo, una prueba, un futuro widget) queda sujeta a la misma restriccion.
     */
    override suspend fun activateRoutine(userId: String, routineId: String) {
        if (isLocked(userId)) return

        val now = clock.now()
        trainingDao.deactivateRoutines(userId, now)
        trainingDao.activateRoutine(routineId, now)
        userDao.updateActiveRoutine(userId, routineId, sequenceIndex = 1)
    }

    override suspend fun addWorkoutsToRoutine(routineId: String, workoutIds: List<String>) {
        if (workoutIds.isEmpty()) return
        val now = clock.now()
        val nextOrder = trainingDao.getRoutineWorkouts(routineId)
            .maxOfOrNull { it.orderIndex }
            ?.plus(1)
            ?: FIRST_ORDER

        trainingDao.upsertRoutineWorkouts(
            workoutIds.mapIndexed { offset, workoutId ->
                RoutineWorkoutEntity(
                    id = idGenerator.newId(),
                    routineId = routineId,
                    workoutId = workoutId,
                    orderIndex = nextOrder + offset,
                    sync = SyncMetadata(updatedAt = now)
                )
            }
        )
    }

    override suspend fun removeRoutineEntry(routineId: String, entryId: String) {
        val now = clock.now()
        trainingDao.softDeleteRoutineWorkout(entryId, now)
        renumber(routineId, now)
    }

    override suspend fun moveRoutineEntry(routineId: String, entryId: String, offset: Int) {
        val entries = trainingDao.getRoutineWorkouts(routineId)
        val from = entries.indexOfFirst { it.id == entryId }
        val to = from + offset
        if (from < 0 || to !in entries.indices) return

        val now = clock.now()
        val reordered = entries.toMutableList().apply { add(to, removeAt(from)) }
        trainingDao.upsertRoutineWorkouts(
            reordered.mapIndexed { index, entry ->
                entry.copy(orderIndex = index + FIRST_ORDER, sync = entry.sync.pending(now))
            }
        )
    }

    /** Deja la secuencia contigua tras un borrado, para que el puntero del usuario siga valiendo. */
    private suspend fun renumber(routineId: String, now: Instant) {
        trainingDao.upsertRoutineWorkouts(
            trainingDao.getRoutineWorkouts(routineId).mapIndexed { index, entry ->
                entry.copy(orderIndex = index + FIRST_ORDER, sync = entry.sync.pending(now))
            }
        )
    }

    private suspend fun isLocked(userId: String): Boolean =
        trainingDao.observeActiveRoutineSessionsSince(userId, clock.weekStartMillis()).first() > 0

    private companion object {
        const val FIRST_ORDER = 1
    }
}

/** Marca la fila como pendiente de subir sin perder el estado de borrado logico. */
internal fun SyncMetadata.pending(now: Instant): SyncMetadata =
    copy(syncState = SyncState.PENDING, updatedAt = now)

private fun RoutineSummaryRow.toDomain(): RoutineSummary = RoutineSummary(
    id = id,
    title = title,
    description = description,
    isActive = isActive,
    workoutCount = workoutCount,
    exerciseCount = exerciseCount,
    setCount = setCount
)

private fun RoutineWorkoutRow.toDomain(): RoutineWorkoutItem = RoutineWorkoutItem(
    entryId = entryId,
    workoutId = workoutId,
    name = name,
    orderIndex = orderIndex,
    exerciseCount = exerciseCount,
    setCount = setCount
)
