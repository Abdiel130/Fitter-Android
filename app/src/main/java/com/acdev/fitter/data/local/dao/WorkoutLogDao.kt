package com.acdev.fitter.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.acdev.fitter.data.local.entity.WorkoutLogEntity
import com.acdev.fitter.data.local.entity.WorkoutLogSetEntity
import kotlinx.coroutines.flow.Flow

/** Series efectivas acumuladas por musculo objetivo dentro de una ventana temporal. */
data class MuscleVolumeRow(
    val muscleName: String,
    val setCount: Int
)

@Dao
interface WorkoutLogDao {

    @Upsert
    suspend fun upsertLog(log: WorkoutLogEntity)

    @Upsert
    suspend fun upsertSets(sets: List<WorkoutLogSetEntity>)

    @Query(
        """
        SELECT * FROM workout_logs
        WHERE userId = :userId AND deletedAt IS NULL
        ORDER BY startedAt DESC
        LIMIT :limit
        """
    )
    fun observeRecentLogs(userId: String, limit: Int): Flow<List<WorkoutLogEntity>>

    /**
     * Series completadas desde `since`. Las de calentamiento no cuentan como volumen efectivo.
     */
    @Query(
        """
        SELECT COUNT(*) FROM workout_log_sets s
        INNER JOIN workout_logs l ON l.id = s.workoutLogId
        WHERE l.userId = :userId
          AND l.startedAt >= :sinceEpochMillis
          AND s.setType != 'WARMUP'
          AND s.deletedAt IS NULL
          AND l.deletedAt IS NULL
        """
    )
    fun observeEffectiveSetCount(userId: String, sinceEpochMillis: Long): Flow<Int>

    @Query(
        """
        SELECT m.name AS muscleName, COUNT(*) AS setCount
        FROM workout_log_sets s
        INNER JOIN workout_logs l ON l.id = s.workoutLogId
        INNER JOIN exercise_muscles em ON em.exerciseId = s.exerciseId AND em.isTarget = 1
        INNER JOIN muscles m ON m.id = em.muscleId
        WHERE l.userId = :userId
          AND l.startedAt >= :sinceEpochMillis
          AND s.setType != 'WARMUP'
          AND s.deletedAt IS NULL
          AND l.deletedAt IS NULL
        GROUP BY m.name
        ORDER BY setCount DESC
        """
    )
    fun observeVolumeByMuscle(userId: String, sinceEpochMillis: Long): Flow<List<MuscleVolumeRow>>

    /** Ultimo peso registrado para un ejercicio: precarga el campo durante el entreno. */
    @Query(
        """
        SELECT * FROM workout_log_sets
        WHERE exerciseId = :exerciseId AND deletedAt IS NULL
        ORDER BY createdAt DESC
        LIMIT 1
        """
    )
    suspend fun lastSetFor(exerciseId: String): WorkoutLogSetEntity?
}
