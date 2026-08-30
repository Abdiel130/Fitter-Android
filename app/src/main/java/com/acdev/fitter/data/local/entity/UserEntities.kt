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
 * `passwordHash` queda fuera de la base local a proposito: la credencial no se guarda en el
 * dispositivo, se guardara el token de sesion cuando exista autenticacion real.
 */
@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    /** Rutina en curso. Referencia debil: la rutina puede no existir todavia. */
    val currentRoutineId: String? = null,
    /** Puntero al `orderIndex` del workout que toca hoy dentro de la rutina. */
    val activeSequenceIndex: Int = 1,
    val createdAt: Instant,
    @Embedded val sync: SyncMetadata = SyncMetadata()
)
