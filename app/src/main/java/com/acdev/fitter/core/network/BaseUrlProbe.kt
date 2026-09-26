package com.acdev.fitter.core.network

import android.util.Log
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

private const val TAG = "BaseUrlProbe"
private const val CONNECT_TIMEOUT_MILLIS = 900L
private const val CALL_TIMEOUT_MILLIS = 1200L
private const val PROBE_PATH = "api/auth/login"

interface BaseUrlProbe {
    /** True si el host responde algo, aunque sea un 405: lo que importa es que exista servidor. */
    suspend fun isReachable(baseUrl: String): Boolean
}

/**
 * Sondea con un cliente propio, sin interceptores ni autenticacion, y con margenes cortos para que
 * descartar un host caido cueste poco mas de un segundo.
 *
 * Pide la ruta de login con GET: el servidor contesta 405 porque solo acepta POST, y eso ya prueba
 * que hay alguien escuchando sin necesitar un endpoint de salud dedicado.
 */
class OkHttpBaseUrlProbe : BaseUrlProbe {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
            .callTimeout(CALL_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(false)
            .build()
    }

    /** La llamada es sincrona, asi que se saca del hilo principal antes de hacerla. */
    override suspend fun isReachable(baseUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(baseUrl + PROBE_PATH).build()
            client.newCall(request).execute().use { true }
        } catch (error: IOException) {
            Log.i(TAG, "Sin respuesta de $baseUrl", error)
            false
        } catch (error: IllegalArgumentException) {
            Log.w(TAG, "Direccion mal formada: $baseUrl", error)
            false
        }
    }
}
