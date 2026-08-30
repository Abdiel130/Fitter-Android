package com.acdev.fitter.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.acdev.fitter.data.local.entity.RoutineEntity
import com.acdev.fitter.data.local.entity.WorkoutEntity
import com.acdev.fitter.data.local.entity.WorkoutExerciseEntity
import com.acdev.fitter.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

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

@Dao
interface TrainingDao {

    @Query("SELECT * FROM routine WHERE userId = :userId AND isActive = 1 AND deletedAt IS NULL LIMIT 1")
    fun observeActiveRoutine(userId: String): Flow<RoutineEntity?>

    @Query(
        """
        SELECT * FROM workout
        WHERE routineId = :routineId AND deletedAt IS NULL
        ORDER BY orderIndex
        """
    )
    fun observeWorkouts(routineId: String): Flow<List<WorkoutEntity>>

    /**
     * Proxima sesion segun el puntero de secuencia del usuario.
     *
     * Si el puntero se sale del rango (por ejemplo tras borrar dias), se toma el primer workout
     * de la rutina: la app nunca se queda sin siguiente sesion.
     */
    @Query(
        """
        SELECT w.id AS workoutId,
               w.name AS name,
               w.orderIndex AS orderIndex,
               r.title AS routineTitle,
               (SELECT COUNT(*) FROM workout_exercises we
                 WHERE we.workoutId = w.id AND we.deletedAt IS NULL) AS exerciseCount,
               (SELECT COUNT(*) FROM workout_sets ws
                 INNER JOIN workout_exercises we2 ON we2.id = ws.workoutExerciseId
                 WHERE we2.workoutId = w.id AND ws.deletedAt IS NULL) AS setCount,
               COALESCE((SELECT SUM(we3.restSeconds) FROM workout_exercises we3
                 WHERE we3.workoutId = w.id AND we3.deletedAt IS NULL), 0) AS restSecondsTotal
        FROM workout w
        INNER JOIN routine r ON r.id = w.routineId
        WHERE r.userId = :userId AND r.isActive = 1 AND w.deletedAt IS NULL
        ORDER BY (w.orderIndex < :sequenceIndex), w.orderIndex
        LIMIT 1
        """
    )
    fun observeNextSession(userId: String, sequenceIndex: Int): Flow<NextSessionRow?>

    @Upsert
    suspend fun upsertRoutine(routine: RoutineEntity)

    @Upsert
    suspend fun upsertWorkouts(workouts: List<WorkoutEntity>)

    @Upsert
    suspend fun upsertWorkoutExercises(exercises: List<WorkoutExerciseEntity>)

    @Upsert
    suspend fun upsertWorkoutSets(sets: List<WorkoutSetEntity>)

    @Query("SELECT COUNT(*) FROM routine WHERE userId = :userId AND deletedAt IS NULL")
    suspend fun countRoutines(userId: String): Int
}
