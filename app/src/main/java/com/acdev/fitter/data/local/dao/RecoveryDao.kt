package com.acdev.fitter.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.acdev.fitter.data.local.entity.DailyHabitLogEntity
import com.acdev.fitter.data.local.entity.DailySupplementLogEntity
import com.acdev.fitter.data.local.entity.SupplementEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Recuento de suplementos tomados frente a los activos, para el resumen del dia. */
data class SupplementProgressRow(
    val taken: Int,
    val total: Int
)

@Dao
interface RecoveryDao {

    @Query(
        """
        SELECT * FROM daily_habit_logs
        WHERE userId = :userId AND logDate = :date AND deletedAt IS NULL
        LIMIT 1
        """
    )
    fun observeHabitLog(userId: String, date: LocalDate): Flow<DailyHabitLogEntity?>

    @Query(
        """
        SELECT * FROM daily_habit_logs
        WHERE userId = :userId AND logDate = :date AND deletedAt IS NULL
        LIMIT 1
        """
    )
    suspend fun getHabitLog(userId: String, date: LocalDate): DailyHabitLogEntity?

    @Upsert
    suspend fun upsertHabitLog(log: DailyHabitLogEntity)

    @Query(
        """
        SELECT
            (SELECT COUNT(*) FROM daily_supplement_logs dsl
              INNER JOIN daily_habit_logs h ON h.id = dsl.dailyHabitLogId
              WHERE h.userId = :userId AND h.logDate = :date AND dsl.isTaken = 1
                AND dsl.deletedAt IS NULL) AS taken,
            (SELECT COUNT(*) FROM supplements
              WHERE userId = :userId AND isActive = 1 AND deletedAt IS NULL) AS total
        """
    )
    fun observeSupplementProgress(userId: String, date: LocalDate): Flow<SupplementProgressRow>

    @Query("SELECT * FROM supplements WHERE userId = :userId AND isActive = 1 AND deletedAt IS NULL ORDER BY name")
    fun observeSupplements(userId: String): Flow<List<SupplementEntity>>

    @Query("SELECT COUNT(*) FROM supplements WHERE userId = :userId AND deletedAt IS NULL")
    suspend fun countSupplements(userId: String): Int

    @Upsert
    suspend fun upsertSupplements(supplements: List<SupplementEntity>)

    @Upsert
    suspend fun upsertSupplementLogs(logs: List<DailySupplementLogEntity>)
}
