package com.acdev.fitter.core.network

/**
 * Memoria de la direccion del servidor.
 *
 * Guarda dos cosas distintas: la ultima que respondio, que es una optimizacion, y la que el
 * usuario fijo a mano, que es una orden y manda sobre cualquier sondeo.
 */
interface BaseUrlStore {
    suspend fun manualUrl(): String
    suspend fun lastGoodUrl(): String
    suspend fun saveManual(url: String?)
    suspend fun saveLastGood(url: String)

    /** Olvida la ultima que respondio, para que el siguiente sondeo empiece de cero. */
    suspend fun clearLastGood()
}
