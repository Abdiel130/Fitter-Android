package com.acdev.fitter.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acdev.fitter.domain.model.AppPreferences
import com.acdev.fitter.domain.model.FitterPalette
import com.acdev.fitter.domain.model.RootTarget
import com.acdev.fitter.domain.model.SyncStatus
import com.acdev.fitter.domain.model.ThemeMode
import com.acdev.fitter.domain.model.WeightUnit
import com.acdev.fitter.domain.model.resolveRootTarget
import com.acdev.fitter.domain.repository.PreferencesRepository
import com.acdev.fitter.domain.repository.SessionRepository
import com.acdev.fitter.domain.repository.SyncRepository
import com.acdev.fitter.domain.usecase.SignOutUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Estado global: sesion, tema, unidades y estado de la cola de sincronizacion.
 *
 * `preferences` empieza en null para distinguir "todavia no lo se" de "no ha completado el
 * asistente". Sin esa distincion la app parpadearia mostrando el asistente durante el arranque.
 */
class AppViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val signOutUseCase: SignOutUseCase,
    sessionRepository: SessionRepository,
    syncRepository: SyncRepository
) : ViewModel() {

    val preferences: StateFlow<AppPreferences?> = preferencesRepository.preferences
        .map { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Que pinta la raiz. La decision vive en el dominio y aqui solo se observa. */
    val root: StateFlow<RootTarget> =
        combine(sessionRepository.state, preferences, ::resolveRootTarget)
            .stateIn(viewModelScope, SharingStarted.Eagerly, RootTarget.BOOT)

    val account: StateFlow<String> = preferencesRepository.preferences
        .map { it.email }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), "")

    val syncStatus: StateFlow<SyncStatus> = syncRepository.status
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SyncStatus())

    fun setPalette(palette: FitterPalette) {
        viewModelScope.launch { preferencesRepository.setPalette(palette) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    fun setWeightUnit(unit: WeightUnit) {
        viewModelScope.launch { preferencesRepository.setWeightUnit(unit) }
    }

    fun signOut() {
        viewModelScope.launch { signOutUseCase() }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
