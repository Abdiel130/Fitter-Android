package com.acdev.fitter.domain.usecase

import com.acdev.fitter.core.time.AppClock
import com.acdev.fitter.domain.model.WeightUnit
import com.acdev.fitter.domain.repository.BodyRepository
import com.acdev.fitter.domain.repository.NutritionRepository
import com.acdev.fitter.domain.repository.PreferencesRepository
import com.acdev.fitter.domain.repository.RecoveryRepository
import com.acdev.fitter.domain.repository.TrainingRepository
import com.acdev.fitter.domain.repository.UserRepository

/** Datos que el asistente de arranque recoge y entrega ya validados. */
data class OnboardingResult(
    val displayName: String,
    val email: String,
    val weightUnit: WeightUnit,
    /** Peso introducido en la unidad elegida por el usuario, o null si lo omitio. */
    val bodyWeight: Double?,
    val waistCm: Double?,
    val chestCm: Double?
)

/**
 * Deja la app lista para usarse tras el asistente.
 *
 * Es el unico sitio donde se decide que significa "primer arranque": crear el usuario local,
 * instalar rutina, objetivos y suplementos de partida, guardar el primer registro si lo hubo y
 * marcar el asistente como visto.
 *
 * El peso se normaliza a kilogramos aqui: la base nunca guarda libras.
 */
class CompleteOnboardingUseCase(
    private val userRepository: UserRepository,
    private val trainingRepository: TrainingRepository,
    private val nutritionRepository: NutritionRepository,
    private val recoveryRepository: RecoveryRepository,
    private val bodyRepository: BodyRepository,
    private val preferencesRepository: PreferencesRepository,
    private val clock: AppClock
) {

    suspend operator fun invoke(result: OnboardingResult) {
        val userId = userRepository.ensureLocalUser(
            name = result.displayName.ifBlank { DEFAULT_USER_NAME },
            email = result.email
        )

        trainingRepository.ensureStarterRoutine(userId)
        nutritionRepository.ensureDefaultTargets(userId)
        recoveryRepository.ensureStarterSupplements(userId)

        val hasMeasurement = result.bodyWeight != null || result.waistCm != null || result.chestCm != null
        if (hasMeasurement) {
            bodyRepository.saveMeasurement(
                userId = userId,
                date = clock.today(),
                weightKg = result.bodyWeight?.let(result.weightUnit::toKg),
                waistCm = result.waistCm,
                chestCm = result.chestCm
            )
        }

        preferencesRepository.setAccount(result.displayName, result.email)
        preferencesRepository.setWeightUnit(result.weightUnit)
        preferencesRepository.setOnboardingCompleted(true)
    }

    private companion object {
        const val DEFAULT_USER_NAME = "Atleta"
    }
}
