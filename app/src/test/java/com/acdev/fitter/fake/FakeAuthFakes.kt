package com.acdev.fitter.fake

import com.acdev.fitter.core.time.AppClock
import com.acdev.fitter.core.util.IdGenerator
import com.acdev.fitter.domain.model.AppPreferences
import com.acdev.fitter.domain.model.FitterPalette
import com.acdev.fitter.domain.model.Hydration
import com.acdev.fitter.domain.model.NextSession
import com.acdev.fitter.domain.model.NutritionSummary
import com.acdev.fitter.domain.model.RemoteResult
import com.acdev.fitter.domain.model.RemoteUser
import com.acdev.fitter.domain.model.SessionState
import com.acdev.fitter.domain.model.SessionTokens
import com.acdev.fitter.domain.model.SignedInAccount
import com.acdev.fitter.domain.model.SleepSummary
import com.acdev.fitter.domain.model.SupplementProgress
import com.acdev.fitter.domain.model.ThemeMode
import com.acdev.fitter.domain.model.WeeklyVolume
import com.acdev.fitter.domain.model.WeightUnit
import com.acdev.fitter.domain.repository.AuthRepository
import com.acdev.fitter.domain.repository.BodyRepository
import com.acdev.fitter.domain.repository.NutritionRepository
import com.acdev.fitter.domain.repository.PreferencesRepository
import com.acdev.fitter.domain.repository.RecoveryRepository
import com.acdev.fitter.domain.repository.SessionRepository
import com.acdev.fitter.domain.repository.TrainingRepository
import com.acdev.fitter.domain.repository.UserRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Dobles de prueba compartidos.
 *
 * Cada uno anota lo que recibe en una lista ordenada, porque en varios casos lo que hay que
 * comprobar no es solo que algo se guardo, sino en que orden.
 */
class FixedClock(
    private var instant: Instant = Instant.parse("2026-09-26T12:00:00Z")
) : AppClock {
    override fun now(): Instant = instant
    override fun today(): LocalDate = LocalDate.ofInstant(instant, ZoneOffset.UTC)
    override fun zone(): ZoneId = ZoneOffset.UTC

    fun advanceTo(value: Instant) {
        instant = value
    }
}

class FixedIdGenerator(private val id: String = "id-fijo") : IdGenerator {
    override fun newId(): String = id
}

class FakePreferencesRepository(
    initial: AppPreferences = AppPreferences()
) : PreferencesRepository {

    private val state = MutableStateFlow(initial)
    val calls: MutableList<String> = mutableListOf()

    override val preferences: Flow<AppPreferences> = state.asStateFlow()

    fun current(): AppPreferences = state.value

    override suspend fun setPalette(palette: FitterPalette) {
        calls += "setPalette"
        state.update { it.copy(palette = palette) }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        calls += "setThemeMode"
        state.update { it.copy(themeMode = mode) }
    }

    override suspend fun setWeightUnit(unit: WeightUnit) {
        calls += "setWeightUnit"
        state.update { it.copy(weightUnit = unit) }
    }

    override suspend fun setAccount(displayName: String, email: String) {
        calls += "setAccount"
        state.update { it.copy(displayName = displayName, email = email) }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        calls += "setOnboardingCompleted"
        state.update { it.copy(onboardingCompleted = completed) }
    }
}

class FakeSessionRepository(
    initial: SessionState = SessionState.SignedOut
) : SessionRepository {

    private val internalState = MutableStateFlow(initial)
    val calls: MutableList<String> = mutableListOf()

    /** Simula un dispositivo que no puede proteger los tokens, por ejemplo sin Keystore usable. */
    var canOpen: Boolean = true

    override val state: Flow<SessionState> = internalState.asStateFlow()

    override val remoteUserId: Flow<String?> =
        internalState.map { (it as? SessionState.SignedIn)?.remoteUserId }

    fun current(): SessionState = internalState.value

    override suspend fun open(user: RemoteUser, tokens: SessionTokens): Boolean {
        calls += "open"
        if (!canOpen) return false
        internalState.value = SessionState.SignedIn(user.id, user.setupCompleted)
        return true
    }

    override suspend fun setSetupCompleted(completed: Boolean) {
        calls += "setSetupCompleted($completed)"
        internalState.update { current ->
            when (current) {
                is SessionState.SignedIn -> current.copy(setupCompleted = completed)
                else -> current
            }
        }
    }

    override suspend fun clearSession() {
        calls += "clearSession"
        internalState.value = SessionState.SignedOut
    }
}

