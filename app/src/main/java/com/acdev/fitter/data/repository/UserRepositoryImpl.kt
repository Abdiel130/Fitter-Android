package com.acdev.fitter.data.repository

import com.acdev.fitter.core.time.AppClock
import com.acdev.fitter.core.util.IdGenerator
import com.acdev.fitter.data.local.dao.UserDao
import com.acdev.fitter.data.local.entity.SyncMetadata
import com.acdev.fitter.data.local.entity.UserEntity
import com.acdev.fitter.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val clock: AppClock,
    private val idGenerator: IdGenerator
) : UserRepository {

    override val currentUserId: Flow<String?> =
        userDao.observeCurrentUser().map { it?.id }

    override val currentUserName: Flow<String> =
        userDao.observeCurrentUser().map { it?.name.orEmpty() }

    override suspend fun ensureLocalUser(name: String, email: String): String {
        val existing = userDao.getCurrentUser()
        if (existing != null) return existing.id

        val now = clock.now()
        val user = UserEntity(
            id = idGenerator.newId(),
            name = name,
            email = email,
            createdAt = now,
            sync = SyncMetadata(updatedAt = now)
        )
        userDao.upsert(user)
        return user.id
    }
}
