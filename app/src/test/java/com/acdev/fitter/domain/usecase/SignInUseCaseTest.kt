package com.acdev.fitter.domain.usecase

import com.acdev.fitter.domain.model.AppPreferences
import com.acdev.fitter.domain.model.RemoteError
import com.acdev.fitter.domain.model.RemoteResult
import com.acdev.fitter.domain.model.RemoteUser
import com.acdev.fitter.domain.model.SessionState
import com.acdev.fitter.domain.model.SessionTokens
import com.acdev.fitter.domain.model.SignedInAccount
import com.acdev.fitter.fake.FakeAuthRepository
import com.acdev.fitter.fake.FakePreferencesRepository
import com.acdev.fitter.fake.FakeSessionRepository
import com.acdev.fitter.fake.FakeUserRepository
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private const val REMOTE_ID = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
private const val LOCAL_ID = "local-antiguo"

class SignInUseCaseTest {

    @Test
    fun `al entrar se guarda la identidad del servidor`() = runTest {
        val preferences = FakePreferencesRepository()
        val session = FakeSessionRepository()
        val useCase = useCase(success(), session, preferences, FakeUserRepository())

        val result = useCase("  atleta@fitter.app ", "miPasswordSeguro123")

        assertTrue(result is RemoteResult.Success)
        assertEquals("Juan Perez", preferences.current().displayName)
        assertEquals("atleta@fitter.app", preferences.current().email)
        assertEquals(SessionState.SignedIn(REMOTE_ID, setupCompleted = false), session.current())
    }

    /** Un dispositivo con datos de la version sin acceso queda atado a la cuenta, nunca borrado. */
    @Test
    fun `un usuario local previo se ata a la cuenta del servidor`() = runTest {
        val users = FakeUserRepository(localId = LOCAL_ID)
        val useCase = useCase(success(), FakeSessionRepository(), FakePreferencesRepository(), users)

        useCase("atleta@fitter.app", "miPasswordSeguro123")

        assertEquals(listOf("linkRemoteId($LOCAL_ID, $REMOTE_ID)"), users.calls)
        assertEquals(REMOTE_ID, users.linkedRemoteId)
    }

    @Test
    fun `sin usuario local no se intenta ningun enlace`() = runTest {
        val users = FakeUserRepository()
        val useCase = useCase(success(), FakeSessionRepository(), FakePreferencesRepository(), users)

        useCase("atleta@fitter.app", "miPasswordSeguro123")

        assertTrue(users.calls.isEmpty())
        assertNull(users.linkedRemoteId)
    }

    @Test
    fun `unas credenciales rechazadas no escriben nada`() = runTest {
        val rejected = FakeAuthRepository(
            RemoteResult.Failure(RemoteError.Unauthorized("Credenciales invalidas."))
        )
        val preferences = FakePreferencesRepository()
        val session = FakeSessionRepository()
        val users = FakeUserRepository(localId = LOCAL_ID)

        val result = useCase(rejected, session, preferences, users)("mal@fitter.app", "12345678")

        assertTrue(result is RemoteResult.Failure)
        assertTrue(preferences.calls.isEmpty())
        assertTrue(session.calls.isEmpty())
        assertTrue(users.calls.isEmpty())
    }

    /** Si los tokens no se pueden proteger, la sesion no queda a medias. */
    @Test
    fun `si la sesion no se puede guardar el acceso falla`() = runTest {
        val preferences = FakePreferencesRepository()
        val session = FakeSessionRepository().apply { canOpen = false }

        val result = useCase(success(), session, preferences, FakeUserRepository())(
            "atleta@fitter.app",
            "miPasswordSeguro123"
        )

        assertTrue(result is RemoteResult.Failure)
        assertTrue(preferences.calls.isEmpty())
        assertEquals(SessionState.SignedOut, session.current())
    }

    @Test
    fun `local completado y servidor pendiente reenvia el patch al entrar`() = runTest {
        val auth = success()
        val preferences = FakePreferencesRepository(AppPreferences(onboardingCompleted = true))

        useCase(auth, FakeSessionRepository(), preferences, FakeUserRepository())(
            "atleta@fitter.app",
            "miPasswordSeguro123"
        )

        assertTrue(auth.calls.contains("pushSetupCompleted(true)"))
    }

    private fun useCase(
        auth: FakeAuthRepository,
        session: FakeSessionRepository,
        preferences: FakePreferencesRepository,
        users: FakeUserRepository
    ) = SignInUseCase(
        authRepository = auth,
        sessionRepository = session,
        preferencesRepository = preferences,
        userRepository = users,
        syncSetupFlag = SyncSetupFlagUseCase(auth, session, preferences)
    )

    private fun success() = FakeAuthRepository(RemoteResult.Success(account()))

    private fun account() = SignedInAccount(
        user = RemoteUser(
            id = REMOTE_ID,
            name = "Juan Perez",
            email = "atleta@fitter.app",
            currentRoutineId = null,
            activeSequenceIndex = 1,
            setupCompleted = false
        ),
        tokens = SessionTokens(
            accessToken = "acceso",
            accessExpiresAt = Instant.parse("2026-10-26T12:00:00Z"),
            refreshToken = "renovacion",
            refreshExpiresAt = Instant.parse("2027-03-26T12:00:00Z")
        )
    )
}