class FakeAuthRepository(
    private var signInResult: RemoteResult<SignedInAccount>,
    private var setupResult: RemoteResult<Boolean> = RemoteResult.Success(true)
) : AuthRepository {

    val calls: MutableList<String> = mutableListOf()

    override suspend fun signIn(email: String, password: String): RemoteResult<SignedInAccount> {
        calls += "signIn($email)"
        return signInResult
    }

    override suspend fun signOut(allDevices: Boolean): RemoteResult<Unit> {
        calls += "signOut($allDevices)"
        return RemoteResult.Success(Unit)
    }

    override suspend fun me(): RemoteResult<RemoteUser> {
        calls += "me"
        return when (val result = signInResult) {
            is RemoteResult.Success -> RemoteResult.Success(result.value.user)
            is RemoteResult.Failure -> result
        }
    }

    override suspend fun pushSetupCompleted(completed: Boolean): RemoteResult<Boolean> {
        calls += "pushSetupCompleted($completed)"
        return setupResult
    }

    fun failSetupWith(result: RemoteResult<Boolean>) {
        setupResult = result
    }
}

class FakeUserRepository(private var localId: String? = null) : UserRepository {

    val calls: MutableList<String> = mutableListOf()
    var linkedRemoteId: String? = null
        private set

    private val userId = MutableStateFlow(localId)

    override val currentUserId: Flow<String?> = userId.asStateFlow()
    override val currentUserName: Flow<String> = MutableStateFlow("").asStateFlow()
    override val currentRemoteId: Flow<String?> = MutableStateFlow(linkedRemoteId).asStateFlow()

    override suspend fun ensureLocalUser(name: String, email: String, remoteId: String?): String {
        calls += "ensureLocalUser(name=$name, email=$email, remoteId=$remoteId)"
        val id = localId ?: remoteId ?: "nuevo"
        localId = id
        userId.value = id
        return id
    }

    override suspend fun linkRemoteId(localUserId: String, remoteId: String) {
        calls += "linkRemoteId($localUserId, $remoteId)"
        linkedRemoteId = remoteId
    }
}

/**
 * Repositorios de datos del asistente.
 *
 * Solo anotan lo que se les pide: lo que se comprueba es el orden de las llamadas y que nadie borre
 * nada al cerrar sesion.
 */
class FakeTrainingRepository : TrainingRepository {
    val calls: MutableList<String> = mutableListOf()

    override fun observeNextSession(userId: String): Flow<NextSession?> = throw NotImplementedError()

    override fun observeWeeklyVolume(userId: String, weekStart: LocalDate): Flow<WeeklyVolume> =
        throw NotImplementedError()

    override suspend fun ensureStarterRoutine(userId: String) {
        calls += "ensureStarterRoutine"
    }
}

class FakeNutritionRepository : NutritionRepository {
    val calls: MutableList<String> = mutableListOf()

    override fun observeDailySummary(userId: String, date: LocalDate): Flow<NutritionSummary> =
        throw NotImplementedError()

    override suspend fun ensureDefaultTargets(userId: String) {
        calls += "ensureDefaultTargets"
    }
}

class FakeRecoveryRepository : RecoveryRepository {
    val calls: MutableList<String> = mutableListOf()

    override fun observeHydration(userId: String, date: LocalDate): Flow<Hydration> =
        throw NotImplementedError()

    override fun observeSleep(userId: String, date: LocalDate): Flow<SleepSummary> =
        throw NotImplementedError()

    override fun observeSupplementProgress(
        userId: String,
        date: LocalDate
    ): Flow<SupplementProgress> = throw NotImplementedError()

    override suspend fun addWater(userId: String, date: LocalDate, milliliters: Int) {
        calls += "addWater"
    }

    override suspend fun ensureStarterSupplements(userId: String) {
        calls += "ensureStarterSupplements"
    }
}

class FakeBodyRepository : BodyRepository {
    val calls: MutableList<String> = mutableListOf()
    var savedWeightKg: Double? = null
        private set

    override suspend fun saveMeasurement(
        userId: String,
        date: LocalDate,
        weightKg: Double?,
        waistCm: Double?,
        chestCm: Double?
    ) {
        calls += "saveMeasurement"
        savedWeightKg = weightKg
    }
}
