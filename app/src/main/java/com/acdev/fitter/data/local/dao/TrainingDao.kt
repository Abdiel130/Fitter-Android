package com.acdev.fitter.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.acdev.fitter.data.local.entity.RoutineEntity
import com.acdev.fitter.data.local.entity.RoutineWorkoutEntity
import com.acdev.fitter.data.local.entity.WorkoutEntity
import com.acdev.fitter.data.local.entity.WorkoutExerciseEntity
import com.acdev.fitter.data.local.entity.WorkoutSetEntity
import com.acdev.fitter.domain.model.SetType
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/** Fila plana con lo que el dashboard necesita saber de la proxima sesion. */
data class NextSessionRow(
    val workoutId: String,
    val name: String,
    val orderIndex: Int,
    val routineTitle: String,
    val exerciseCount: Int,
    val setCount: Int,
    val restSecondsTotal: Int
)

/** Rutina con sus recuentos resueltos en SQL, para pintar la lista sin recorrer nada en Kotlin. */
data class RoutineSummaryRow(
    val id: String,
    val title: String,
    val description: String?,
    val isActive: Boolean,
    val workoutCount: Int,
    val exerciseCount: Int,
    val setCount: Int
)

/** Entrada de una rutina: la fila puente mas los datos de la lista a la que apunta. */
data class RoutineWorkoutRow(
    val entryId: String,
    val workoutId: String,
    val name: String,
    val orderIndex: Int,
    val exerciseCount: Int,
    val setCount: Int
)

/** Lista de ejercicios con sus recuentos y en cuantas rutinas se usa. */
data class WorkoutSummaryRow(
    val id: String,
    val name: String,
    val exerciseCount: Int,
    val setCount: Int,
    val routineCount: Int,
    val preview: String?
)

/** Ejercicio dentro de una lista, ya resuelto contra el catalogo. */
data class WorkoutExerciseRow(
    val id: String,
    val exerciseId: String,
    val name: String,
    val iconKey: String?,
    val targetMuscle: String?,
    val orderInRoutine: Int,
    val restSeconds: Int,
    val notes: String?
)

/** Serie planificada, con el ejercicio de la lista al que pertenece. */
data class WorkoutSetRow(
    val id: String,
    val workoutExerciseId: String,
    val setOrder: Int,
    val type: SetType,
    val rangeRepIni: Int,
    val rangeRepEnd: Int,
    val targetRpe: Double?,
    val restSeconds: Int?
)

@Dao
interface TrainingDao {

    // -----------------------------------------------------------------------
    // Rutinas
    // -----------------------------------------------------------------------

    @Query("SELECT * FROM routine WHERE userId = :userId AND isActive = 1 AND deletedAt IS NULL LIMIT 1")
    fun observeActiveRoutine(userId: String): Flow<RoutineEntity?>

    @Query(
        """
        SELECT r.id AS id,
               r.title AS title,
               r.description AS description,
               r.isActive AS isActive,
               (SELECT COUNT(*) FROM routine_workouts rw
                 WHERE rw.routineId = r.id AND rw.deletedAt IS NULL) AS workoutCount,
               (SELECT COUNT(*) FROM routine_workouts rw2
                 INNER JOIN workout_exercises we ON we.workoutId = rw2.workoutId
                 WHERE rw2.routineId = r.id AND rw2.deletedAt IS NULL
                   AND we.deletedAt IS NULL) AS exerciseCount,
               (SELECT COUNT(*) FROM routine_workouts rw3
                 INNER JOIN workout_exercises we2 ON we2.workoutId = rw3.workoutId
                 INNER JOIN workout_sets ws ON ws.workoutExerciseId = we2.id
                 WHERE rw3.routineId = r.id AND rw3.deletedAt IS NULL
                   AND we2.deletedAt IS NULL AND ws.deletedAt IS NULL) AS setCount
        FROM routine r
        WHERE r.userId = :userId AND r.deletedAt IS NULL
        ORDER BY r.isActive DESC, r.createdAt
        """
    )
    fun observeRoutines(userId: String): Flow<List<RoutineSummaryRow>>

