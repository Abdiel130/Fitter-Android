package com.acdev.fitter.core.network

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface BaseUrlResolver {
    /**
     * Direccion efectiva. Sondea una sola vez y la recuerda; las llamadas concurrentes esperan al
     * mismo sondeo en lugar de lanzar uno cada una.
     */
    suspend fun resolve(): String

    /**
     * Olvida la direccion en uso y la recordada, de modo que la proxima llamada vuelva a sondear.
     * Se llama al fijar una direccion a mano y cuando un intento de login se queda sin red: es la
     * senal de que se cambio de red y la IP recordada ya no existe.
     */
    suspend fun invalidate()
}

/**
 * Elige a que servidor hablar.
 *
 * Orden de precedencia: lo que el usuario fijo a mano, luego la ultima que funciono, y solo si no
 * hay ninguna se sondean las candidatas en orden. En produccion el sondeo esta apagado, asi que
 * devuelve la unica candidata sin tocar la red.
 */
class DefaultBaseUrlResolver(
    private val candidates: List<String>,
    private val store: BaseUrlStore,
    private val probe: BaseUrlProbe,
    private val probeEnabled: Boolean
) : BaseUrlResolver {

    private val mutex = Mutex()

    @Volatile
    private var cached: String? = null

    init {
        require(candidates.isNotEmpty()) { "Hace falta al menos una direccion candidata" }
    }

    override suspend fun resolve(): String = cached ?: mutex.withLock {
        cached ?: pick().also { cached = it }
    }

    override suspend fun invalidate() {
        mutex.withLock {
            cached = null
            store.clearLastGood()
        }
    }

    private suspend fun pick(): String {
        val manual = store.manualUrl()
        if (manual.isNotBlank()) return manual
        if (!probeEnabled) return candidates.first()

        val remembered = store.lastGoodUrl()
        if (remembered.isNotBlank()) return remembered

        return probeFirstReachable()
    }

    /**
     * Si ninguna responde se devuelve la primera: la peticion fallara con un error de red, que es
     * exactamente lo que la interfaz sabe contar al usuario.
     */
    private suspend fun probeFirstReachable(): String {
        val winner = candidates.firstOrNull { probe.isReachable(it) } ?: return candidates.first()
        store.saveLastGood(winner)
        return winner
    }
}
