package com.acdev.fitter.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.acdev.fitter.data.local.entity.DailyFoodLogEntity
import com.acdev.fitter.data.local.entity.FoodItemEntity
import com.acdev.fitter.data.local.entity.NutritionTargetEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Suma de macros de un dia. Todos los campos pueden ser nulos si no hay lineas registradas. */
data class DailyMacrosRow(
    val calories: Double?,
    val protein: Double?,
    val carbs: Double?,
    val fat: Double?
)

@Dao
interface NutritionDao {

    @Query(
        """
        SELECT SUM(calculatedCalories) AS calories,
               SUM(calculatedProtein) AS protein,
               SUM(calculatedCarbs) AS carbs,
               SUM(calculatedFat) AS fat
        FROM daily_food_logs
        WHERE userId = :userId AND logDate = :date AND deletedAt IS NULL
        """
    )
    fun observeDailyMacros(userId: String, date: LocalDate): Flow<DailyMacrosRow>

    @Query(
        """
        SELECT * FROM nutrition_targets
        WHERE userId = :userId AND deletedAt IS NULL
        ORDER BY isDefault DESC
        LIMIT 1
        """
    )
    fun observeDefaultTarget(userId: String): Flow<NutritionTargetEntity?>

    @Upsert
    suspend fun upsertTarget(target: NutritionTargetEntity)

    @Upsert
    suspend fun upsertFoodItems(items: List<FoodItemEntity>)

    @Upsert
    suspend fun upsertFoodLogs(logs: List<DailyFoodLogEntity>)

    @Query("SELECT COUNT(*) FROM daily_food_logs WHERE userId = :userId AND logDate = :date AND deletedAt IS NULL")
    suspend fun countFoodLogs(userId: String, date: LocalDate): Int

    @Query("SELECT COUNT(*) FROM nutrition_targets WHERE userId = :userId AND deletedAt IS NULL")
    suspend fun countTargets(userId: String): Int
}
