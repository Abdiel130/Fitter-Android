package com.acdev.fitter.core.network

import android.util.Log
import com.acdev.fitter.core.connectivity.ConnectivityObserver
import com.acdev.fitter.data.session.TokenStorage
import com.acdev.fitter.domain.model.RemoteResult
import com.acdev.fitter.domain.model.shouldClearSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

private const val TAG = "TokenAuthenticator"
private const val MAX_ATTEMPTS = 2
private const val RETRIED = "true"

/**
 * Renueva la sesion cuando el servidor responde 401 y reintenta la peticion original.
 *
 * Cuatro guardas, todas necesarias:
 *
 * El contador de respuestas y el marcador de reintento cortan el bucle si el servidor insiste en
 * rechazar el token nuevo.
 *
 * El candado evita que dos peticiones que caducan a la vez pidan dos huellas y gasten dos veces el
 * token de renovacion, que el servidor destruye al primer uso.
 *
 * Sin conexion no se intenta nada: los datos se quedan en la cola local, que es el comportamiento
 * correcto de una app que funciona sin red.
 */
class TokenAuthenticator(
    private val tokenStorage: TokenStorage,
    private val refresher: TokenRefresher,
    private val connectivityObserver: ConnectivityObserver,
    private val onSessionRejected: suspend () -> Unit
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= MAX_ATTEMPTS) return null
        if (response.request.header(HEADER_RETRIED) != null) return null
        if (!isOnline()) return null

        return synchronized(lock) { renewedRequest(response) }
    }

    private fun renewedRequest(response: Response): Request? {
        val used = response.request.header(HEADER_AUTHORIZATION)?.removeBearer()
        val stored = runBlocking { tokenStorage.accessToken() }

        // Otro hilo ya renovo mientras este esperaba el candado: basta con reintentar.
        if (stored != null && stored != used) return response.retryWith(stored)

        val renewed = runBlocking { rotate() } ?: return null
        return response.retryWith(renewed)
    }

    private suspend fun rotate(): String? {
        val refreshToken = tokenStorage.refreshToken() ?: return null

        return when (val result = refresher.refresh(refreshToken)) {
            is RemoteResult.Success -> result.value.accessToken
                .takeIf { tokenStorage.save(result.value) }
            is RemoteResult.Failure -> null.also { dismissIfRejected(result) }
        }
    }

    /**
     * Solo un 401 explicito del servidor cierra la sesion. Un fallo de red o del servidor deja los
     * tokens donde estan para reintentar mas tarde.
     */
    private suspend fun dismissIfRejected(failure: RemoteResult.Failure) {
        if (!shouldClearSession(failure.error)) return
        Log.i(TAG, "El servidor rechazo el token de renovacion: hay que volver a entrar")
        onSessionRejected()
    }

    private fun isOnline(): Boolean = runBlocking { connectivityObserver.isOnline.first() }

    private fun Response.retryWith(accessToken: String): Request = request.newBuilder()
        .header(HEADER_AUTHORIZATION, "$BEARER_PREFIX $accessToken")
        .header(HEADER_RETRIED, RETRIED)
        .build()

    private fun String.removeBearer(): String = removePrefix("$BEARER_PREFIX ")

    private fun responseCount(response: Response): Int =
        generateSequence(response) { it.priorResponse }.count()
}
