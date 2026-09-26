package com.acdev.fitter.domain.usecase

import com.acdev.fitter.domain.model.RemoteError
import com.acdev.fitter.domain.model.RemoteResult
import com.acdev.fitter.domain.repository.AuthRepository
import com.acdev.fitter.domain.repository.PreferencesRepository
import com.acdev.fitter.domain.repository.SessionRepository
import com.acdev.fitter.domain.repository.UserRepository
import kotlinx.coroutines.flow.first

private const val UNSAVED_SESSION = "No se pudo guardar la sesion en este dispositivo"

/**
 * Inicia sesion y deja el dispositivo listo.
 *
 * Nada de esto borra la base local: si el dispositivo ya tenia entrenamientos, se atan a la cuenta
 * en lugar de descartarse, para que la sincronizacion sepa de quien son.
 */
class SignInUseCase(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
    private val preferencesRepository: PreferencesRepository,
    private val userRepository: UserRepository,
    private val syncSetupFlag: SyncSetupFlagUseCase
) {

    suspend operator fun invoke(email: String, password: String): RemoteResult<Unit> {
        val result = authRepository.signIn(email.trim(), password)
        if (result is RemoteResult.Failure) return result

        val account = (result as RemoteResult.Success).value
        val opened = sessionRepository.open(account.user, account.tokens)
        if (!opened) return RemoteResult.Failure(RemoteError.Unexpected(UNSAVED_SESSION))

        preferencesRepository.setAccount(account.user.name, account.user.email)
        userRepository.currentUserId.first()?.let { localId ->
            userRepository.linkRemoteId(localId, account.user.id)
        }
        syncSetupFlag.reconcile(account.user.setupCompleted)

        return RemoteResult.Success(Unit)
    }
}