    @Query("SELECT * FROM routine WHERE id = :routineId AND deletedAt IS NULL")
    fun observeRoutine(routineId: String): Flow<RoutineEntity?>

    @Query(
        """
        SELECT rw.id AS entryId,
               w.id AS workoutId,
               w.name AS name,
               rw.orderIndex AS orderIndex,
               (SELECT COUNT(*) FROM workout_exercises we
                 WHERE we.workoutId = w.id AND we.deletedAt IS NULL) AS exerciseCount,
               (SELECT COUNT(*) FROM workout_sets ws
                 INNER JOIN workout_exercises we2 ON we2.id = ws.workoutExerciseId
                 WHERE we2.workoutId = w.id AND we2.deletedAt IS NULL
                   AND ws.deletedAt IS NULL) AS setCount
        FROM routine_workouts rw
        INNER JOIN workout w ON w.id = rw.workoutId
        WHERE rw.routineId = :routineId AND rw.deletedAt IS NULL AND w.deletedAt IS NULL
        ORDER BY rw.orderIndex
        """
    )
    fun observeRoutineWorkouts(routineId: String): Flow<List<RoutineWorkoutRow>>

    @Query(
        """
        SELECT * FROM routine_workouts
        WHERE routineId = :routineId AND deletedAt IS NULL
        ORDER BY orderIndex
        """
    )
    suspend fun getRoutineWorkouts(routineId: String): List<RoutineWorkoutEntity>

    @Query("SELECT * FROM routine WHERE id = :routineId")
    suspend fun getRoutine(routineId: String): RoutineEntity?

    @Upsert
    suspend fun upsertRoutine(routine: RoutineEntity)

    @Upsert
    suspend fun upsertRoutineWorkouts(entries: List<RoutineWorkoutEntity>)

    @Query(
        """
        UPDATE routine SET deletedAt = :now, syncState = 'PENDING', updatedAt = :now
        WHERE id = :routineId
        """
    )
    suspend fun softDeleteRoutine(routineId: String, now: Instant)

    @Query(
        """
        UPDATE routine_workouts SET deletedAt = :now, syncState = 'PENDING', updatedAt = :now
        WHERE id = :entryId
        """
    )
    suspend fun softDeleteRoutineWorkout(entryId: String, now: Instant)

    @Query(
        """
        UPDATE routine SET isActive = 0, syncState = 'PENDING', updatedAt = :now
        WHERE userId = :userId AND isActive = 1
        """
    )
    suspend fun deactivateRoutines(userId: String, now: Instant)

    @Query(
        """
        UPDATE routine SET isActive = 1, syncState = 'PENDING', updatedAt = :now
        WHERE id = :routineId
        """
    )
    suspend fun activateRoutine(routineId: String, now: Instant)

    @Query("SELECT COUNT(*) FROM routine WHERE userId = :userId AND deletedAt IS NULL")
    suspend fun countRoutines(userId: String): Int

    /**
     * Sesiones ya registradas desde `sinceEpochMillis` con listas de la rutina activa.
     *
     * Es la senal que bloquea el cambio de rutina: empezada la semana, la secuencia se respeta
     * hasta el lunes siguiente.
     */
    @Query(
        """
        SELECT COUNT(*) FROM workout_logs l
        WHERE l.userId = :userId
          AND l.startedAt >= :sinceEpochMillis
          AND l.deletedAt IS NULL
          AND l.workoutId IN (
            SELECT rw.workoutId FROM routine_workouts rw
            INNER JOIN routine r ON r.id = rw.routineId
            WHERE r.userId = :userId AND r.isActive = 1
              AND r.deletedAt IS NULL AND rw.deletedAt IS NULL
          )
        """
    )
    fun observeActiveRoutineSessionsSince(userId: String, sinceEpochMillis: Long): Flow<Int>

