package com.acdev.fitter.data.repository

import com.acdev.fitter.core.connectivity.ConnectivityObserver
import com.acdev.fitter.data.local.dao.SyncDao
import com.acdev.fitter.domain.model.SyncStatus
import com.acdev.fitter.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Estado de la cola de sincronizacion.
 *
 * Hoy solo lee la base local, porque todavia no hay servidor: `isSyncing` sera cierto cuando el
 * trabajo de WorkManager este en curso. La UI ya consume el contrato definitivo, de modo que al
 * conectar el backend no cambia ni una pantalla.
 */
class SyncRepositoryImpl(
    private val syncDao: SyncDao,
    connectivityObserver: ConnectivityObserver
) : SyncRepository {

    override val status: Flow<SyncStatus> =
        combine(syncDao.observeCounts(), connectivityObserver.isOnline) { counts, online ->
            SyncStatus(
                pendingCount = counts.pendingCount,
                failedCount = counts.failedCount,
                conflictCount = counts.conflictCount,
                isSyncing = false,
                isOnline = online,
                lastSyncAt = null
            )
        }
}
