package com.acdev.fitter.domain.repository

import com.acdev.fitter.domain.model.ActiveRoutineLock
import com.acdev.fitter.domain.model.AppPreferences
import com.acdev.fitter.domain.model.ExerciseDetail
import com.acdev.fitter.domain.model.ExerciseDraft
import com.acdev.fitter.domain.model.ExerciseFacets
import com.acdev.fitter.domain.model.ExerciseSummary
import com.acdev.fitter.domain.model.FitterPalette
import com.acdev.fitter.domain.model.Hydration
import com.acdev.fitter.domain.model.NextSession
import com.acdev.fitter.domain.model.NutritionSummary
import com.acdev.fitter.domain.model.PlannedSet
import com.acdev.fitter.domain.model.RoutineDetail
import com.acdev.fitter.domain.model.RoutineSummary
import com.acdev.fitter.domain.model.SleepSummary
import com.acdev.fitter.domain.model.SupplementProgress
import com.acdev.fitter.domain.model.SyncStatus
import com.acdev.fitter.domain.model.ThemeMode
import com.acdev.fitter.domain.model.WeeklyVolume
import com.acdev.fitter.domain.model.WeightUnit
import com.acdev.fitter.domain.model.WorkoutDetail
import com.acdev.fitter.domain.model.WorkoutSummary
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

/**
 * Rutinas: agrupan listas de ejercicios en una secuencia ciclica y deciden cual esta activa.
 *
 * Se separa de `WorkoutRepository` porque son dos responsabilidades distintas: aqui se ordena y
 * se elige; alli se construye el contenido de cada dia.
 */
interface RoutineRepository {
    fun observeRoutines(userId: String): Flow<List<RoutineSummary>>

    fun observeRoutine(routineId: String): Flow<RoutineDetail?>

    /** Estado del bloqueo semanal de la rutina activa. */
    fun observeActiveRoutineLock(userId: String): Flow<ActiveRoutineLock>

    suspend fun createRoutine(userId: String, title: String): String

    suspend fun updateRoutine(routineId: String, title: String, description: String?)

    suspend fun deleteRoutine(routineId: String)

    /** Copia la secuencia bajo un titulo nuevo. Las listas se comparten, no se duplican. */
    suspend fun duplicateRoutine(userId: String, routineId: String, title: String): String

    /**
     * Activa una rutina. No hace nada si el bloqueo semanal esta vigente: la regla vive aqui y no
     * en la pantalla, para que ninguna otra via de entrada pueda saltarsela.
     */
    suspend fun activateRoutine(userId: String, routineId: String)

    /** Anade listas al final de la secuencia, respetando el orden recibido. */
    suspend fun addWorkoutsToRoutine(routineId: String, workoutIds: List<String>)

    suspend fun removeRoutineEntry(routineId: String, entryId: String)

    /** Mueve una entrada `offset` posiciones. Fuera de rango no hace nada. */
    suspend fun moveRoutineEntry(routineId: String, entryId: String, offset: Int)
}

/**
 * Listas de ejercicios: el contenido de un dia de entreno y sus series planificadas.
 *
 * Una lista existe por si misma, aunque no pertenezca a ninguna rutina.
 */
interface WorkoutRepository {
    fun observeWorkouts(userId: String): Flow<List<WorkoutSummary>>

    fun observeWorkout(workoutId: String): Flow<WorkoutDetail?>

    suspend fun createWorkout(userId: String, name: String): String

    suspend fun updateWorkout(workoutId: String, name: String, notes: String?)

    suspend fun deleteWorkout(workoutId: String)

    /** Copia la lista entera con sus ejercicios y series, bajo un nombre nuevo. */
    suspend fun duplicateWorkout(userId: String, workoutId: String, name: String): String

    /** Anade ejercicios al final, cada uno con series por defecto para que nazcan utilizables. */
    suspend fun addExercises(workoutId: String, exerciseIds: List<String>)

    suspend fun removeExercise(workoutId: String, workoutExerciseId: String)

    suspend fun moveExercise(workoutId: String, workoutExerciseId: String, offset: Int)

    suspend fun updateExerciseRest(workoutExerciseId: String, restSeconds: Int)

    suspend fun updateExerciseNotes(workoutExerciseId: String, notes: String?)

    /** Anade una serie clonando la ultima, o una por defecto si aun no hay ninguna. */
    suspend fun addSet(workoutExerciseId: String)

    suspend fun updateSet(workoutExerciseId: String, set: PlannedSet)

    /** Aplica la configuracion de una serie a todas las del mismo ejercicio. */
    suspend fun applySetToAll(workoutExerciseId: String, template: PlannedSet)

    suspend fun removeSet(workoutExerciseId: String, setId: String)
}

/** Catalogo de ejercicios: los precargados y los que crea el usuario. */
interface ExerciseRepository {
    fun observeCatalog(userId: String): Flow<List<ExerciseSummary>>

    /** Vocabulario para clasificar y para filtrar: zonas, musculos y material. */
    fun observeFacets(): Flow<ExerciseFacets>

    suspend fun getDetail(exerciseId: String): ExerciseDetail?

    /** Crea o actualiza un ejercicio propio y devuelve su id. */
    suspend fun saveCustomExercise(userId: String, draft: ExerciseDraft): String

    suspend fun deleteCustomExercise(exerciseId: String)

    /** En cuantas listas se usa. Se consulta antes de borrar para avisar al usuario. */
    suspend fun countUsages(exerciseId: String): Int
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
