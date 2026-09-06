package com.acdev.fitter.domain.model

import java.time.LocalDate

/**
 * Icono de un ejercicio.
 *
 * Es un catalogo cerrado a proposito: el usuario elige entre estos y no puede introducir un
 * emoji ni una imagen suelta, de modo que la parrilla de ejercicios mantiene un unico trazo.
 * Se guarda por nombre en `exercises.iconKey`.
 */
enum class ExerciseIcon {
    DUMBBELL,
    BARBELL,
    MACHINE,
    CABLE,
    KETTLEBELL,
    BODYWEIGHT,
    LEGS,
    CORE,
    CARDIO,
    STRETCH;

    companion object {
        val DEFAULT: ExerciseIcon = DUMBBELL

        /** Tolerante con claves desconocidas: un icono retirado no rompe el catalogo. */
        fun fromKey(key: String?): ExerciseIcon =
            entries.firstOrNull { it.name == key } ?: DEFAULT
    }
}

/** Rutina en la lista, con sus recuentos ya calculados. */
data class RoutineSummary(
    val id: String,
    val title: String,
    val description: String?,
    val isActive: Boolean,
    val workoutCount: Int,
    val exerciseCount: Int,
    val setCount: Int
)

/** Lista dentro de una rutina. `entryId` identifica la posicion, no la lista. */
data class RoutineWorkoutItem(
    val entryId: String,
    val workoutId: String,
    val name: String,
    val orderIndex: Int,
    val exerciseCount: Int,
    val setCount: Int
)

/** Rutina abierta en el editor. */
data class RoutineDetail(
    val id: String,
    val title: String,
    val description: String?,
    val isActive: Boolean,
    val workouts: List<RoutineWorkoutItem>
)

/**
 * Bloqueo del cambio de rutina.
 *
 * La regla del producto: si la semana ya se empezo con la rutina activa, la secuencia se respeta
 * hasta el lunes siguiente. Crear y editar listas sigue permitido; solo se bloquea activar otra.
 */
data class ActiveRoutineLock(
    val isLocked: Boolean = false,
    val sessionsThisWeek: Int = 0,
    val unlocksOn: LocalDate? = null
)

/** Lista de ejercicios en la pestana de listas. */
data class WorkoutSummary(
    val id: String,
    val name: String,
    val exerciseCount: Int,
    val setCount: Int,
    val routineCount: Int,
    val previewExercises: List<String>
)

/** Serie planificada de un ejercicio dentro de una lista. */
data class PlannedSet(
    val id: String,
    val setOrder: Int,
    val type: SetType,
    val repIni: Int,
    val repEnd: Int,
    val targetRpe: Double?,
    val restSeconds: Int?
)

/** Ejercicio dentro de una lista, con sus series planificadas. */
data class WorkoutExerciseItem(
    val id: String,
    val exerciseId: String,
    val name: String,
    val icon: ExerciseIcon,
    val targetMuscle: String?,
    val orderInRoutine: Int,
    val restSeconds: Int,
    val notes: String?,
    val sets: List<PlannedSet>
)

/** Lista abierta en el editor. */
data class WorkoutDetail(
    val id: String,
    val name: String,
    val notes: String?,
    val exercises: List<WorkoutExerciseItem>
)

/** Ejercicio del catalogo tal y como se lista y se busca. */
data class ExerciseSummary(
    val id: String,
    val name: String,
    val icon: ExerciseIcon,
    val bodyParts: List<String>,
    val targetMuscles: List<String>,
    val equipments: List<String>,
    val isCustom: Boolean
)

/** Ejercicio abierto en su editor, con todo lo que el usuario puede tocar. */
data class ExerciseDetail(
    val id: String,
    val name: String,
    val icon: ExerciseIcon,
    val instructions: List<String>,
    val bodyParts: List<String>,
    val targetMuscles: List<String>,
    val secondaryMuscles: List<String>,
    val equipments: List<String>,
    val isCustom: Boolean
)

/**
 * Datos con los que se crea o edita un ejercicio propio.
 *
 * `id` nulo significa alta. Las etiquetas viajan por nombre porque los catalogos cerrados
 * (`body_parts`, `muscles`, `equipments`) se resuelven o se crean en el repositorio.
 */
data class ExerciseDraft(
    val id: String? = null,
    val name: String = "",
    val icon: ExerciseIcon = ExerciseIcon.DEFAULT,
    val instructions: List<String> = emptyList(),
    val bodyParts: List<String> = emptyList(),
    val targetMuscles: List<String> = emptyList(),
    val secondaryMuscles: List<String> = emptyList(),
    val equipments: List<String> = emptyList()
)

/** Vocabulario disponible para clasificar un ejercicio. */
data class ExerciseFacets(
    val bodyParts: List<String> = emptyList(),
    val muscles: List<String> = emptyList(),
    val equipments: List<String> = emptyList()
)
