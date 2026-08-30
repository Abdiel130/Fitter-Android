package com.acdev.fitter.data.local.dao

import androidx.room.Dao
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

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: String): ExerciseEntity?

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int

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

    @Query("SELECT id FROM muscles WHERE name = :name LIMIT 1")
    suspend fun findMuscleId(name: String): Int?

    @Query("SELECT id FROM body_parts WHERE name = :name LIMIT 1")
    suspend fun findBodyPartId(name: String): Int?

    @Query("SELECT id FROM equipments WHERE name = :name LIMIT 1")
    suspend fun findEquipmentId(name: String): Int?
}
