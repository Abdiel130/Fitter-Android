package com.acdev.fitter.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Ejercicio del catalogo o propio del usuario.
 *
 * `userId == null` significa catalogo precargado (comun a todos). Con id, es un ejercicio que
 * creo el usuario y por tanto se sincroniza.
 */
@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index(value = ["externalId"], unique = true)]
)
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val userId: String? = null,
    /** Identificador en la fuente externa (ExerciseDB). Nulo en ejercicios propios. */
    val externalId: String? = null,
    val name: String,
    /** Ruta local del gif descargado o URL remota. */
    val gifUrl: String? = null,
    /** Pasos de ejecucion serializados como JSON. */
    val instructions: String? = null,
    /** Nombre de la constante de `ExerciseIcon`. Nulo usa el icono por defecto. */
    val iconKey: String? = null,
    val createdAt: Instant,
    @Embedded val sync: SyncMetadata = SyncMetadata()
)

/** Catalogo cerrado: zona corporal (chest, back, waist...). No se sincroniza. */
@Entity(tableName = "body_parts", indices = [Index(value = ["name"], unique = true)])
data class BodyPartEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

/** Catalogo cerrado: musculo concreto (latissimus dorsi, biceps...). */
@Entity(tableName = "muscles", indices = [Index(value = ["name"], unique = true)])
data class MuscleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

/** Catalogo cerrado: material (barbell, dumbbell, cable...). */
@Entity(tableName = "equipments", indices = [Index(value = ["name"], unique = true)])
data class EquipmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

@Entity(
    tableName = "exercise_body_parts",
    primaryKeys = ["exerciseId", "bodyPartId"],
    foreignKeys = [
        ForeignKey(ExerciseEntity::class, ["id"], ["exerciseId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(BodyPartEntity::class, ["id"], ["bodyPartId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("bodyPartId")]
)
data class ExerciseBodyPartCrossRef(
    val exerciseId: String,
    val bodyPartId: Int
)

@Entity(
    tableName = "exercise_equipments",
    primaryKeys = ["exerciseId", "equipmentId"],
    foreignKeys = [
        ForeignKey(ExerciseEntity::class, ["id"], ["exerciseId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(EquipmentEntity::class, ["id"], ["equipmentId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("equipmentId")]
)
data class ExerciseEquipmentCrossRef(
    val exerciseId: String,
    val equipmentId: Int
)

/**
 * Relacion ejercicio-musculo. `isTarget` distingue el musculo objetivo del secundario y forma
 * parte de la clave: un mismo musculo puede ser objetivo en un ejercicio y secundario en otro.
 */
@Entity(
    tableName = "exercise_muscles",
    primaryKeys = ["exerciseId", "muscleId", "isTarget"],
    foreignKeys = [
        ForeignKey(ExerciseEntity::class, ["id"], ["exerciseId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(MuscleEntity::class, ["id"], ["muscleId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("muscleId")]
)
data class ExerciseMuscleCrossRef(
    val exerciseId: String,
    val muscleId: Int,
    val isTarget: Boolean = true
)

/** Ejercicio alternativo cuando la maquina esta ocupada. */
@Entity(
    tableName = "exercise_substitutes",
    foreignKeys = [
        ForeignKey(ExerciseEntity::class, ["id"], ["exerciseId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(ExerciseEntity::class, ["id"], ["substituteExerciseId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("exerciseId"), Index("substituteExerciseId")]
)
data class ExerciseSubstituteEntity(
    @PrimaryKey val id: String,
    val exerciseId: String,
    val substituteExerciseId: String,
    val notes: String? = null,
    @Embedded val sync: SyncMetadata = SyncMetadata()
)
