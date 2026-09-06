package com.acdev.fitter.data.repository

import com.acdev.fitter.core.time.AppClock
import com.acdev.fitter.core.util.IdGenerator
import com.acdev.fitter.data.local.dao.ExerciseCatalogRow
import com.acdev.fitter.data.local.dao.ExerciseDao
import com.acdev.fitter.data.local.dao.TrainingDao
import com.acdev.fitter.data.local.entity.BodyPartEntity
import com.acdev.fitter.data.local.entity.EquipmentEntity
import com.acdev.fitter.data.local.entity.ExerciseBodyPartCrossRef
import com.acdev.fitter.data.local.entity.ExerciseEntity
import com.acdev.fitter.data.local.entity.ExerciseEquipmentCrossRef
import com.acdev.fitter.data.local.entity.ExerciseMuscleCrossRef
import com.acdev.fitter.data.local.entity.MuscleEntity
import com.acdev.fitter.data.local.entity.SyncMetadata
import com.acdev.fitter.domain.model.ExerciseDetail
import com.acdev.fitter.domain.model.ExerciseDraft
import com.acdev.fitter.domain.model.ExerciseFacets
import com.acdev.fitter.domain.model.ExerciseIcon
import com.acdev.fitter.domain.model.ExerciseSummary
import com.acdev.fitter.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class ExerciseRepositoryImpl(
    private val exerciseDao: ExerciseDao,
    private val trainingDao: TrainingDao,
    private val clock: AppClock,
    private val idGenerator: IdGenerator
) : ExerciseRepository {

    override fun observeCatalog(userId: String): Flow<List<ExerciseSummary>> =
        exerciseDao.observeCatalogDetails(userId).map { rows ->
            rows.map { row ->
                ExerciseSummary(
                    id = row.id,
                    name = row.name,
                    icon = ExerciseIcon.fromKey(row.iconKey),
                    bodyParts = row.bodyParts.toNames(),
                    targetMuscles = row.targetMuscles.toNames(),
                    equipments = row.equipments.toNames(),
                    isCustom = row.userId != null
                )
            }
        }

    override fun observeFacets(): Flow<ExerciseFacets> = combine(
        exerciseDao.observeBodyParts(),
        exerciseDao.observeMuscles(),
        exerciseDao.observeEquipments()
    ) { bodyParts, muscles, equipments ->
        ExerciseFacets(
            bodyParts = bodyParts.map(BodyPartEntity::name),
            muscles = muscles.map(MuscleEntity::name),
            equipments = equipments.map(EquipmentEntity::name)
        )
    }

    override suspend fun getDetail(exerciseId: String): ExerciseDetail? =
        exerciseDao.getCatalogDetail(exerciseId)?.toDetail()

    override suspend fun saveCustomExercise(userId: String, draft: ExerciseDraft): String {
        val now = clock.now()
        val exerciseId = draft.id ?: idGenerator.newId()
        val stored = draft.id?.let { exerciseDao.getById(it) }

        exerciseDao.upsertExercise(
            ExerciseEntity(
                id = exerciseId,
                userId = userId,
                externalId = stored?.externalId,
                name = draft.name.trim(),
                gifUrl = stored?.gifUrl,
                instructions = draft.instructions
                    .map(String::trim)
                    .filter(String::isNotEmpty)
                    .takeIf(List<String>::isNotEmpty)
                    ?.let(json::encodeToString),
                iconKey = draft.icon.name,
                createdAt = stored?.createdAt ?: now,
                sync = stored?.sync?.pending(now) ?: SyncMetadata(updatedAt = now)
            )
        )

        writeBodyParts(exerciseId, draft.bodyParts)
        writeMuscles(exerciseId, draft.targetMuscles, draft.secondaryMuscles)
        writeEquipments(exerciseId, draft.equipments)
        return exerciseId
    }

    override suspend fun deleteCustomExercise(exerciseId: String) {
        exerciseDao.softDeleteCustomExercise(exerciseId, clock.now())
    }

    override suspend fun countUsages(exerciseId: String): Int =
        trainingDao.countUsagesOfExercise(exerciseId)

    private suspend fun writeBodyParts(exerciseId: String, names: List<String>) {
        exerciseDao.clearBodyPartsOf(exerciseId)
        val refs = names.cleaned().mapNotNull { name ->
            exerciseDao.insertBodyPart(BodyPartEntity(name = name))
            exerciseDao.findBodyPartId(name)?.let { ExerciseBodyPartCrossRef(exerciseId, it) }
        }
        exerciseDao.upsertExerciseBodyParts(refs)
    }

    private suspend fun writeMuscles(
        exerciseId: String,
        targets: List<String>,
        secondary: List<String>
    ) {
        exerciseDao.clearMusclesOf(exerciseId)
        val targetNames = targets.cleaned()
        // Un musculo no puede ser objetivo y secundario a la vez: gana el objetivo.
        val secondaryNames = secondary.cleaned().filterNot(targetNames::contains)

        val refs = targetNames.toRefs(exerciseId, isTarget = true) +
            secondaryNames.toRefs(exerciseId, isTarget = false)
        exerciseDao.upsertExerciseMuscles(refs)
    }

    private suspend fun List<String>.toRefs(
        exerciseId: String,
        isTarget: Boolean
    ): List<ExerciseMuscleCrossRef> = mapNotNull { name ->
        exerciseDao.insertMuscle(MuscleEntity(name = name))
        exerciseDao.findMuscleId(name)?.let { ExerciseMuscleCrossRef(exerciseId, it, isTarget) }
    }

    private suspend fun writeEquipments(exerciseId: String, names: List<String>) {
        exerciseDao.clearEquipmentsOf(exerciseId)
        val refs = names.cleaned().mapNotNull { name ->
            exerciseDao.insertEquipment(EquipmentEntity(name = name))
            exerciseDao.findEquipmentId(name)?.let { ExerciseEquipmentCrossRef(exerciseId, it) }
        }
        exerciseDao.upsertExerciseEquipments(refs)
    }

    private fun ExerciseCatalogRow.toDetail(): ExerciseDetail = ExerciseDetail(
        id = id,
        name = name,
        icon = ExerciseIcon.fromKey(iconKey),
        instructions = decodeInstructions(instructions),
        bodyParts = bodyParts.toNames(),
        targetMuscles = targetMuscles.toNames(),
        secondaryMuscles = secondaryMuscles.toNames(),
        isCustom = userId != null,
        equipments = equipments.toNames()
    )

    /**
     * Los pasos llegan como array JSON desde ExerciseDB. Un texto corrupto no debe impedir abrir
     * el ejercicio, asi que se degrada a lista vacia dejando rastro en el registro.
     */
    private fun decodeInstructions(raw: String?): List<String> {
        val text = raw?.takeIf(String::isNotBlank) ?: return emptyList()
        return runCatching { json.decodeFromString<List<String>>(text) }
            .onFailure { error ->
                android.util.Log.w(TAG, "Instrucciones ilegibles, se ignoran", error)
            }
            .getOrDefault(emptyList())
    }

    private companion object {
        const val TAG = "ExerciseRepository"
        val json = Json { ignoreUnknownKeys = true }
    }
}

private fun List<String>.cleaned(): List<String> =
    map(String::trim).filter(String::isNotEmpty).distinct()

/** `GROUP_CONCAT` devuelve los nombres separados por coma, o nulo si no hay ninguno. */
private fun String?.toNames(): List<String> =
    this?.split(',')?.map(String::trim)?.filter(String::isNotEmpty).orEmpty()
