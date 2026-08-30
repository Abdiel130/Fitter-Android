package com.acdev.fitter.data.repository

import com.acdev.fitter.core.util.IdGenerator
import com.acdev.fitter.data.local.dao.NutritionDao
import com.acdev.fitter.data.local.entity.NutritionTargetEntity
import com.acdev.fitter.data.local.entity.SyncMetadata
import com.acdev.fitter.domain.model.NutritionSummary
import com.acdev.fitter.domain.repository.NutritionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

/** Objetivo de arranque, editable por el usuario en cuanto entre en Nutricion. */
private const val DEFAULT_TARGET_NAME = "Objetivo diario"
private const val DEFAULT_CALORIES = 2600.0
private const val DEFAULT_PROTEIN = 180.0
private const val DEFAULT_CARBS = 280.0
private const val DEFAULT_FAT = 80.0

class NutritionRepositoryImpl(
    private val nutritionDao: NutritionDao,
    private val idGenerator: IdGenerator
) : NutritionRepository {

    override fun observeDailySummary(userId: String, date: LocalDate): Flow<NutritionSummary> =
        combine(
            nutritionDao.observeDailyMacros(userId, date),
            nutritionDao.observeDefaultTarget(userId)
        ) { macros, target ->
            NutritionSummary(
                calories = macros.calories ?: 0.0,
                protein = macros.protein ?: 0.0,
                carbs = macros.carbs ?: 0.0,
                fat = macros.fat ?: 0.0,
                caloriesTarget = target?.caloriesKcal,
                proteinTarget = target?.proteinG
            )
        }

    override suspend fun ensureDefaultTargets(userId: String) {
        if (nutritionDao.countTargets(userId) > 0) return
        nutritionDao.upsertTarget(
            NutritionTargetEntity(
                id = idGenerator.newId(),
                userId = userId,
                name = DEFAULT_TARGET_NAME,
                caloriesKcal = DEFAULT_CALORIES,
                proteinG = DEFAULT_PROTEIN,
                carbsG = DEFAULT_CARBS,
                fatG = DEFAULT_FAT,
                isDefault = true,
                sync = SyncMetadata()
            )
        )
    }
}
