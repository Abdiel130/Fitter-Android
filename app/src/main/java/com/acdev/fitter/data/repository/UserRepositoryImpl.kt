package com.acdev.fitter.data.repository

import android.util.Log
import com.acdev.fitter.core.time.AppClock
import com.acdev.fitter.core.util.IdGenerator
import com.acdev.fitter.data.local.dao.UserDao
import com.acdev.fitter.data.local.entity.SyncMetadata
import com.acdev.fitter.data.local.entity.UserEntity
import com.acdev.fitter.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val TAG = "UserRepository"

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val clock: AppClock,
    private val idGenerator: IdGenerator
) : UserRepository {

    override val currentUserId: Flow<String?> =
        userDao.observeCurrentUser().map { it?.id }

    override val currentUserName: Flow<String> =
        userDao.observeCurrentUser().map { it?.name.orEmpty() }

    override val currentRemoteId: Flow<String?> =
        userDao.observeCurrentUser().map { it?.remoteId }

    /**
     * Si ya hay usuario local se reutiliza y, si viene con cuenta del servidor, se enlaza. Un
     * usuario nuevo nace con el id del servidor como clave primaria, de modo que a partir de ahora
     * local y remoto coinciden y la sincronizacion no necesita traducir identificadores.
     */
    override suspend fun ensureLocalUser(name: String, email: String, remoteId: String?): String {
        val existing = userDao.getCurrentUser()
        if (existing != null) {
            if (remoteId != null) linkRemoteId(existing.id, remoteId)
            return existing.id
        }

        val now = clock.now()
        val user = UserEntity(
            id = remoteId ?: idGenerator.newId(),
            name = name,
            email = email,
            remoteId = remoteId,
            createdAt = now,
            sync = SyncMetadata(updatedAt = now)
        )
        userDao.upsert(user)
        return user.id
    }

    override suspend fun linkRemoteId(localUserId: String, remoteId: String) {
        val existing = userDao.getCurrentUser() ?: return
        if (existing.remoteId == remoteId) return

        if (existing.remoteId != null) {
            // Este dispositivo tenia datos de otra cuenta. No se borra nada: la fase de
            // sincronizacion decidira que hacer con las filas huerfanas.
            Log.w(TAG, "El usuario local ya estaba enlazado a otra cuenta del servidor")
        }

        userDao.updateRemoteId(localUserId, remoteId, clock.now())
    }
}
