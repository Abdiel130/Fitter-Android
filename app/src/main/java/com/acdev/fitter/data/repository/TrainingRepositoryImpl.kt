package com.acdev.fitter.data.repository

import com.acdev.fitter.core.time.AppClock
import com.acdev.fitter.core.util.IdGenerator
import com.acdev.fitter.data.local.dao.ExerciseDao
import com.acdev.fitter.data.local.dao.NextSessionRow
import com.acdev.fitter.data.local.dao.TrainingDao
import com.acdev.fitter.data.local.dao.UserDao
import com.acdev.fitter.data.local.dao.WorkoutLogDao
import com.acdev.fitter.data.local.entity.BodyPartEntity
import com.acdev.fitter.data.local.entity.EquipmentEntity
import com.acdev.fitter.data.local.entity.ExerciseBodyPartCrossRef
import com.acdev.fitter.data.local.entity.ExerciseEntity
import com.acdev.fitter.data.local.entity.ExerciseEquipmentCrossRef
import com.acdev.fitter.data.local.entity.ExerciseMuscleCrossRef
import com.acdev.fitter.data.local.entity.MuscleEntity
import com.acdev.fitter.data.local.entity.RoutineEntity
import com.acdev.fitter.data.local.entity.RoutineWorkoutEntity
import com.acdev.fitter.data.local.entity.SyncMetadata
import com.acdev.fitter.data.local.entity.WorkoutEntity
import com.acdev.fitter.data.local.entity.WorkoutExerciseEntity
import com.acdev.fitter.data.local.entity.WorkoutSetEntity
import com.acdev.fitter.data.local.seed.StarterCatalog
import com.acdev.fitter.domain.model.NextSession
import com.acdev.fitter.domain.model.SyncState
import com.acdev.fitter.domain.model.WeeklyVolume
import com.acdev.fitter.domain.repository.TrainingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** Series objetivo por semana. Referencia de volumen para el anillo del dashboard. */
private const val WEEKLY_SET_TARGET = 80

/** Trabajo estimado por serie, en segundos, para calcular la duracion de una sesion. */
private const val SECONDS_OF_WORK_PER_SET = 45

