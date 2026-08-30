package com.acdev.fitter.domain.model

import java.time.Instant

/**
 * Estado de sincronizacion de una fila local frente al servidor.
 *
 * Toda tabla que pertenece al usuario lo lleva. La UI nunca lo escribe: lo escribe el
 * repositorio al guardar y el motor de sincronizacion al confirmar.
 */
enum class SyncState {
    /** Escrito en local, pendiente de subir. */
    PENDING,

    /** Subida en curso. */
    SYNCING,

    /** Confirmado por el servidor. */
    SYNCED,

    /** La subida fallo; se reintentara con backoff. */
    FAILED,

    /** El servidor tiene una version distinta; requiere resolucion del usuario. */
    CONFLICT
}

/**
 * Resumen del estado de la cola, unico dato que consume el indicador de sincronizacion.
 *
 * La isla dinamica se pinta a partir de esto y de nada mas.
 */
data class SyncStatus(
    val pendingCount: Int = 0,
    val failedCount: Int = 0,
    val conflictCount: Int = 0,
    val isSyncing: Boolean = false,
    val isOnline: Boolean = true,
    val lastSyncAt: Instant? = null
) {
    val isUpToDate: Boolean
        get() = pendingCount == 0 && failedCount == 0 && conflictCount == 0
}
