package com.acdev.fitter.data.repository

import com.acdev.fitter.core.time.AppClock
import com.acdev.fitter.core.util.IdGenerator
import com.acdev.fitter.data.local.dao.BodyMeasurementDao
import com.acdev.fitter.data.local.entity.BodyMeasurementEntity
import com.acdev.fitter.data.local.entity.SyncMetadata
import com.acdev.fitter.domain.repository.BodyRepository
import java.time.LocalDate

class BodyRepositoryImpl(
    private val bodyMeasurementDao: BodyMeasurementDao,
    private val clock: AppClock,
    private val idGenerator: IdGenerator
) : BodyRepository {

    override suspend fun saveMeasurement(
        userId: String,
        date: LocalDate,
        weightKg: Double?,
        waistCm: Double?,
        chestCm: Double?
    ) {
        bodyMeasurementDao.upsert(
            BodyMeasurementEntity(
                id = idGenerator.newId(),
                userId = userId,
                logDate = date,
                weightKg = weightKg,
                waistNavelCm = waistCm,
                chestCm = chestCm,
                sync = SyncMetadata(updatedAt = clock.now())
            )
        )
    }
}
