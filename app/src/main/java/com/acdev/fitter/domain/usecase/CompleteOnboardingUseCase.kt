package com.acdev.fitter.domain.usecase

import com.acdev.fitter.core.time.AppClock
import com.acdev.fitter.domain.model.SessionState
import com.acdev.fitter.domain.model.WeightUnit
import com.acdev.fitter.domain.repository.BodyRepository
import com.acdev.fitter.domain.repository.NutritionRepository
import com.acdev.fitter.domain.repository.PreferencesRepository
import com.acdev.fitter.domain.repository.RecoveryRepository
import com.acdev.fitter.domain.repository.SessionRepository
import com.acdev.fitter.domain.repository.TrainingRepository
import com.acdev.fitter.domain.repository.UserRepository
import kotlinx.coroutines.flow.first

/** Datos que el asistente de arranque recoge y entrega ya validados. */
data class OnboardingResult(
    val weightUnit: WeightUnit,
    /** Peso introducido en la unidad elegida por el usuario, o null si lo omitio. */
    val bodyWeight: Double?,
    val waistCm: Double?,
    val chestCm: Double?
)

/**
 * Deja la app lista para usarse tras el asistente.
 *
 * Es el unico sitio donde se decide que significa "primer arranque": atar el usuario local a la
 * cuenta que inicio sesion, instalar rutina, objetivos y suplementos de partida, guardar el primer
 * registro si lo hubo y marcar el asistente como visto.
 *
 * El nombre y el correo ya no se preguntan: los trae el inicio de sesion, que es la unica fuente
 * de verdad de la identidad.
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
    private val sessionRepository: SessionRepository,
    private val syncSetupFlag: SyncSetupFlagUseCase,
    private val clock: AppClock
) {

    suspend operator fun invoke(result: OnboardingResult) {
        // Sin sesion no hay cuenta a la que atribuir nada, y el asistente no deberia estar visible.
        val account = sessionRepository.state.first() as? SessionState.SignedIn ?: return
        val identity = preferencesRepository.preferences.first()

        val userId = userRepository.ensureLocalUser(
            name = identity.displayName.ifBlank { DEFAULT_USER_NAME },
            email = identity.email,
            remoteId = account.remoteUserId
        )

        trainingRepository.ensureStarterRoutine(userId)
        nutritionRepository.ensureDefaultTargets(userId)
        recoveryRepository.ensureStarterSupplements(userId)

        saveFirstMeasurement(userId, result)

        preferencesRepository.setWeightUnit(result.weightUnit)
        syncSetupFlag(completed = true)
        preferencesRepository.setOnboardingCompleted(true)
    }

    private suspend fun saveFirstMeasurement(userId: String, result: OnboardingResult) {
        val hasMeasurement =
            result.bodyWeight != null || result.waistCm != null || result.chestCm != null
        if (!hasMeasurement) return

        bodyRepository.saveMeasurement(
            userId = userId,
            date = clock.today(),
            weightKg = result.bodyWeight?.let(result.weightUnit::toKg),
            waistCm = result.waistCm,
            chestCm = result.chestCm
        )
    }

    private companion object {
        const val DEFAULT_USER_NAME = "Atleta"
    }
}
