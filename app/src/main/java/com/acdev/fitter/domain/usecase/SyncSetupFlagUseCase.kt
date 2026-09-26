package com.acdev.fitter.domain.usecase

import com.acdev.fitter.domain.model.RemoteResult
import com.acdev.fitter.domain.repository.AuthRepository
import com.acdev.fitter.domain.repository.PreferencesRepository
import com.acdev.fitter.domain.repository.SessionRepository
import kotlinx.coroutines.flow.first

/**
 * Mantiene alineada la bandera del asistente entre dispositivo y servidor.
 *
 * El espejo local se escribe primero para que la app funcione sin red; el PATCH es idempotente, asi
 * que reintentarlo mas tarde no cuesta nada ni mueve la fecha que ya guardo el servidor.
 */
class SyncSetupFlagUseCase(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
    private val preferencesRepository: PreferencesRepository
) {

    suspend operator fun invoke(completed: Boolean): RemoteResult<Boolean> {
        sessionRepository.setSetupCompleted(completed)
        return authRepository.pushSetupCompleted(completed)
    }

    /**
     * Reconcilia tras iniciar sesion.
     *
     * Si el dispositivo dice que el asistente ya se completo y el servidor dice que no, gana el
     * dispositivo y se reenvia el PATCH: el usuario paso por el asistente aqui y esa es la verdad.
     * Solo una accion explicita suya deberia enviar un false.
     */
    suspend fun reconcile(remoteCompleted: Boolean) {
        val localCompleted = preferencesRepository.preferences.first().onboardingCompleted

        when {
            localCompleted && !remoteCompleted -> invoke(completed = true)
            else -> sessionRepository.setSetupCompleted(remoteCompleted)
        }
    }
}