    /**
     * Proxima sesion segun el puntero de secuencia del usuario.
     *
     * Si el puntero se sale del rango (por ejemplo tras quitar listas), se toma la primera de la
     * rutina: la app nunca se queda sin siguiente sesion.
     */
    @Query(
        """
        SELECT w.id AS workoutId,
               w.name AS name,
               rw.orderIndex AS orderIndex,
               r.title AS routineTitle,
               (SELECT COUNT(*) FROM workout_exercises we
                 WHERE we.workoutId = w.id AND we.deletedAt IS NULL) AS exerciseCount,
               (SELECT COUNT(*) FROM workout_sets ws
                 INNER JOIN workout_exercises we2 ON we2.id = ws.workoutExerciseId
                 WHERE we2.workoutId = w.id AND we2.deletedAt IS NULL
                   AND ws.deletedAt IS NULL) AS setCount,
               COALESCE((SELECT SUM(we3.restSeconds) FROM workout_exercises we3
                 WHERE we3.workoutId = w.id AND we3.deletedAt IS NULL), 0) AS restSecondsTotal
        FROM routine_workouts rw
        INNER JOIN workout w ON w.id = rw.workoutId
        INNER JOIN routine r ON r.id = rw.routineId
        WHERE r.userId = :userId AND r.isActive = 1
          AND r.deletedAt IS NULL AND rw.deletedAt IS NULL AND w.deletedAt IS NULL
        ORDER BY (rw.orderIndex < :sequenceIndex), rw.orderIndex
        LIMIT 1
        """
    )
    fun observeNextSession(userId: String, sequenceIndex: Int): Flow<NextSessionRow?>

    // -----------------------------------------------------------------------
    // Listas de ejercicios
    // -----------------------------------------------------------------------

    @Query(
        """
        SELECT w.id AS id,
               w.name AS name,
               (SELECT COUNT(*) FROM workout_exercises we
                 WHERE we.workoutId = w.id AND we.deletedAt IS NULL) AS exerciseCount,
               (SELECT COUNT(*) FROM workout_sets ws
                 INNER JOIN workout_exercises we2 ON we2.id = ws.workoutExerciseId
                 WHERE we2.workoutId = w.id AND we2.deletedAt IS NULL
                   AND ws.deletedAt IS NULL) AS setCount,
               (SELECT COUNT(DISTINCT rw.routineId) FROM routine_workouts rw
                 INNER JOIN routine r ON r.id = rw.routineId
                 WHERE rw.workoutId = w.id AND rw.deletedAt IS NULL
                   AND r.deletedAt IS NULL) AS routineCount,
               (SELECT GROUP_CONCAT(e.name) FROM workout_exercises we3
                 INNER JOIN exercises e ON e.id = we3.exerciseId
                 WHERE we3.workoutId = w.id AND we3.deletedAt IS NULL) AS preview
        FROM workout w
        WHERE w.userId = :userId AND w.deletedAt IS NULL
        ORDER BY w.createdAt
        """
    )
    fun observeWorkouts(userId: String): Flow<List<WorkoutSummaryRow>>

    @Query("SELECT * FROM workout WHERE id = :workoutId AND deletedAt IS NULL")
    fun observeWorkout(workoutId: String): Flow<WorkoutEntity?>

    @Query("SELECT * FROM workout WHERE id = :workoutId")
    suspend fun getWorkout(workoutId: String): WorkoutEntity?