class TrainingRepositoryImpl(
    private val trainingDao: TrainingDao,
    private val exerciseDao: ExerciseDao,
    private val workoutLogDao: WorkoutLogDao,
    private val userDao: UserDao,
    private val clock: AppClock,
    private val idGenerator: IdGenerator
) : TrainingRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeNextSession(userId: String): Flow<NextSession?> =
        userDao.observeCurrentUser().flatMapLatest { user ->
            when (user) {
                null -> flowOf(null)
                else -> trainingDao.observeNextSession(userId, user.activeSequenceIndex)
                    .map { row -> row?.toNextSession() }
            }
        }

    override fun observeWeeklyVolume(userId: String, weekStart: LocalDate): Flow<WeeklyVolume> {
        val since = weekStart.atStartOfDay(clock.zone()).toInstant().toEpochMilli()
        return workoutLogDao.observeEffectiveSetCount(userId, since)
            .map { WeeklyVolume(effectiveSets = it, targetSets = WEEKLY_SET_TARGET) }
    }

    override suspend fun ensureStarterRoutine(userId: String) {
        if (trainingDao.countRoutines(userId) > 0) return

        val exerciseIdsByKey = seedCatalogIfNeeded()
        seedRoutine(userId, exerciseIdsByKey)
    }

    /**
     * Inserta el catalogo de arranque si la base esta vacia y devuelve el id de cada ejercicio
     * indexado por su clave de semilla.
     */
    private suspend fun seedCatalogIfNeeded(): Map<String, String> {
        val now = clock.now()
        val seeds = StarterCatalog.exercises

        exerciseDao.upsertBodyParts(seeds.map { it.bodyPart }.distinct().map { BodyPartEntity(name = it) })
        exerciseDao.upsertMuscles(seeds.map { it.targetMuscle }.distinct().map { MuscleEntity(name = it) })
        exerciseDao.upsertEquipments(seeds.map { it.equipment }.distinct().map { EquipmentEntity(name = it) })

        val idsByKey = seeds.associate { it.key to idGenerator.newId() }

        exerciseDao.upsertExercises(
            seeds.map { seed ->
                ExerciseEntity(
                    id = idsByKey.getValue(seed.key),
                    userId = null,
                    externalId = seed.key,
                    name = seed.name,
                    iconKey = seed.icon.name,
                    createdAt = now,
                    // El catalogo comun no viaja al servidor: nace ya sincronizado.
                    sync = SyncMetadata(syncState = SyncState.SYNCED, updatedAt = now)
                )
            }
        )

        seeds.forEach { seed ->
            val exerciseId = idsByKey.getValue(seed.key)
            exerciseDao.findBodyPartId(seed.bodyPart)?.let {
                exerciseDao.upsertExerciseBodyParts(listOf(ExerciseBodyPartCrossRef(exerciseId, it)))
            }
            exerciseDao.findMuscleId(seed.targetMuscle)?.let {
                exerciseDao.upsertExerciseMuscles(listOf(ExerciseMuscleCrossRef(exerciseId, it, isTarget = true)))
            }
            exerciseDao.findEquipmentId(seed.equipment)?.let {
                exerciseDao.upsertExerciseEquipments(listOf(ExerciseEquipmentCrossRef(exerciseId, it)))
            }
        }
        return idsByKey
    }

    private suspend fun seedRoutine(userId: String, exerciseIdsByKey: Map<String, String>) {
        val now = clock.now()
        val meta = SyncMetadata(updatedAt = now)
        val routineId = idGenerator.newId()

        trainingDao.upsertRoutine(
            RoutineEntity(
                id = routineId,
                userId = userId,
                title = StarterCatalog.ROUTINE_TITLE,
                isActive = true,
                createdAt = now,
                sync = meta
            )
        )

        val workouts = StarterCatalog.workouts.map { seed ->
            seed to WorkoutEntity(
                id = idGenerator.newId(),
                userId = userId,
                name = seed.name,
                createdAt = now,
                sync = meta
            )
        }
        trainingDao.upsertWorkouts(workouts.map { it.second })

        trainingDao.upsertRoutineWorkouts(
            workouts.map { (seed, workout) ->
                RoutineWorkoutEntity(
                    id = idGenerator.newId(),
                    routineId = routineId,
                    workoutId = workout.id,
                    orderIndex = seed.orderIndex,
                    sync = meta
                )
            }
        )

        val workoutExercises = workouts.flatMap { (seedWorkout, workout) ->
            seedWorkout.exercises.mapIndexed { index, seedExercise ->
                seedExercise to WorkoutExerciseEntity(
                    id = idGenerator.newId(),
                    workoutId = workout.id,
                    exerciseId = exerciseIdsByKey.getValue(seedExercise.exerciseKey),
                    orderInRoutine = index + 1,
                    restSeconds = seedExercise.restSeconds,
                    sync = meta
                )
            }
        }
        trainingDao.upsertWorkoutExercises(workoutExercises.map { it.second })

        val sets = workoutExercises.flatMap { (seedExercise, workoutExercise) ->
            (1..seedExercise.sets).map { setOrder ->
                WorkoutSetEntity(
                    id = idGenerator.newId(),
                    workoutExerciseId = workoutExercise.id,
                    setOrder = setOrder,
                    rangeRepIni = seedExercise.repIni,
                    rangeRepEnd = seedExercise.repEnd,
                    targetRpe = seedExercise.targetRpe,
                    restSeconds = seedExercise.restSeconds,
                    sync = meta
                )
            }
        }
        trainingDao.upsertWorkoutSets(sets)

        userDao.updateActiveRoutine(userId, routineId, sequenceIndex = 1)
    }
}

/**
 * Duracion estimada: cada serie cuesta su descanso mas el trabajo. El descanso medio sale de los
 * ejercicios del dia, no de una constante, para que un dia de fuerza estime mas que uno de bombeo.
 */
private fun NextSessionRow.toNextSession(): NextSession {
    val averageRest = if (exerciseCount > 0) restSecondsTotal / exerciseCount else 90
    val estimatedSeconds = setCount * (averageRest + SECONDS_OF_WORK_PER_SET)
    return NextSession(
        workoutId = workoutId,
        name = name,
        routineTitle = routineTitle,
        exerciseCount = exerciseCount,
        setCount = setCount,
        estimatedMinutes = estimatedSeconds / 60
    )
}
