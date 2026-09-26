package com.acdev.fitter.data.remote

import com.acdev.fitter.core.network.TokenRefresher
import com.acdev.fitter.data.remote.api.AuthApi
import com.acdev.fitter.data.remote.mapper.toDomainOrNull
import com.acdev.fitter.domain.model.RemoteError
import com.acdev.fitter.domain.model.RemoteResult
import com.acdev.fitter.domain.model.SessionTokens
import kotlinx.serialization.json.Json

private const val UNREADABLE_TOKENS = "Los tokens renovados llegaron con fechas ilegibles"

/**
 * Renovacion sobre un `AuthApi` construido con un cliente sin autenticador.
 *
 * El servidor destruye el token presentado y devuelve un par nuevo, asi que esta llamada no se
 * puede reintentar a ciegas: si falla, hay que volver a pedir la contrasena.
 */
class RemoteTokenRefresher(
    private val authApi: AuthApi,
    private val json: Json
) : TokenRefresher {

    override suspend fun refresh(refreshToken: String): RemoteResult<SessionTokens> {
        val result = apiCall(json) { authApi.refresh("Bearer $refreshToken") }

        return when (result) {
            is RemoteResult.Failure -> result
            is RemoteResult.Success -> result.value.tokens.toDomainOrNull()
                ?.let { RemoteResult.Success(it) }
                ?: RemoteResult.Failure(RemoteError.Unexpected(UNREADABLE_TOKENS))
        }
    }
}