    @Query(
        """
        SELECT we.id AS id,
               we.exerciseId AS exerciseId,
               e.name AS name,
               e.iconKey AS iconKey,
               (SELECT m.name FROM exercise_muscles em
                 INNER JOIN muscles m ON m.id = em.muscleId
                 WHERE em.exerciseId = e.id AND em.isTarget = 1 LIMIT 1) AS targetMuscle,
               we.orderInRoutine AS orderInRoutine,
               we.restSeconds AS restSeconds,
               we.notes AS notes
        FROM workout_exercises we
        INNER JOIN exercises e ON e.id = we.exerciseId
        WHERE we.workoutId = :workoutId AND we.deletedAt IS NULL
        ORDER BY we.orderInRoutine
        """
    )
    fun observeWorkoutExercises(workoutId: String): Flow<List<WorkoutExerciseRow>>

    @Query(
        """
        SELECT ws.id AS id,
               ws.workoutExerciseId AS workoutExerciseId,
               ws.setOrder AS setOrder,
               ws.type AS type,
               ws.rangeRepIni AS rangeRepIni,
               ws.rangeRepEnd AS rangeRepEnd,
               ws.targetRpe AS targetRpe,
               ws.restSeconds AS restSeconds
        FROM workout_sets ws
        INNER JOIN workout_exercises we ON we.id = ws.workoutExerciseId
        WHERE we.workoutId = :workoutId AND ws.deletedAt IS NULL AND we.deletedAt IS NULL
        ORDER BY we.orderInRoutine, ws.setOrder
        """
    )
    fun observeWorkoutSets(workoutId: String): Flow<List<WorkoutSetRow>>

    @Query(
        """
        SELECT * FROM workout_exercises
        WHERE workoutId = :workoutId AND deletedAt IS NULL
        ORDER BY orderInRoutine
        """
    )
    suspend fun getWorkoutExercises(workoutId: String): List<WorkoutExerciseEntity>

    @Query("SELECT * FROM workout_exercises WHERE id = :workoutExerciseId")
    suspend fun getWorkoutExercise(workoutExerciseId: String): WorkoutExerciseEntity?

    @Query(
        """
        SELECT * FROM workout_sets
        WHERE workoutExerciseId = :workoutExerciseId AND deletedAt IS NULL
        ORDER BY setOrder
        """
    )
    suspend fun getSets(workoutExerciseId: String): List<WorkoutSetEntity>

    @Query("SELECT * FROM workout_sets WHERE id = :setId")
    suspend fun getSet(setId: String): WorkoutSetEntity?

    @Upsert
    suspend fun upsertWorkout(workout: WorkoutEntity)

    @Upsert
    suspend fun upsertWorkouts(workouts: List<WorkoutEntity>)

    @Upsert
    suspend fun upsertWorkoutExercises(exercises: List<WorkoutExerciseEntity>)

    @Upsert
    suspend fun upsertWorkoutSets(sets: List<WorkoutSetEntity>)

    @Query(
        """
        UPDATE workout SET deletedAt = :now, syncState = 'PENDING', updatedAt = :now
        WHERE id = :workoutId
        """
    )
    suspend fun softDeleteWorkout(workoutId: String, now: Instant)

    /** Al borrar una lista desaparece de toda rutina que la usaba. */
    @Query(
        """
        UPDATE routine_workouts SET deletedAt = :now, syncState = 'PENDING', updatedAt = :now
        WHERE workoutId = :workoutId AND deletedAt IS NULL
        """
    )
    suspend fun softDeleteRoutineWorkoutsOf(workoutId: String, now: Instant)

    @Query(
        """
        UPDATE workout_exercises SET deletedAt = :now, syncState = 'PENDING', updatedAt = :now
        WHERE id = :workoutExerciseId
        """
    )
    suspend fun softDeleteWorkoutExercise(workoutExerciseId: String, now: Instant)

    @Query(
        """
        UPDATE workout_sets SET deletedAt = :now, syncState = 'PENDING', updatedAt = :now
        WHERE id = :setId
        """
    )
    suspend fun softDeleteSet(setId: String, now: Instant)

    @Query("SELECT COUNT(*) FROM workout_exercises WHERE exerciseId = :exerciseId AND deletedAt IS NULL")
    suspend fun countUsagesOfExercise(exerciseId: String): Int
}
