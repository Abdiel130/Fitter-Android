package com.acdev.fitter.data.local.entity

import com.acdev.fitter.domain.model.SyncState
import java.time.Instant

/**
 * Columnas de sincronizacion comunes a toda entidad propiedad del usuario.
 *
 * Se incrusta con `@Embedded` para que el esquema quede identico en todas las tablas y el motor
 * de sincronizacion pueda tratarlas de forma uniforme.
 *
 * `deletedAt` implementa borrado logico: una fila borrada en local sigue existiendo hasta que el
 * servidor confirma la baja.
 */
data class SyncMetadata(
    val syncState: SyncState = SyncState.PENDING,
    val updatedAt: Instant = Instant.EPOCH,
    val deletedAt: Instant? = null
)
