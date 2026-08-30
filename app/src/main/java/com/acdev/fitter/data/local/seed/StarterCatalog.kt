package com.acdev.fitter.data.local.seed

/** Definicion declarativa de un ejercicio del catalogo de arranque. */
data class SeedExercise(
    val key: String,
    val name: String,
    val bodyPart: String,
    val targetMuscle: String,
    val equipment: String
)

/** Ejercicio dentro de un dia de la rutina de arranque. */
data class SeedWorkoutExercise(
    val exerciseKey: String,
    val sets: Int,
    val repIni: Int,
    val repEnd: Int,
    val restSeconds: Int,
    val targetRpe: Double
)

/** Dia de la rutina de arranque. */
data class SeedWorkout(
    val name: String,
    val orderIndex: Int,
    val exercises: List<SeedWorkoutExercise>
)

/**
 * Contenido con el que arranca la app: catalogo minimo de ejercicios y una rutina de ejemplo.
 *
 * No son datos falsos de progreso: son plantillas editables, igual que las que trae cualquier
 * app de entrenamiento. El usuario puede renombrarlas o borrarlas.
 */
object StarterCatalog {

    const val ROUTINE_TITLE = "PPL 4 dias - enfoque dorsal"

    val exercises: List<SeedExercise> = listOf(
        SeedExercise("bench_press", "Press banca con barra", "chest", "pectoralis major", "barbell"),
        SeedExercise("incline_db_press", "Press inclinado con mancuernas", "chest", "pectoralis major", "dumbbell"),
        SeedExercise("machine_fly", "Aperturas en maquina", "chest", "pectoralis major", "machine"),
        SeedExercise("overhead_press", "Press militar con barra", "shoulders", "deltoid anterior", "barbell"),
        SeedExercise("lateral_raise", "Elevaciones laterales", "shoulders", "deltoid lateral", "dumbbell"),
        SeedExercise("triceps_pushdown", "Extension de triceps en polea", "upper arms", "triceps brachii", "cable"),
        SeedExercise("pull_up", "Dominadas", "back", "latissimus dorsi", "body weight"),
        SeedExercise("barbell_row", "Remo con barra", "back", "latissimus dorsi", "barbell"),
        SeedExercise("lat_pulldown", "Jalon al pecho", "back", "latissimus dorsi", "cable"),
        SeedExercise("seated_row", "Remo sentado en polea", "back", "rhomboids", "cable"),
        SeedExercise("face_pull", "Face pull", "shoulders", "deltoid posterior", "cable"),
        SeedExercise("barbell_curl", "Curl con barra", "upper arms", "biceps brachii", "barbell"),
        SeedExercise("back_squat", "Sentadilla trasera", "upper legs", "quadriceps", "barbell"),
        SeedExercise("romanian_deadlift", "Peso muerto rumano", "upper legs", "hamstrings", "barbell"),
        SeedExercise("leg_press", "Prensa de piernas", "upper legs", "quadriceps", "machine"),
        SeedExercise("leg_curl", "Curl femoral tumbado", "upper legs", "hamstrings", "machine"),
        SeedExercise("standing_calf", "Elevacion de gemelos de pie", "lower legs", "calves", "machine")
    )

    val workouts: List<SeedWorkout> = listOf(
        SeedWorkout(
            name = "Empuje A",
            orderIndex = 1,
            exercises = listOf(
                SeedWorkoutExercise("bench_press", 4, 5, 8, 150, 8.0),
                SeedWorkoutExercise("overhead_press", 3, 6, 10, 120, 8.0),
                SeedWorkoutExercise("incline_db_press", 3, 8, 12, 90, 8.5),
                SeedWorkoutExercise("lateral_raise", 3, 12, 15, 60, 9.0),
                SeedWorkoutExercise("triceps_pushdown", 3, 10, 14, 60, 9.0)
            )
        ),
        SeedWorkout(
            name = "Jale A - Dorsal",
            orderIndex = 2,
            exercises = listOf(
                SeedWorkoutExercise("pull_up", 4, 5, 9, 150, 8.5),
                SeedWorkoutExercise("barbell_row", 4, 6, 10, 120, 8.0),
                SeedWorkoutExercise("lat_pulldown", 3, 10, 12, 90, 8.5),
                SeedWorkoutExercise("seated_row", 3, 10, 12, 90, 8.5),
                SeedWorkoutExercise("face_pull", 3, 14, 18, 60, 9.0),
                SeedWorkoutExercise("barbell_curl", 3, 8, 12, 60, 9.0)
            )
        ),
        SeedWorkout(
            name = "Pierna",
            orderIndex = 3,
            exercises = listOf(
                SeedWorkoutExercise("back_squat", 4, 5, 8, 180, 8.0),
                SeedWorkoutExercise("romanian_deadlift", 3, 8, 10, 150, 8.0),
                SeedWorkoutExercise("leg_press", 3, 10, 14, 120, 8.5),
                SeedWorkoutExercise("leg_curl", 3, 10, 14, 90, 9.0),
                SeedWorkoutExercise("standing_calf", 4, 12, 16, 60, 9.0)
            )
        ),
        SeedWorkout(
            name = "Empuje B",
            orderIndex = 4,
            exercises = listOf(
                SeedWorkoutExercise("incline_db_press", 4, 6, 10, 150, 8.0),
                SeedWorkoutExercise("machine_fly", 3, 12, 15, 75, 9.0),
                SeedWorkoutExercise("overhead_press", 3, 8, 12, 120, 8.5),
                SeedWorkoutExercise("lateral_raise", 4, 12, 18, 60, 9.0),
                SeedWorkoutExercise("triceps_pushdown", 3, 12, 15, 60, 9.0)
            )
        )
    )

    val supplements: List<Pair<String, String>> = listOf(
        "Creatina monohidratada" to "5 g",
        "Proteina de suero" to "1 scoop",
        "Omega 3" to "2 capsulas",
        "Vitamina D3" to "1 capsula"
    )
}
