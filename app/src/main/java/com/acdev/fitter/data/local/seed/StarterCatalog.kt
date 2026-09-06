package com.acdev.fitter.data.local.seed

import com.acdev.fitter.domain.model.ExerciseIcon

/** Definicion declarativa de un ejercicio del catalogo de arranque. */
data class SeedExercise(
    val key: String,
    val name: String,
    val bodyPart: String,
    val targetMuscle: String,
    val equipment: String,
    val icon: ExerciseIcon
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
        SeedExercise(
            key = "bench_press",
            name = "Press banca con barra",
            bodyPart = "chest",
            targetMuscle = "pectoralis major",
            equipment = "barbell",
            icon = ExerciseIcon.BARBELL
        ),
        SeedExercise(
            key = "incline_db_press",
            name = "Press inclinado con mancuernas",
            bodyPart = "chest",
            targetMuscle = "pectoralis major",
            equipment = "dumbbell",
            icon = ExerciseIcon.DUMBBELL
        ),
        SeedExercise(
            key = "machine_fly",
            name = "Aperturas en maquina",
            bodyPart = "chest",
            targetMuscle = "pectoralis major",
            equipment = "machine",
            icon = ExerciseIcon.MACHINE
        ),
        SeedExercise(
            key = "overhead_press",
            name = "Press militar con barra",
            bodyPart = "shoulders",
            targetMuscle = "deltoid anterior",
            equipment = "barbell",
            icon = ExerciseIcon.BARBELL
        ),
        SeedExercise(
            key = "lateral_raise",
            name = "Elevaciones laterales",
            bodyPart = "shoulders",
            targetMuscle = "deltoid lateral",
            equipment = "dumbbell",
            icon = ExerciseIcon.DUMBBELL
        ),
        SeedExercise(
            key = "triceps_pushdown",
            name = "Extension de triceps en polea",
            bodyPart = "upper arms",
            targetMuscle = "triceps brachii",
            equipment = "cable",
            icon = ExerciseIcon.CABLE
        ),
        SeedExercise(
            key = "pull_up",
            name = "Dominadas",
            bodyPart = "back",
            targetMuscle = "latissimus dorsi",
            equipment = "body weight",
            icon = ExerciseIcon.BODYWEIGHT
        ),
        SeedExercise(
            key = "barbell_row",
            name = "Remo con barra",
            bodyPart = "back",
            targetMuscle = "latissimus dorsi",
            equipment = "barbell",
            icon = ExerciseIcon.BARBELL
        ),
        SeedExercise(
            key = "lat_pulldown",
            name = "Jalon al pecho",
            bodyPart = "back",
            targetMuscle = "latissimus dorsi",
            equipment = "cable",
            icon = ExerciseIcon.CABLE
        ),
        SeedExercise(
            key = "seated_row",
            name = "Remo sentado en polea",
            bodyPart = "back",
            targetMuscle = "rhomboids",
            equipment = "cable",
            icon = ExerciseIcon.CABLE
        ),
        SeedExercise(
            key = "face_pull",
            name = "Face pull",
            bodyPart = "shoulders",
            targetMuscle = "deltoid posterior",
            equipment = "cable",
            icon = ExerciseIcon.CABLE
        ),
        SeedExercise(
            key = "barbell_curl",
            name = "Curl con barra",
            bodyPart = "upper arms",
            targetMuscle = "biceps brachii",
            equipment = "barbell",
            icon = ExerciseIcon.BARBELL
        ),
        SeedExercise(
            key = "back_squat",
            name = "Sentadilla trasera",
            bodyPart = "upper legs",
            targetMuscle = "quadriceps",
            equipment = "barbell",
            icon = ExerciseIcon.LEGS
        ),
        SeedExercise(
            key = "romanian_deadlift",
            name = "Peso muerto rumano",
            bodyPart = "upper legs",
            targetMuscle = "hamstrings",
            equipment = "barbell",
            icon = ExerciseIcon.BARBELL
        ),
        SeedExercise(
            key = "leg_press",
            name = "Prensa de piernas",
            bodyPart = "upper legs",
            targetMuscle = "quadriceps",
            equipment = "machine",
            icon = ExerciseIcon.LEGS
        ),
        SeedExercise(
            key = "leg_curl",
            name = "Curl femoral tumbado",
            bodyPart = "upper legs",
            targetMuscle = "hamstrings",
            equipment = "machine",
            icon = ExerciseIcon.LEGS
        ),
        SeedExercise(
            key = "standing_calf",
            name = "Elevacion de gemelos de pie",
            bodyPart = "lower legs",
            targetMuscle = "calves",
            equipment = "machine",
            icon = ExerciseIcon.MACHINE
        )
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
