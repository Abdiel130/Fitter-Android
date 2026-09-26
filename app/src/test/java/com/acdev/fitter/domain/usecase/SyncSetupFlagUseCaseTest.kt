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
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val REMOTE_ID = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"

class SyncSetupFlagUseCaseTest {

    @Test
    fun `local completado y servidor pendiente reenvia el patch una sola vez`() = runTest {
        val auth = auth()
        val session = FakeSessionRepository(SessionState.SignedIn(REMOTE_ID, setupCompleted = false))
        val preferences = FakePreferencesRepository(AppPreferences(onboardingCompleted = true))
        val useCase = SyncSetupFlagUseCase(auth, session, preferences)

        useCase.reconcile(remoteCompleted = false)

        assertEquals(listOf("pushSetupCompleted(true)"), auth.calls)
        assertEquals(SessionState.SignedIn(REMOTE_ID, setupCompleted = true), session.current())
    }

    @Test
    fun `servidor completado y local pendiente no manda nada`() = runTest {
        val auth = auth()
        val session = FakeSessionRepository(SessionState.SignedIn(REMOTE_ID, setupCompleted = false))
        val preferences = FakePreferencesRepository(AppPreferences(onboardingCompleted = false))
        val useCase = SyncSetupFlagUseCase(auth, session, preferences)

        useCase.reconcile(remoteCompleted = true)

        assertTrue(auth.calls.isEmpty())
        assertEquals(SessionState.SignedIn(REMOTE_ID, setupCompleted = true), session.current())
    }

    @Test
    fun `los dos de acuerdo no producen ninguna llamada`() = runTest {
        val auth = auth()
        val session = FakeSessionRepository(SessionState.SignedIn(REMOTE_ID, setupCompleted = true))
        val preferences = FakePreferencesRepository(AppPreferences(onboardingCompleted = true))
        val useCase = SyncSetupFlagUseCase(auth, session, preferences)

        useCase.reconcile(remoteCompleted = true)

        assertTrue(auth.calls.isEmpty())
    }

    /** Sin red el espejo local ya esta escrito, asi que el usuario entra y el patch se reintenta. */
    @Test
    fun `si el patch se queda sin red el espejo local sigue escrito`() = runTest {
        val auth = auth().apply { failSetupWith(RemoteResult.Failure(RemoteError.Offline)) }
        val session = FakeSessionRepository(SessionState.SignedIn(REMOTE_ID, setupCompleted = false))
        val preferences = FakePreferencesRepository()
        val useCase = SyncSetupFlagUseCase(auth, session, preferences)

        val result = useCase(completed = true)

        assertTrue(result is RemoteResult.Failure)
        assertEquals(SessionState.SignedIn(REMOTE_ID, setupCompleted = true), session.current())
    }

    @Test
    fun `el espejo local se escribe antes de hablar con el servidor`() = runTest {
        val auth = auth()
        val session = FakeSessionRepository(SessionState.SignedIn(REMOTE_ID, setupCompleted = false))
        val useCase = SyncSetupFlagUseCase(auth, session, FakePreferencesRepository())

        useCase(completed = true)

        assertEquals(listOf("setSetupCompleted(true)"), session.calls)
    }

    private fun auth() = FakeAuthRepository(RemoteResult.Success(account()))

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
