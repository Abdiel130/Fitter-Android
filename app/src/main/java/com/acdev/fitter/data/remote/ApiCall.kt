package com.acdev.fitter.data.remote

import android.util.Log
import com.acdev.fitter.domain.model.RemoteError
import com.acdev.fitter.domain.model.RemoteResult
import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.Response

private const val TAG = "ApiCall"
private const val EMPTY_DATA = "La respuesta llego sin cuerpo"

/**
 * Frontera de la capa de red.
 *
 * Es el unico sitio del proyecto donde existen excepciones de red: cualquier cosa que lance OkHttp
 * o el deserializador se convierte aqui en un `RemoteError`, de modo que ni los repositorios ni el
 * dominio necesitan `try` ni conocen a Retrofit.
 *
 * El orden de los `catch` importa: las tres subclases de [IOException] que significan "hay red pero
 * el servidor no esta" van antes que ella, porque el primero que encaja gana.
 */
suspend fun <T : Any> apiCall(
    json: Json,
    request: suspend () -> Response<ApiEnvelope<T>>
): RemoteResult<T> = try {
    request().toResult(json)
} catch (error: SocketTimeoutException) {
    Log.w(TAG, "El servidor tardo demasiado", error)
    RemoteResult.Failure(RemoteError.Timeout)
} catch (error: UnknownHostException) {
    Log.w(TAG, "El nombre del servidor no resuelve", error)
    RemoteResult.Failure(RemoteError.ServerUnreachable)
} catch (error: ConnectException) {
    Log.w(TAG, "El servidor rechazo la conexion", error)
    RemoteResult.Failure(RemoteError.ServerUnreachable)
} catch (error: NoRouteToHostException) {
    Log.w(TAG, "No hay ruta hasta el servidor", error)
    RemoteResult.Failure(RemoteError.ServerUnreachable)
} catch (error: IOException) {
    Log.w(TAG, "Fallo de red al hablar con el servidor", error)
    RemoteResult.Failure(RemoteError.Offline)
} catch (error: SerializationException) {
    Log.w(TAG, "Respuesta ilegible", error)
    RemoteResult.Failure(RemoteError.Unexpected(error.message.orEmpty()))
}

private fun <T : Any> Response<ApiEnvelope<T>>.toResult(json: Json): RemoteResult<T> {
    val envelope = body()
    return when {
        isSuccessful && envelope?.data != null -> RemoteResult.Success(envelope.data)
        isSuccessful -> RemoteResult.Failure(RemoteError.Unexpected(EMPTY_DATA))
        else -> RemoteResult.Failure(mapHttpFailure(code(), parseErrorEnvelope(json)))
    }
}

/**
 * Lee el sobre de un error. Devuelve null si no se puede parsear en lugar de lanzar: un 502 de un
 * proxy responde HTML, y eso no debe convertirse en un fallo distinto del que ya ocurrio.
 */
private fun Response<*>.parseErrorEnvelope(json: Json): ApiEnvelope<Unit>? {
    val raw = errorBody()?.string().orEmpty()
    if (raw.isBlank()) return null
    return try {
        json.decodeFromString<ApiEnvelope<Unit>>(raw)
    } catch (error: SerializationException) {
        Log.w(TAG, "Cuerpo de error no es un sobre de la API", error)
        null
    }
}
