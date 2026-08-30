package com.acdev.fitter.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.acdev.fitter.domain.model.PoseType
import java.time.Instant
import java.time.LocalDate

/**
 * Registro antropometrico de un dia. Todos los campos salvo la fecha son opcionales: el usuario
 * puede registrar solo peso, solo medidas, o solo fotos, y completarlo mas tarde.
 */
@Entity(
    tableName = "body_measurement",
    foreignKeys = [
        ForeignKey(UserEntity::class, ["id"], ["userId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["userId", "logDate"], unique = true)]
)
data class BodyMeasurementEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val logDate: LocalDate,
    /** Peso en ayunas, normalizado a kilogramos. */
    val weightKg: Double? = null,
    val bodyFatPercentage: Double? = null,
    val neckCm: Double? = null,
    val chestCm: Double? = null,
    /** Cintura a la altura del ombligo. */
    val waistNavelCm: Double? = null,
    val hipsCm: Double? = null,
    val bicepLeftCm: Double? = null,
    val bicepRightCm: Double? = null,
    val thighLeftCm: Double? = null,
    val thighRightCm: Double? = null,
    val calfCm: Double? = null,
    val notes: String? = null,
    @Embedded val sync: SyncMetadata = SyncMetadata()
)

/** Foto de progreso, siempre colgando de un registro antropometrico. */
@Entity(
    tableName = "progress_photos",
    foreignKeys = [
        ForeignKey(
            BodyMeasurementEntity::class,
            ["id"],
            ["measurementId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("measurementId")]
)
data class ProgressPhotoEntity(
    @PrimaryKey val id: String,
    val measurementId: String,
    val poseType: PoseType,
    /** Ruta en el almacenamiento privado de la app. Nunca una URI externa. */
    val photoPath: String,
    val takenAt: Instant,
    @Embedded val sync: SyncMetadata = SyncMetadata()
)
