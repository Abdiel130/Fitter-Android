package com.acdev.fitter.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.acdev.fitter.domain.model.SetType
import com.acdev.fitter.domain.model.WeightUnit
import java.time.Instant

/** Sesion realmente ejecutada. Se abre al empezar a entrenar y se cierra al terminar. */
@Entity(
    tableName = "workout_logs",
    foreignKeys = [
        ForeignKey(UserEntity::class, ["id"], ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(WorkoutEntity::class, ["id"], ["workoutId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("userId"), Index("workoutId"), Index("startedAt")]
)
data class WorkoutLogEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val workoutId: String,
    val title: String,
    val startedAt: Instant,
    val finishedAt: Instant? = null,
    /** Fatiga general de la sesion, escala 1-10. */
    val overallRpe: Int? = null,
    val notes: String? = null,
    @Embedded val sync: SyncMetadata = SyncMetadata()
)

/**
 * Serie ejecutada.
 *
 * Se guardan las dos caras del peso: `weightInput` con la unidad que uso el usuario, para poder
 * mostrarle exactamente lo que escribio, y `weightKg` normalizado, unico valor que entra en
 * calculos y analiticas.
 */
@Entity(
    tableName = "workout_log_sets",
    foreignKeys = [
        ForeignKey(WorkoutLogEntity::class, ["id"], ["workoutLogId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(ExerciseEntity::class, ["id"], ["exerciseId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("workoutLogId"), Index("exerciseId")]
)
data class WorkoutLogSetEntity(
    @PrimaryKey val id: String,
    val workoutLogId: String,
    val exerciseId: String,
    val setOrder: Int,
    val setType: SetType = SetType.NORMAL,
    val weightInput: Double,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val weightKg: Double,
    val repsCompleted: Int,
    val rpe: Double? = null,
    /** 1RM teorico calculado siempre sobre `weightKg`. */
    val calculated1rm: Double? = null,
    val isPersonalRecord: Boolean = false,
    val createdAt: Instant,
    @Embedded val sync: SyncMetadata = SyncMetadata()
)
