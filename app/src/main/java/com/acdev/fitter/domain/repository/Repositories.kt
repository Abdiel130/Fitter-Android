package com.acdev.fitter.domain.repository

import com.acdev.fitter.domain.model.AppPreferences
import com.acdev.fitter.domain.model.FitterPalette
import com.acdev.fitter.domain.model.Hydration
import com.acdev.fitter.domain.model.NextSession
import com.acdev.fitter.domain.model.NutritionSummary
import com.acdev.fitter.domain.model.SleepSummary
import com.acdev.fitter.domain.model.SupplementProgress
import com.acdev.fitter.domain.model.SyncStatus
import com.acdev.fitter.domain.model.ThemeMode
import com.acdev.fitter.domain.model.WeeklyVolume
import com.acdev.fitter.domain.model.WeightUnit
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Contratos de datos del dominio.
 *
 * La UI y los casos de uso solo conocen estas interfaces; las implementaciones de Room y
 * DataStore viven en `data`. Cuando exista servidor, la implementacion cambia y nada mas.
 */

interface PreferencesRepository {
    val preferences: Flow<AppPreferences>

    suspend fun setPalette(palette: FitterPalette)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setWeightUnit(unit: WeightUnit)
    suspend fun setAccount(displayName: String, email: String)
    suspend fun setOnboardingCompleted(completed: Boolean)
}

interface UserRepository {
    /** Identificador del usuario local, o null si el asistente aun no lo ha creado. */
    val currentUserId: Flow<String?>

    val currentUserName: Flow<String>

    /** Crea el usuario local del dispositivo y devuelve su id. Idempotente. */
    suspend fun ensureLocalUser(name: String, email: String): String
}

interface TrainingRepository {
    fun observeNextSession(userId: String): Flow<NextSession?>

    fun observeWeeklyVolume(userId: String, weekStart: LocalDate): Flow<WeeklyVolume>

    /** Instala la rutina de arranque si el usuario todavia no tiene ninguna. */
    suspend fun ensureStarterRoutine(userId: String)
}

interface NutritionRepository {
    fun observeDailySummary(userId: String, date: LocalDate): Flow<NutritionSummary>

    suspend fun ensureDefaultTargets(userId: String)
}

interface RecoveryRepository {
    fun observeHydration(userId: String, date: LocalDate): Flow<Hydration>

    fun observeSleep(userId: String, date: LocalDate): Flow<SleepSummary>

    fun observeSupplementProgress(userId: String, date: LocalDate): Flow<SupplementProgress>

    /** Suma agua al dia indicado, creando el registro diario si aun no existe. */
    suspend fun addWater(userId: String, date: LocalDate, milliliters: Int)

    /** Instala la lista de suplementos de arranque la primera vez. */
    suspend fun ensureStarterSupplements(userId: String)
}

interface BodyRepository {
    /** Guarda un registro antropometrico. Los valores llegan ya normalizados a kg y cm. */
    suspend fun saveMeasurement(
        userId: String,
        date: LocalDate,
        weightKg: Double?,
        waistCm: Double?,
        chestCm: Double?
    )
}

interface SyncRepository {
    val status: Flow<SyncStatus>
}
