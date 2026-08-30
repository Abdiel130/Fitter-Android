package com.acdev.fitter.domain.usecase

import com.acdev.fitter.core.time.AppClock
import com.acdev.fitter.domain.model.DashboardSnapshot
import com.acdev.fitter.domain.repository.NutritionRepository
import com.acdev.fitter.domain.repository.RecoveryRepository
import com.acdev.fitter.domain.repository.SyncRepository
import com.acdev.fitter.domain.repository.TrainingRepository
import com.acdev.fitter.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters

/**
 * Reune en un unico flujo todo lo que muestra el dashboard.
 *
 * La pantalla no sabe que existen cinco repositorios: recibe una foto coherente y la pinta. Si
 * manana el volumen semanal se calcula de otra forma, cambia aqui y no en la UI.
 */
class ObserveDashboardUseCase(
    private val userRepository: UserRepository,
    private val trainingRepository: TrainingRepository,
    private val nutritionRepository: NutritionRepository,
    private val recoveryRepository: RecoveryRepository,
    private val syncRepository: SyncRepository,
    private val clock: AppClock
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<DashboardSnapshot> =
        userRepository.currentUserId.flatMapLatest { userId ->
            when (userId) {
                null -> flowOf(DashboardSnapshot())
                else -> snapshotFor(userId)
            }
        }

    private fun snapshotFor(userId: String): Flow<DashboardSnapshot> {
        val today = clock.today()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        val trainingAndFood = combine(
            trainingRepository.observeNextSession(userId),
            trainingRepository.observeWeeklyVolume(userId, weekStart),
            nutritionRepository.observeDailySummary(userId, today)
        ) { nextSession, weeklyVolume, nutrition ->
            DashboardSnapshot(
                nextSession = nextSession,
                weeklyVolume = weeklyVolume,
                nutrition = nutrition
            )
        }

        val recovery = combine(
            recoveryRepository.observeHydration(userId, today),
            recoveryRepository.observeSleep(userId, today),
            recoveryRepository.observeSupplementProgress(userId, today)
        ) { hydration, sleep, supplements ->
            Triple(hydration, sleep, supplements)
        }

        return combine(
            trainingAndFood,
            recovery,
            syncRepository.status,
            userRepository.currentUserName
        ) { base, (hydration, sleep, supplements), syncStatus, userName ->
            base.copy(
                userName = userName,
                hydration = hydration,
                sleep = sleep,
                supplements = supplements,
                syncStatus = syncStatus
            )
        }
    }
}
