package com.acdev.fitter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.acdev.fitter.data.local.entity.BodyPartEntity
import com.acdev.fitter.data.local.entity.EquipmentEntity
import com.acdev.fitter.data.local.entity.ExerciseBodyPartCrossRef
import com.acdev.fitter.data.local.entity.ExerciseEntity
import com.acdev.fitter.data.local.entity.ExerciseEquipmentCrossRef
import com.acdev.fitter.data.local.entity.ExerciseMuscleCrossRef
import com.acdev.fitter.data.local.entity.MuscleEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Ejercicio del catalogo con sus etiquetas ya agregadas en SQL.
 *
 * Las listas llegan como texto separado por comas porque `GROUP_CONCAT` resuelve la union en la
 * base: la alternativa seria traer las tablas puente enteras y cruzarlas en Kotlin.
 */
data class ExerciseCatalogRow(
    val id: String,
    val userId: String?,
    val name: String,
    val iconKey: String?,
    val instructions: String?,
    val bodyParts: String?,
    val targetMuscles: String?,
    val secondaryMuscles: String?,
    val equipments: String?
)

@Dao
interface ExerciseDao {

    /** Catalogo visible para un usuario: los precargados mas los suyos. */
    @Query(
        """
        SELECT * FROM exercises
        WHERE (userId IS NULL OR userId = :userId) AND deletedAt IS NULL
        ORDER BY name
        """
    )
    fun observeCatalog(userId: String): Flow<List<ExerciseEntity>>

    @Query(
        """
        SELECT e.id AS id,
               e.userId AS userId,
               e.name AS name,
               e.iconKey AS iconKey,
               e.instructions AS instructions,
               (SELECT GROUP_CONCAT(bp.name) FROM exercise_body_parts ebp
                 INNER JOIN body_parts bp ON bp.id = ebp.bodyPartId
                 WHERE ebp.exerciseId = e.id) AS bodyParts,
               (SELECT GROUP_CONCAT(m.name) FROM exercise_muscles em
                 INNER JOIN muscles m ON m.id = em.muscleId
                 WHERE em.exerciseId = e.id AND em.isTarget = 1) AS targetMuscles,
               (SELECT GROUP_CONCAT(m2.name) FROM exercise_muscles em2
                 INNER JOIN muscles m2 ON m2.id = em2.muscleId
                 WHERE em2.exerciseId = e.id AND em2.isTarget = 0) AS secondaryMuscles,
               (SELECT GROUP_CONCAT(eq.name) FROM exercise_equipments eeq
                 INNER JOIN equipments eq ON eq.id = eeq.equipmentId
                 WHERE eeq.exerciseId = e.id) AS equipments
        FROM exercises e
        WHERE (e.userId IS NULL OR e.userId = :userId) AND e.deletedAt IS NULL
        ORDER BY e.name COLLATE NOCASE
        """
    )
    fun observeCatalogDetails(userId: String): Flow<List<ExerciseCatalogRow>>

    @Query(
        """
        SELECT e.id AS id,
               e.userId AS userId,
               e.name AS name,
               e.iconKey AS iconKey,
               e.instructions AS instructions,
               (SELECT GROUP_CONCAT(bp.name) FROM exercise_body_parts ebp
                 INNER JOIN body_parts bp ON bp.id = ebp.bodyPartId
                 WHERE ebp.exerciseId = e.id) AS bodyParts,
               (SELECT GROUP_CONCAT(m.name) FROM exercise_muscles em
                 INNER JOIN muscles m ON m.id = em.muscleId
                 WHERE em.exerciseId = e.id AND em.isTarget = 1) AS targetMuscles,
               (SELECT GROUP_CONCAT(m2.name) FROM exercise_muscles em2
                 INNER JOIN muscles m2 ON m2.id = em2.muscleId
                 WHERE em2.exerciseId = e.id AND em2.isTarget = 0) AS secondaryMuscles,
               (SELECT GROUP_CONCAT(eq.name) FROM exercise_equipments eeq
                 INNER JOIN equipments eq ON eq.id = eeq.equipmentId
                 WHERE eeq.exerciseId = e.id) AS equipments
        FROM exercises e
        WHERE e.id = :exerciseId
        """
    )
    suspend fun getCatalogDetail(exerciseId: String): ExerciseCatalogRow?

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: String): ExerciseEntity?

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int

    @Query("SELECT * FROM body_parts ORDER BY name")
    fun observeBodyParts(): Flow<List<BodyPartEntity>>

    @Query("SELECT * FROM muscles ORDER BY name")
    fun observeMuscles(): Flow<List<MuscleEntity>>

    @Query("SELECT * FROM equipments ORDER BY name")
    fun observeEquipments(): Flow<List<EquipmentEntity>>

    /**
     * Alta de un catalogo cerrado. Se ignora el conflicto porque el nombre es unico y dos
     * ejercicios pueden pedir la misma etiqueta a la vez.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBodyPart(bodyPart: BodyPartEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMuscle(muscle: MuscleEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEquipment(equipment: EquipmentEntity)

    @Upsert
    suspend fun upsertExercise(exercise: ExerciseEntity)

    @Upsert
    suspend fun upsertExercises(exercises: List<ExerciseEntity>)

    @Upsert
    suspend fun upsertBodyParts(bodyParts: List<BodyPartEntity>)

    @Upsert
    suspend fun upsertMuscles(muscles: List<MuscleEntity>)

    @Upsert
    suspend fun upsertEquipments(equipments: List<EquipmentEntity>)

    @Upsert
    suspend fun upsertExerciseBodyParts(refs: List<ExerciseBodyPartCrossRef>)

    @Upsert
    suspend fun upsertExerciseMuscles(refs: List<ExerciseMuscleCrossRef>)

    @Upsert
    suspend fun upsertExerciseEquipments(refs: List<ExerciseEquipmentCrossRef>)

    /**
     * Las tablas puente no se sincronizan: describen la clasificacion del ejercicio, no un dato
     * del usuario. Por eso se borran de verdad al reescribir sus etiquetas.
     */
    @Query("DELETE FROM exercise_body_parts WHERE exerciseId = :exerciseId")
    suspend fun clearBodyPartsOf(exerciseId: String)

    @Query("DELETE FROM exercise_muscles WHERE exerciseId = :exerciseId")
    suspend fun clearMusclesOf(exerciseId: String)

    @Query("DELETE FROM exercise_equipments WHERE exerciseId = :exerciseId")
    suspend fun clearEquipmentsOf(exerciseId: String)

    @Query(
        """
        UPDATE exercises SET deletedAt = :now, syncState = 'PENDING', updatedAt = :now
        WHERE id = :exerciseId AND userId IS NOT NULL
        """
    )
    suspend fun softDeleteCustomExercise(exerciseId: String, now: Instant)

    @Query("SELECT id FROM muscles WHERE name = :name LIMIT 1")
    suspend fun findMuscleId(name: String): Int?

    @Query("SELECT id FROM body_parts WHERE name = :name LIMIT 1")
    suspend fun findBodyPartId(name: String): Int?

    @Query("SELECT id FROM equipments WHERE name = :name LIMIT 1")
    suspend fun findEquipmentId(name: String): Int?
}
