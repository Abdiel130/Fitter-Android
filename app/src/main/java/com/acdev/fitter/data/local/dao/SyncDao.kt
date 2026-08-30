package com.acdev.fitter.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Recuento de filas por estado, sumado sobre todas las tablas sincronizables. */
data class SyncCountsRow(
    val pendingCount: Int,
    val failedCount: Int,
    val conflictCount: Int
)

/**
 * Consulta unica del estado de la cola.
 *
 * Al anadir una tabla sincronizable hay que sumarla aqui. Es el unico punto del proyecto que
 * conoce la lista completa, y por eso vive solo en este DAO.
 */
@Dao
interface SyncDao {

    @Query(
        """
        SELECT
          (SELECT COUNT(*) FROM workout_logs WHERE syncState = 'PENDING')
        + (SELECT COUNT(*) FROM workout_log_sets WHERE syncState = 'PENDING')
        + (SELECT COUNT(*) FROM body_measurement WHERE syncState = 'PENDING')
        + (SELECT COUNT(*) FROM daily_habit_logs WHERE syncState = 'PENDING')
        + (SELECT COUNT(*) FROM daily_food_logs WHERE syncState = 'PENDING')
        + (SELECT COUNT(*) FROM daily_supplement_logs WHERE syncState = 'PENDING')
        + (SELECT COUNT(*) FROM joint_discomfort_logs WHERE syncState = 'PENDING')
        + (SELECT COUNT(*) FROM routine WHERE syncState = 'PENDING')
        + (SELECT COUNT(*) FROM workout WHERE syncState = 'PENDING')
        AS pendingCount,
          (SELECT COUNT(*) FROM workout_logs WHERE syncState = 'FAILED')
        + (SELECT COUNT(*) FROM workout_log_sets WHERE syncState = 'FAILED')
        + (SELECT COUNT(*) FROM body_measurement WHERE syncState = 'FAILED')
        + (SELECT COUNT(*) FROM daily_habit_logs WHERE syncState = 'FAILED')
        + (SELECT COUNT(*) FROM daily_food_logs WHERE syncState = 'FAILED')
        AS failedCount,
          (SELECT COUNT(*) FROM workout_logs WHERE syncState = 'CONFLICT')
        + (SELECT COUNT(*) FROM body_measurement WHERE syncState = 'CONFLICT')
        + (SELECT COUNT(*) FROM daily_habit_logs WHERE syncState = 'CONFLICT')
        AS conflictCount
        """
    )
    fun observeCounts(): Flow<SyncCountsRow>
}
