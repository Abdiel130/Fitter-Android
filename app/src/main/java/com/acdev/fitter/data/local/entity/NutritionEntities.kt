package com.acdev.fitter.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.acdev.fitter.domain.model.MealType
import java.time.Instant
import java.time.LocalDate

/** Objetivo nutricional. El usuario puede tener varios (dia de entreno, descanso, flexible). */
@Entity(
    tableName = "nutrition_targets",
    foreignKeys = [ForeignKey(UserEntity::class, ["id"], ["userId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("userId")]
)
data class NutritionTargetEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val caloriesKcal: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val isDefault: Boolean = false,
    @Embedded val sync: SyncMetadata = SyncMetadata()
)

/**
 * Alimento. `userId == null` significa que viene del catalogo publico (Open Food Facts);
 * con id, es un alimento propio del usuario.
 */
@Entity(
    tableName = "food_items",
    foreignKeys = [ForeignKey(UserEntity::class, ["id"], ["userId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("userId"), Index(value = ["barcode"], unique = true)]
)
data class FoodItemEntity(
    @PrimaryKey val id: String,
    val userId: String? = null,
    val barcode: String? = null,
    val name: String,
    val brand: String? = null,
    val servingSizeG: Double = 100.0,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double,
    val fiberPer100g: Double = 0.0,
    val createdAt: Instant,
    @Embedded val sync: SyncMetadata = SyncMetadata()
)

/** Combinacion fija de alimentos: "Mi desayuno habitual". */
@Entity(
    tableName = "recipes",
    foreignKeys = [ForeignKey(UserEntity::class, ["id"], ["userId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("userId")]
)
data class RecipeEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val description: String? = null,
    @Embedded val sync: SyncMetadata = SyncMetadata()
)

@Entity(
    tableName = "recipe_items",
    foreignKeys = [
        ForeignKey(RecipeEntity::class, ["id"], ["recipeId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(FoodItemEntity::class, ["id"], ["foodItemId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("recipeId"), Index("foodItemId")]
)
data class RecipeItemEntity(
    @PrimaryKey val id: String,
    val recipeId: String,
    val foodItemId: String,
    val amountInGrams: Double,
    @Embedded val sync: SyncMetadata = SyncMetadata()
)

/**
 * Linea del diario de comidas.
 *
 * Una linea apunta a un alimento suelto **o** a una receta completa, nunca a los dos. El esquema
 * original marcaba ambas columnas como obligatorias; aqui son opcionales y la exclusividad la
 * garantiza el repositorio al insertar.
 *
 * Los macros se guardan ya calculados para que el resumen del dia no dependa de recalcular sobre
 * alimentos que el usuario puede editar despues.
 */
@Entity(
    tableName = "daily_food_logs",
    foreignKeys = [
        ForeignKey(UserEntity::class, ["id"], ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(FoodItemEntity::class, ["id"], ["foodItemId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(RecipeEntity::class, ["id"], ["recipeId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index(value = ["userId", "logDate"]), Index("foodItemId"), Index("recipeId")]
)
data class DailyFoodLogEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val foodItemId: String? = null,
    val recipeId: String? = null,
    val logDate: LocalDate,
    val mealType: MealType,
    val amountInGrams: Double,
    val calculatedCalories: Double,
    val calculatedProtein: Double,
    val calculatedCarbs: Double,
    val calculatedFat: Double,
    val createdAt: Instant,
    @Embedded val sync: SyncMetadata = SyncMetadata()
)
