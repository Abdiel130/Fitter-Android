package com.acdev.fitter.domain.usecase

import com.acdev.fitter.domain.model.AppPreferences
import com.acdev.fitter.domain.model.RemoteResult
import com.acdev.fitter.domain.model.RemoteUser
import com.acdev.fitter.domain.model.SessionState
import com.acdev.fitter.domain.model.SessionTokens
import com.acdev.fitter.domain.model.SignedInAccount
import com.acdev.fitter.domain.model.WeightUnit
import com.acdev.fitter.fake.FakeAuthRepository
import com.acdev.fitter.fake.FakeBodyRepository
import com.acdev.fitter.fake.FakeNutritionRepository
import com.acdev.fitter.fake.FakePreferencesRepository
import com.acdev.fitter.fake.FakeRecoveryRepository
import com.acdev.fitter.fake.FakeSessionRepository
import com.acdev.fitter.fake.FakeTrainingRepository
import com.acdev.fitter.fake.FakeUserRepository
import com.acdev.fitter.fake.FixedClock
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private const val REMOTE_ID = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
private const val POUNDS_180 = 180.0
private const val KG_TOLERANCE = 0.01

class CompleteOnboardingUseCaseTest {

    @Test
    fun `marcar el asistente como visto es la ultima escritura`() = runTest {
        val preferences = FakePreferencesRepository(
            AppPreferences(displayName = "Juan Perez", email = "atleta@fitter.app")
        )
        val world = world(preferences)

        world.useCase(result())

        assertEquals("setOnboardingCompleted", preferences.calls.last())
    }

    @Test
    fun `el id de la cuenta llega al usuario local`() = runTest {
        val world = world()

        world.useCase(result())

        assertTrue(
            world.users.calls.any { call -> call.contains("remoteId=$REMOTE_ID") }
        )
    }

    @Test
    fun `se instalan rutina objetivos y suplementos de partida`() = runTest {
        val world = world()

        world.useCase(result())

        assertEquals(listOf("ensureStarterRoutine"), world.training.calls)
        assertEquals(listOf("ensureDefaultTargets"), world.nutrition.calls)
        assertEquals(listOf("ensureStarterSupplements"), world.recovery.calls)
    }

    /** La base nunca guarda libras: la conversion ocurre en el borde. */
    @Test
    fun `un peso en libras se guarda en kilogramos`() = runTest {
        val world = world()

        world.useCase(result(unit = WeightUnit.LBS, weight = POUNDS_180))

        val expected = WeightUnit.LBS.toKg(POUNDS_180)
        assertEquals(expected, world.body.savedWeightKg ?: 0.0, KG_TOLERANCE)
    }

    @Test
    fun `sin medidas no se guarda ningun registro`() = runTest {
        val world = world()

        world.useCase(result(weight = null))

        assertTrue(world.body.calls.isEmpty())
        assertNull(world.body.savedWeightKg)
    }

    /** Sin sesion no hay cuenta a la que atribuir nada, y el asistente no deberia estar visible. */
    @Test
    fun `sin sesion no se escribe nada`() = runTest {
        val preferences = FakePreferencesRepository()
        val world = world(preferences, session = FakeSessionRepository(SessionState.SignedOut))

        world.useCase(result())

        assertTrue(preferences.calls.isEmpty())
        assertTrue(world.training.calls.isEmpty())
        assertTrue(world.users.calls.isEmpty())
    }

    private class World(
        val useCase: CompleteOnboardingUseCase,
        val users: FakeUserRepository,
        val training: FakeTrainingRepository,
        val nutrition: FakeNutritionRepository,
        val recovery: FakeRecoveryRepository,
        val body: FakeBodyRepository
    )

    private fun world(
        preferences: FakePreferencesRepository = FakePreferencesRepository(),
        session: FakeSessionRepository = FakeSessionRepository(
            SessionState.SignedIn(REMOTE_ID, setupCompleted = false)
        )
    ): World {
        val users = FakeUserRepository()
        val training = FakeTrainingRepository()
        val nutrition = FakeNutritionRepository()
        val recovery = FakeRecoveryRepository()
        val body = FakeBodyRepository()
        val auth = FakeAuthRepository(RemoteResult.Success(account()))

        return World(
            useCase = CompleteOnboardingUseCase(
                userRepository = users,
                trainingRepository = training,
                nutritionRepository = nutrition,
                recoveryRepository = recovery,
                bodyRepository = body,
                preferencesRepository = preferences,
                sessionRepository = session,
                syncSetupFlag = SyncSetupFlagUseCase(auth, session, preferences),
                clock = FixedClock()
            ),
            users = users,
            training = training,
            nutrition = nutrition,
            recovery = recovery,
            body = body
        )
    }

    private fun result(unit: WeightUnit = WeightUnit.KG, weight: Double? = 82.0) = OnboardingResult(
        weightUnit = unit,
        bodyWeight = weight,
        waistCm = null,
        chestCm = null
    )

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
