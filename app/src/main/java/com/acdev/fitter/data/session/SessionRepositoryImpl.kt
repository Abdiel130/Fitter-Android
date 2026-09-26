package com.acdev.fitter.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.acdev.fitter.core.time.AppClock
import com.acdev.fitter.domain.model.RemoteUser
import com.acdev.fitter.domain.model.SessionState
import com.acdev.fitter.domain.model.SessionTokens
import com.acdev.fitter.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Sesion sobre DataStore.
 *
 * Hay sesion cuando existe el id del servidor y el token de renovacion todavia no ha caducado: sin
 * el, la app no podria volver a hablar con el servidor sin pedir la contrasena, asi que su
 * vencimiento es el que marca el final de la sesion.
 */
class SessionRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val tokenStorage: TokenStorage,
    private val clock: AppClock
) : SessionRepository {

    override val state: Flow<SessionState> = dataStore.data.map { stored ->
        val userId = stored[SessionKeys.remoteUserId]
        val refreshExpiry = stored[SessionKeys.refreshExpiresAt]

        when {
            userId == null || refreshExpiry == null -> SessionState.SignedOut
            refreshExpiry <= clock.now().toEpochMilli() -> SessionState.SignedOut
            else -> SessionState.SignedIn(
                remoteUserId = userId,
                setupCompleted = stored[SessionKeys.setupCompleted] == true
            )
        }
    }

    override val remoteUserId: Flow<String?> =
        dataStore.data.map { it[SessionKeys.remoteUserId] }

    /**
     * Los tokens se guardan antes que el usuario: si protegerlos falla no queda una sesion a medias
     * que apunte a una cuenta sin credenciales con las que hablar.
     */
    override suspend fun open(user: RemoteUser, tokens: SessionTokens): Boolean {
        if (!tokenStorage.save(tokens)) return false

        dataStore.edit { stored ->
            stored[SessionKeys.remoteUserId] = user.id
            stored[SessionKeys.setupCompleted] = user.setupCompleted
        }
        return true
    }

    override suspend fun setSetupCompleted(completed: Boolean) {
        dataStore.edit { it[SessionKeys.setupCompleted] = completed }
    }

    override suspend fun clearSession() {
        tokenStorage.clear()
        dataStore.edit { stored ->
            stored.remove(SessionKeys.remoteUserId)
            stored.remove(SessionKeys.setupCompleted)
        }
    }
}
