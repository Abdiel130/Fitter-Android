package com.acdev.fitter.ui.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acdev.fitter.domain.model.FitterPalette
import com.acdev.fitter.domain.model.ThemeMode
import com.acdev.fitter.domain.model.WeightUnit
import com.acdev.fitter.domain.repository.PreferencesRepository
import com.acdev.fitter.domain.usecase.CompleteOnboardingUseCase
import com.acdev.fitter.domain.usecase.OnboardingResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Pasos del asistente, en orden. */
enum class OnboardingStep { WELCOME, THEME, UNITS, FIRST_RECORD, ACCOUNT }

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val palette: FitterPalette = FitterPalette.MIDNIGHT,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val weightInput: String = "",
    val waistInput: String = "",
    val chestInput: String = "",
    val name: String = "",
    val email: String = "",
    val isSubmitting: Boolean = false
) {
    val isFirstStep: Boolean get() = step == OnboardingStep.WELCOME
    val isLastStep: Boolean get() = step == OnboardingStep.ACCOUNT
    val stepNumber: Int get() = step.ordinal + 1
    val stepCount: Int get() = OnboardingStep.entries.size
}

/**
 * Asistente de primer arranque.
 *
 * El tema se aplica en cuanto se elige, sin esperar a terminar: el usuario ve el resultado en la
 * misma pantalla en la que decide. El resto de datos se guarda al final, en una sola operacion.
 */
class OnboardingViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val completeOnboarding: CompleteOnboardingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun next() {
        val current = _state.value.step
        val nextStep = OnboardingStep.entries.getOrNull(current.ordinal + 1) ?: return
        _state.update { it.copy(step = nextStep) }
    }

    fun back() {
        val current = _state.value.step
        val previous = OnboardingStep.entries.getOrNull(current.ordinal - 1) ?: return
        _state.update { it.copy(step = previous) }
    }

    fun selectPalette(palette: FitterPalette) {
        _state.update { it.copy(palette = palette) }
        viewModelScope.launch { preferencesRepository.setPalette(palette) }
    }

    fun selectThemeMode(mode: ThemeMode) {
        _state.update { it.copy(themeMode = mode) }
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    fun selectWeightUnit(unit: WeightUnit) = _state.update { it.copy(weightUnit = unit) }

    fun updateWeight(value: String) = _state.update { it.copy(weightInput = value) }

    fun updateWaist(value: String) = _state.update { it.copy(waistInput = value) }

    fun updateChest(value: String) = _state.update { it.copy(chestInput = value) }

    fun updateName(value: String) = _state.update { it.copy(name = value) }

    fun updateEmail(value: String) = _state.update { it.copy(email = value) }

    /**
     * Cierra el asistente. Marcar la preferencia es lo ultimo que ocurre, de modo que si algo
     * falla antes el usuario vuelve a verlo en el siguiente arranque en lugar de entrar a una app
     * a medio configurar.
     */
    fun finish() {
        if (_state.value.isSubmitting) return
        val snapshot = _state.value
        _state.update { it.copy(isSubmitting = true) }

        viewModelScope.launch {
            completeOnboarding(
                OnboardingResult(
                    displayName = snapshot.name.trim(),
                    email = snapshot.email.trim(),
                    weightUnit = snapshot.weightUnit,
                    bodyWeight = snapshot.weightInput.toDecimalOrNull(),
                    waistCm = snapshot.waistInput.toDecimalOrNull(),
                    chestCm = snapshot.chestInput.toDecimalOrNull()
                )
            )
        }
    }
}

/**
 * Convierte lo que escribe el usuario en numero.
 *
 * Acepta coma o punto porque el teclado decimal de Android ofrece uno u otro segun el idioma.
 */
private fun String.toDecimalOrNull(): Double? =
    trim().replace(',', '.').toDoubleOrNull()
