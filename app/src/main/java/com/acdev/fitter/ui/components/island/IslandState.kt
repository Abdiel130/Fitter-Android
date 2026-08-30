package com.acdev.fitter.ui.components.island

import androidx.compose.runtime.Immutable
import com.acdev.fitter.domain.model.SyncStatus

/**
 * Estados que puede adoptar la isla dinamica.
 *
 * Es un contrato cerrado a proposito: la isla nunca muestra algo que no este aqui, y cada estado
 * trae ya resuelto lo que necesita pintar.
 */
@Immutable
sealed interface IslandState {

    /** Clave de contenido: identifica la forma, no los datos. Evita reanimar en cada segundo. */
    val kind: String

    /** Reposo. Solo un punto con el color del estado de la cola. */
    data class Idle(val sync: SyncStatus) : IslandState {
        override val kind: String = "idle"
    }

    /** Subida en curso. */
    data class Syncing(val pendingCount: Int) : IslandState {
        override val kind: String = "syncing"
    }

    /** Cronometro de descanso entre series. */
    data class Rest(
        val remainingSeconds: Int,
        val totalSeconds: Int,
        val exerciseName: String
    ) : IslandState {
        override val kind: String = "rest"

        val progress: Float
            get() = when {
                totalSeconds <= 0 -> 0f
                else -> (remainingSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f)
            }
    }

    /** Record personal recien conseguido. */
    data class Record(
        val exerciseName: String,
        val weightLabel: String,
        val reps: Int,
        val deltaLabel: String
    ) : IslandState {
        override val kind: String = "record"
    }
}

/**
 * Prioridad entre estados cuando varios estan activos a la vez.
 *
 * Un record tapa un descanso, y un descanso tapa la sincronizacion: lo que el usuario acaba de
 * lograr pesa mas que lo que la app hace por su cuenta.
 */
fun resolveIslandState(
    record: IslandState.Record?,
    rest: IslandState.Rest?,
    syncStatus: SyncStatus
): IslandState = when {
    record != null -> record
    rest != null -> rest
    syncStatus.isSyncing -> IslandState.Syncing(syncStatus.pendingCount)
    else -> IslandState.Idle(syncStatus)
}
