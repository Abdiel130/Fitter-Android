package com.acdev.fitter.domain.usecase

import com.acdev.fitter.domain.model.AppPreferences
import com.acdev.fitter.domain.model.RemoteError
import com.acdev.fitter.domain.model.RemoteResult
import com.acdev.fitter.domain.model.RemoteUser
import com.acdev.fitter.domain.model.SessionState
import com.acdev.fitter.domain.model.SessionTokens
import com.acdev.fitter.domain.model.SignedInAccount
import com.acdev.fitter.domain.model.shouldClearSession
import com.acdev.fitter.fake.FakeAuthRepository
import com.acdev.fitter.fake.FakeBodyRepository
import com.acdev.fitter.fake.FakeNutritionRepository
import com.acdev.fitter.fake.FakePreferencesRepository
import com.acdev.fitter.fake.FakeRecoveryRepository
import com.acdev.fitter.fake.FakeSessionRepository
import com.acdev.fitter.fake.FakeTrainingRepository
import com.acdev.fitter.fake.FakeUserRepository
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private const val REMOTE_ID = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"

class SignOutUseCaseTest {

    /**
     * La regla de oro del modo sin conexion: salir cierra la sesion y no toca ni una fila de la base
     * local ni las preferencias del dispositivo.
     */
    @Test
    fun `cerrar sesion no toca los datos locales`() = runTest {
        val session = FakeSessionRepository(SessionState.SignedIn(REMOTE_ID, setupCompleted = true))
        val auth = FakeAuthRepository(RemoteResult.Success(account()))
        val preferences = FakePreferencesRepository(AppPreferences(onboardingCompleted = true))
        val users = FakeUserRepository(localId = "local")
        val training = FakeTrainingRepository()
        val nutrition = FakeNutritionRepository()
        val recovery = FakeRecoveryRepository()
        val body = FakeBodyRepository()

        SignOutUseCase(auth, session)()

        assertEquals(SessionState.SignedOut, session.current())
        assertTrue(preferences.calls.isEmpty())
        assertTrue(preferences.current().onboardingCompleted)
        assertTrue(users.calls.isEmpty())
        assertTrue(training.calls.isEmpty())
        assertTrue(nutrition.calls.isEmpty())
        assertTrue(recovery.calls.isEmpty())
        assertTrue(body.calls.isEmpty())
    }

    @Test
    fun `se avisa al servidor solo de este dispositivo`() = runTest {
        val auth = FakeAuthRepository(RemoteResult.Success(account()))

        SignOutUseCase(auth, FakeSessionRepository())()

        assertEquals(listOf("signOut(false)"), auth.calls)
    }

    /** Solo un 401 explicito del servidor invalida la sesion; sin red no se echa a nadie. */
    @Test
    fun `unicamente un token rechazado cierra la sesion`() {
        assertTrue(shouldClearSession(RemoteError.Unauthorized(null)))
        assertFalse(shouldClearSession(RemoteError.Offline))
        assertFalse(shouldClearSession(RemoteError.ServerUnreachable))
        assertFalse(shouldClearSession(RemoteError.Timeout))
        assertFalse(shouldClearSession(RemoteError.Http(403, null)))
        assertFalse(shouldClearSession(RemoteError.Http(500, null)))
        assertFalse(shouldClearSession(RemoteError.Unexpected("cuerpo raro")))
    }

    private fun account() = SignedInAccount(
        user = RemoteUser(
            id = REMOTE_ID,
            name = "Juan Perez",
            email = "atleta@fitter.app",
            currentRoutineId = null,
            activeSequenceIndex = 1,
            setupCompleted = true
        ),
        tokens = SessionTokens(
            accessToken = "acceso",
            accessExpiresAt = Instant.parse("2026-10-26T12:00:00Z"),
            refreshToken = "renovacion",
            refreshExpiresAt = Instant.parse("2027-03-26T12:00:00Z")
        )
    )
}
