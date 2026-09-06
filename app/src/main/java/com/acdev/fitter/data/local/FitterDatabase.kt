package com.acdev.fitter.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.acdev.fitter.data.local.dao.BodyMeasurementDao
import com.acdev.fitter.data.local.dao.ExerciseDao
import com.acdev.fitter.data.local.dao.NutritionDao
import com.acdev.fitter.data.local.dao.RecoveryDao
import com.acdev.fitter.data.local.dao.SyncDao
import com.acdev.fitter.data.local.dao.TrainingDao
import com.acdev.fitter.data.local.dao.UserDao
import com.acdev.fitter.data.local.dao.WorkoutLogDao
import com.acdev.fitter.data.local.entity.BodyMeasurementEntity
import com.acdev.fitter.data.local.entity.BodyPartEntity
import com.acdev.fitter.data.local.entity.DailyFoodLogEntity
import com.acdev.fitter.data.local.entity.DailyHabitLogEntity
import com.acdev.fitter.data.local.entity.DailySupplementLogEntity
import com.acdev.fitter.data.local.entity.EquipmentEntity
import com.acdev.fitter.data.local.entity.ExerciseBodyPartCrossRef
import com.acdev.fitter.data.local.entity.ExerciseEntity
import com.acdev.fitter.data.local.entity.ExerciseEquipmentCrossRef
import com.acdev.fitter.data.local.entity.ExerciseMuscleCrossRef
import com.acdev.fitter.data.local.entity.ExerciseSubstituteEntity
import com.acdev.fitter.data.local.entity.FoodItemEntity
import com.acdev.fitter.data.local.entity.JointDiscomfortLogEntity
import com.acdev.fitter.data.local.entity.MuscleEntity
import com.acdev.fitter.data.local.entity.NutritionTargetEntity
import com.acdev.fitter.data.local.entity.ProgressPhotoEntity
import com.acdev.fitter.data.local.entity.RecipeEntity
import com.acdev.fitter.data.local.entity.RecipeItemEntity
import com.acdev.fitter.data.local.entity.RoutineEntity
import com.acdev.fitter.data.local.entity.RoutineWorkoutEntity
import com.acdev.fitter.data.local.entity.SupplementEntity
import com.acdev.fitter.data.local.entity.UserEntity
import com.acdev.fitter.data.local.entity.WorkoutEntity
import com.acdev.fitter.data.local.entity.WorkoutExerciseEntity
import com.acdev.fitter.data.local.entity.WorkoutLogEntity
import com.acdev.fitter.data.local.entity.WorkoutLogSetEntity
import com.acdev.fitter.data.local.entity.WorkoutSetEntity
import com.acdev.fitter.data.local.migration.FitterMigrations

/**
 * Base de datos local. Es la unica fuente de verdad de la app: la UI jamas lee de la red.
 *
 * Reglas de evolucion del esquema:
 * - Subir `version` y anadir una `Migration` explicita. No se usa `fallbackToDestructiveMigration`
 *   fuera de depuracion porque los datos del usuario no existen en ningun otro sitio.
 * - Los esquemas exportados viven en `app/schemas` y se versionan en git.
 */
@Database(
    entities = [
        UserEntity::class,
        RoutineEntity::class,
        WorkoutEntity::class,
        RoutineWorkoutEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSetEntity::class,
        ExerciseEntity::class,
        BodyPartEntity::class,
        MuscleEntity::class,
        EquipmentEntity::class,
        ExerciseBodyPartCrossRef::class,
        ExerciseEquipmentCrossRef::class,
        ExerciseMuscleCrossRef::class,
        ExerciseSubstituteEntity::class,
        WorkoutLogEntity::class,
        WorkoutLogSetEntity::class,
        BodyMeasurementEntity::class,
        ProgressPhotoEntity::class,
        NutritionTargetEntity::class,
        FoodItemEntity::class,
        RecipeEntity::class,
        RecipeItemEntity::class,
        DailyFoodLogEntity::class,
        DailyHabitLogEntity::class,
        SupplementEntity::class,
        DailySupplementLogEntity::class,
        JointDiscomfortLogEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(FitterConverters::class)
abstract class FitterDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun trainingDao(): TrainingDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutLogDao(): WorkoutLogDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun recoveryDao(): RecoveryDao
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
    abstract fun syncDao(): SyncDao

    companion object {
        private const val NAME = "fitter.db"

        fun build(context: Context): FitterDatabase =
            Room.databaseBuilder(context.applicationContext, FitterDatabase::class.java, NAME)
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .addMigrations(*FitterMigrations.all)
                .build()
    }
}
