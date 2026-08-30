package com.acdev.fitter.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.acdev.fitter.domain.model.SetType
import java.time.Instant

/**
 * Rutina semanal (split). Solo una puede estar activa por usuario.
 *
 * Ejemplo del dominio: "Semana enfoque empuje" con los workouts empuje, jale, pierna, empuje.
 */
@Entity(
    tableName = "routine",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class RoutineEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String? = null,
    val isActive: Boolean = false,
    val createdAt: Instant,
    @Embedded val sync: SyncMetadata = SyncMetadata()
)

/**
 * Dia de la rutina. `orderIndex` define la secuencia ciclica, no el dia de la semana: el usuario
 * decide cuando entrena y la app avanza el puntero.
 */
@Entity(
    tableName = "workout",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["routineId", "orderIndex"], unique = true)]
)
data class WorkoutEntity(
    @PrimaryKey val id: String,
    val routineId: String,
    val name: String,
    val orderIndex: Int,
    val createdAt: Instant,
    @Embedded val sync: SyncMetadata = SyncMetadata()
)

/** Ejercicio dentro de un dia de rutina, con su orden y descanso por defecto. */
@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("workoutId"), Index("exerciseId")]
)
data class WorkoutExerciseEntity(
    @PrimaryKey val id: String,
    val workoutId: String,
    val exerciseId: String,
    val orderInRoutine: Int,
    /** Descanso global del ejercicio. Cada serie puede sobrescribirlo. */
    val restSeconds: Int = 90,
    val notes: String? = null,
    @Embedded val sync: SyncMetadata = SyncMetadata()
)

/** Serie planificada (el objetivo), no la ejecutada. La ejecutada vive en `workout_log_sets`. */
@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workoutExerciseId")]
)
data class WorkoutSetEntity(
    @PrimaryKey val id: String,
    val workoutExerciseId: String,
    val setOrder: Int,
    val type: SetType = SetType.NORMAL,
    val rangeRepIni: Int,
    val rangeRepEnd: Int,
    val targetRpe: Double? = null,
    /** Descanso especifico de esta serie. Tiene prioridad sobre el del ejercicio. */
    val restSeconds: Int? = null,
    @Embedded val sync: SyncMetadata = SyncMetadata()
)
