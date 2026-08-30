package com.acdev.fitter.data.local

import androidx.room.TypeConverter
import com.acdev.fitter.domain.model.MealType
import com.acdev.fitter.domain.model.PoseType
import com.acdev.fitter.domain.model.SetType
import com.acdev.fitter.domain.model.SyncState
import com.acdev.fitter.domain.model.WeightUnit
import java.time.Instant
import java.time.LocalDate

/**
 * Conversores de la base local.
 *
 * Criterio: instantes como epoch millis (ordenables y compactos) y fechas civiles como texto ISO
 * (`2026-08-30`), porque una fecha de registro no debe moverse al cambiar de zona horaria.
 * Los enums se guardan por nombre, no por ordinal: reordenar el enum no debe corromper datos.
 */
class FitterConverters {

    @TypeConverter
    fun instantToMillis(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun millisToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun localDateToText(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun textToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun syncStateToText(value: SyncState): String = value.name

    @TypeConverter
    fun textToSyncState(value: String): SyncState = SyncState.valueOf(value)

    @TypeConverter
    fun setTypeToText(value: SetType): String = value.name

    @TypeConverter
    fun textToSetType(value: String): SetType = SetType.valueOf(value)

    @TypeConverter
    fun poseTypeToText(value: PoseType): String = value.name

    @TypeConverter
    fun textToPoseType(value: String): PoseType = PoseType.valueOf(value)

    @TypeConverter
    fun mealTypeToText(value: MealType): String = value.name

    @TypeConverter
    fun textToMealType(value: String): MealType = MealType.valueOf(value)

    @TypeConverter
    fun weightUnitToText(value: WeightUnit): String = value.name

    @TypeConverter
    fun textToWeightUnit(value: String): WeightUnit = WeightUnit.valueOf(value)
}
