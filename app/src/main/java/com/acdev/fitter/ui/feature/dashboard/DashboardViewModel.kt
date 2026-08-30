package com.acdev.fitter.ui.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acdev.fitter.core.time.AppClock
import com.acdev.fitter.domain.model.DashboardSnapshot
import com.acdev.fitter.domain.repository.RecoveryRepository
import com.acdev.fitter.domain.repository.UserRepository
import com.acdev.fitter.domain.usecase.ObserveDashboardUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Momento del dia con el que se saluda al usuario. */
enum class Greeting { MORNING, AFTERNOON, EVENING }

/** Estado que consume la pantalla de inicio. */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val greeting: Greeting = Greeting.MORNING,
    val snapshot: DashboardSnapshot = DashboardSnapshot()
)

/** Cantidad que suma el acceso rapido de hidratacion: un vaso. */
private const val WATER_QUICK_ADD_ML = 250

private const val AFTERNOON_FROM_HOUR = 13
private const val EVENING_FROM_HOUR = 20

class DashboardViewModel(
    observeDashboard: ObserveDashboardUseCase,
    private val userRepository: UserRepository,
    private val recoveryRepository: RecoveryRepository,
    private val clock: AppClock
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = observeDashboard()
        .map { snapshot ->
            DashboardUiState(
                isLoading = false,
                greeting = greetingForNow(),
                snapshot = snapshot
            )
        }
        .stateIn(
            scope = viewModelScope,
            // La suscripcion sobrevive a un giro de pantalla sin recargar la base.
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = DashboardUiState()
        )

    private val currentUserId: StateFlow<String?> = userRepository.currentUserId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    fun addWaterGlass() {
        val userId = currentUserId.value ?: return
        viewModelScope.launch {
            recoveryRepository.addWater(userId, clock.today(), WATER_QUICK_ADD_ML)
        }
    }

    private fun greetingForNow(): Greeting {
        val hour = clock.now().atZone(clock.zone()).hour
        return when {
            hour >= EVENING_FROM_HOUR -> Greeting.EVENING
            hour >= AFTERNOON_FROM_HOUR -> Greeting.AFTERNOON
            else -> Greeting.MORNING
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
