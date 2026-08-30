package com.acdev.fitter.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

/**
 * Diario diario de habitos: sueno, energia e hidratacion. Una fila por usuario y dia.
 *
 * Es la fila raiz del dia: de ella cuelgan la toma de suplementos y las molestias articulares.
 */
@Entity(
    tableName = "daily_habit_logs",
    foreignKeys = [ForeignKey(UserEntity::class, ["id"], ["userId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["userId", "logDate"], unique = true)]
)
data class DailyHabitLogEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val logDate: LocalDate,
    val sleepHours: Double? = null,
    /** Escala 1-5. */
    val sleepQuality: Int? = null,
    /** Escala 1-5. */
    val energyLevel: Int? = null,
    val waterIntakeMl: Int = 0,
    val waterTargetMl: Int = 3000,
    @Embedded val sync: SyncMetadata = SyncMetadata()
)

/** Suplemento que el usuario quiere seguir a diario. */
@Entity(
    tableName = "supplements",
    foreignKeys = [ForeignKey(UserEntity::class, ["id"], ["userId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("userId")]
)
data class SupplementEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val targetDosage: String? = null,
    val isActive: Boolean = true,
    @Embedded val sync: SyncMetadata = SyncMetadata()
)

/** Marca de toma de un suplemento en un dia concreto. */
@Entity(
    tableName = "daily_supplement_logs",
    foreignKeys = [
        ForeignKey(
            DailyHabitLogEntity::class,
            ["id"],
            ["dailyHabitLogId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(SupplementEntity::class, ["id"], ["supplementId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["dailyHabitLogId", "supplementId"], unique = true), Index("supplementId")]
)
data class DailySupplementLogEntity(
    @PrimaryKey val id: String,
    val dailyHabitLogId: String,
    val supplementId: String,
    val isTaken: Boolean = false,
    val takenAt: Instant? = null,
    @Embedded val sync: SyncMetadata = SyncMetadata()
)

/** Bitacora de molestias articulares para ajustar la sesion del dia. */
@Entity(
    tableName = "joint_discomfort_logs",
    foreignKeys = [
        ForeignKey(
            DailyHabitLogEntity::class,
            ["id"],
            ["dailyHabitLogId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dailyHabitLogId")]
)
data class JointDiscomfortLogEntity(
    @PrimaryKey val id: String,
    val dailyHabitLogId: String,
    /** Identificador estable de la zona: shoulder_left, knee_right, lumbar... */
    val jointArea: String,
    /** Escala 1-10. */
    val painIntensity: Int,
    val notes: String? = null,
    @Embedded val sync: SyncMetadata = SyncMetadata()
)
