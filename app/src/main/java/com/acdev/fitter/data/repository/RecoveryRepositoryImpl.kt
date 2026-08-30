package com.acdev.fitter.data.repository

import com.acdev.fitter.core.time.AppClock
import com.acdev.fitter.core.util.IdGenerator
import com.acdev.fitter.data.local.dao.RecoveryDao
import com.acdev.fitter.data.local.entity.DailyHabitLogEntity
import com.acdev.fitter.data.local.entity.SupplementEntity
import com.acdev.fitter.data.local.entity.SyncMetadata
import com.acdev.fitter.data.local.seed.StarterCatalog
import com.acdev.fitter.domain.model.Hydration
import com.acdev.fitter.domain.model.SleepSummary
import com.acdev.fitter.domain.model.SupplementProgress
import com.acdev.fitter.domain.model.SyncState
import com.acdev.fitter.domain.repository.RecoveryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** Meta de hidratacion por defecto mientras el usuario no la cambie. */
private const val DEFAULT_WATER_TARGET_ML = 3000

class RecoveryRepositoryImpl(
    private val recoveryDao: RecoveryDao,
    private val clock: AppClock,
    private val idGenerator: IdGenerator
) : RecoveryRepository {

    override fun observeHydration(userId: String, date: LocalDate): Flow<Hydration> =
        recoveryDao.observeHabitLog(userId, date).map { log ->
            Hydration(
                intakeMl = log?.waterIntakeMl ?: 0,
                targetMl = log?.waterTargetMl ?: DEFAULT_WATER_TARGET_ML
            )
        }

    override fun observeSleep(userId: String, date: LocalDate): Flow<SleepSummary> =
        recoveryDao.observeHabitLog(userId, date).map { log ->
            SleepSummary(hours = log?.sleepHours, quality = log?.sleepQuality)
        }

    override fun observeSupplementProgress(
        userId: String,
        date: LocalDate
    ): Flow<SupplementProgress> =
        recoveryDao.observeSupplementProgress(userId, date).map {
            SupplementProgress(taken = it.taken, total = it.total)
        }

    override suspend fun addWater(userId: String, date: LocalDate, milliliters: Int) {
        val current = ensureHabitLog(userId, date)
        recoveryDao.upsertHabitLog(
            current.copy(
                waterIntakeMl = (current.waterIntakeMl + milliliters).coerceAtLeast(0),
                sync = current.sync.copy(syncState = SyncState.PENDING, updatedAt = clock.now())
            )
        )
    }

    /** Crea el diario del dia si aun no existe. Toda escritura de habitos pasa por aqui. */
    private suspend fun ensureHabitLog(userId: String, date: LocalDate): DailyHabitLogEntity {
        recoveryDao.getHabitLog(userId, date)?.let { return it }

        val created = DailyHabitLogEntity(
            id = idGenerator.newId(),
            userId = userId,
            logDate = date,
            waterTargetMl = DEFAULT_WATER_TARGET_ML,
            sync = SyncMetadata(updatedAt = clock.now())
        )
        recoveryDao.upsertHabitLog(created)
        return created
    }

    override suspend fun ensureStarterSupplements(userId: String) {
        if (recoveryDao.countSupplements(userId) > 0) return

        recoveryDao.upsertSupplements(
            StarterCatalog.supplements.map { (name, dosage) ->
                SupplementEntity(
                    id = idGenerator.newId(),
                    userId = userId,
                    name = name,
                    targetDosage = dosage,
                    sync = SyncMetadata(updatedAt = clock.now())
                )
            }
        )
    }
}
