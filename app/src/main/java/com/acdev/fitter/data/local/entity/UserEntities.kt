package com.acdev.fitter.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Usuario local. Mientras no exista servidor, la app crea un unico usuario en el asistente de
 * arranque y trabaja siempre con el.
 *
 * `passwordHash` queda fuera de la base local a proposito: la credencial nunca se guarda en el
 * dispositivo. Los tokens de sesion viven cifrados en DataStore, no aqui.
 */
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["remoteId"])
    ]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    /**
     * Id de la cuenta en el servidor. Null mientras nadie haya iniciado sesion en este dispositivo.
     *
     * La clave primaria no se reasigna al enlazar porque la referencian veintisiete tablas; este
     * enlace es lo que permitira a la sincronizacion saber a quien pertenece cada fila.
     */
    val remoteId: String? = null,
    /** Rutina en curso. Referencia debil: la rutina puede no existir todavia. */
    val currentRoutineId: String? = null,
    /** Puntero al `orderIndex` del workout que toca hoy dentro de la rutina. */
    val activeSequenceIndex: Int = 1,
    val createdAt: Instant,
    @Embedded val sync: SyncMetadata = SyncMetadata()
)
