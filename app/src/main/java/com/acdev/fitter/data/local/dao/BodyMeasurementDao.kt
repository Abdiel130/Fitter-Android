package com.acdev.fitter.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.acdev.fitter.data.local.entity.BodyMeasurementEntity
import com.acdev.fitter.data.local.entity.ProgressPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyMeasurementDao {

    @Query(
        """
        SELECT * FROM body_measurement
        WHERE userId = :userId AND deletedAt IS NULL
        ORDER BY logDate DESC
        """
    )
    fun observeHistory(userId: String): Flow<List<BodyMeasurementEntity>>

    @Query(
        """
        SELECT * FROM body_measurement
        WHERE userId = :userId AND weightKg IS NOT NULL AND deletedAt IS NULL
        ORDER BY logDate DESC
        LIMIT 1
        """
    )
    fun observeLatestWeight(userId: String): Flow<BodyMeasurementEntity?>

    @Upsert
    suspend fun upsert(measurement: BodyMeasurementEntity)

    @Upsert
    suspend fun upsertPhotos(photos: List<ProgressPhotoEntity>)
}
