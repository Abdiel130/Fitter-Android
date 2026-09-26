package com.acdev.fitter.core.network

import com.acdev.fitter.domain.model.RemoteResult
import com.acdev.fitter.domain.model.SessionTokens

/**
 * Pide un par de tokens nuevo.
 *
 * Es una interfaz de un solo metodo a proposito: su implementacion vive sobre un cliente HTTP sin
 * autenticador, de modo que un 401 en la renovacion no puede volver a entrar en el autenticador y
 * provocar una recursion.
 */
fun interface TokenRefresher {
    suspend fun refresh(refreshToken: String): RemoteResult<SessionTokens>
}
