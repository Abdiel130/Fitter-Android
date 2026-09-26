package com.acdev.fitter.domain.model

import java.time.Instant

/**
 * Par de tokens emitido por el servidor.
 *
 * El de acceso vale 30 dias y sirve para las rutas operativas; el de renovacion vale 180 y solo
 * sirve para pedir un par nuevo. El servidor destruye el de renovacion en cuanto se usa.
 */
data class SessionTokens(
    val accessToken: String,
    val accessExpiresAt: Instant,
    val refreshToken: String,
    val refreshExpiresAt: Instant
)

/** Usuario tal como lo describe el servidor. */
data class RemoteUser(
    val id: String,
    val name: String,
    val email: String,
    val currentRoutineId: String?,
    val activeSequenceIndex: Int,
    val setupCompleted: Boolean
)

/** Lo que devuelve un inicio de sesion correcto: quien entra y con que credenciales. */
data class SignedInAccount(val user: RemoteUser, val tokens: SessionTokens)

/**
 * Estado de la sesion.
 *
 * `Unknown` es el equivalente al `null` de las preferencias: distingue "todavia no se ha leido el
 * almacenamiento" de "no hay sesion", que llevan a pantallas distintas.
 */
sealed interface SessionState {
    data object Unknown : SessionState
    data object SignedOut : SessionState
    data class SignedIn(
        val remoteUserId: String,
        val setupCompleted: Boolean
    ) : SessionState
}
