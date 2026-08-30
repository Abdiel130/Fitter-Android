package com.acdev.fitter.domain.model

/** Proxima sesion que toca segun el puntero de secuencia de la rutina activa. */
data class NextSession(
    val workoutId: String,
    val name: String,
    val routineTitle: String,
    val exerciseCount: Int,
    val setCount: Int,
    val estimatedMinutes: Int
)

/** Hidratacion del dia. */
data class Hydration(
    val intakeMl: Int,
    val targetMl: Int
) {
    val progress: Float
        get() = if (targetMl <= 0) 0f else (intakeMl.toFloat() / targetMl).coerceIn(0f, 1f)
}

/** Descanso registrado al despertar. */
data class SleepSummary(
    val hours: Double?,
    val quality: Int?
)

/** Suplementos tomados frente a los activos. */
data class SupplementProgress(
    val taken: Int,
    val total: Int
)

/** Consumo del dia frente al objetivo por defecto. */
data class NutritionSummary(
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val caloriesTarget: Double?,
    val proteinTarget: Double?
) {
    val caloriesProgress: Float
        get() = ratio(calories, caloriesTarget)

    val proteinProgress: Float
        get() = ratio(protein, proteinTarget)

    private fun ratio(value: Double, target: Double?): Float =
        if (target == null || target <= 0.0) 0f else (value / target).toFloat().coerceIn(0f, 1f)
}

/** Volumen de entrenamiento de la semana en curso. */
data class WeeklyVolume(
    val effectiveSets: Int,
    val targetSets: Int
) {
    val progress: Float
        get() = if (targetSets <= 0) 0f else (effectiveSets.toFloat() / targetSets).coerceIn(0f, 1f)
}

/**
 * Todo lo que el dashboard necesita, ya resuelto.
 *
 * La pantalla no combina fuentes: recibe esto y lo pinta.
 */
data class DashboardSnapshot(
    val userName: String = "",
    val nextSession: NextSession? = null,
    val hydration: Hydration = Hydration(0, 3000),
    val sleep: SleepSummary = SleepSummary(null, null),
    val supplements: SupplementProgress = SupplementProgress(0, 0),
    val nutrition: NutritionSummary = NutritionSummary(0.0, 0.0, 0.0, 0.0, null, null),
    val weeklyVolume: WeeklyVolume = WeeklyVolume(0, 0),
    val syncStatus: SyncStatus = SyncStatus()
)
