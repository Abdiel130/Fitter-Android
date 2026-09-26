package com.acdev.fitter.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.acdev.fitter.data.local.entity.UserEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    /**
     * Usuario local. Mientras no exista multiusuario en el dispositivo, la app trabaja con el
     * primero creado por el asistente de arranque.
     */
    @Query("SELECT * FROM users WHERE deletedAt IS NULL ORDER BY createdAt LIMIT 1")
    fun observeCurrentUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE deletedAt IS NULL ORDER BY createdAt LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Upsert
    suspend fun upsert(user: UserEntity)

    @Query("UPDATE users SET currentRoutineId = :routineId, activeSequenceIndex = :sequenceIndex WHERE id = :userId")
    suspend fun updateActiveRoutine(userId: String, routineId: String?, sequenceIndex: Int)

    @Query("UPDATE users SET remoteId = :remoteId, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun updateRemoteId(userId: String, remoteId: String, updatedAt: Instant)
}
