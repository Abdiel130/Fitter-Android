package com.acdev.fitter.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.acdev.fitter.domain.model.AppPreferences
import com.acdev.fitter.domain.model.FitterPalette
import com.acdev.fitter.domain.model.ThemeMode
import com.acdev.fitter.domain.model.WeightUnit
import com.acdev.fitter.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Preferencias sobre DataStore.
 *
 * Los enums se leen con `enumOrDefault`: si el valor almacenado desaparece del codigo (por un
 * renombrado o una version antigua), la app arranca con el valor por defecto en lugar de romper.
 */
class PreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : PreferencesRepository {

    private object Keys {
        val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
        val palette = stringPreferencesKey("palette")
        val themeMode = stringPreferencesKey("theme_mode")
        val weightUnit = stringPreferencesKey("weight_unit")
        val displayName = stringPreferencesKey("display_name")
        val email = stringPreferencesKey("email")
    }

    override val preferences: Flow<AppPreferences> = dataStore.data.map { stored ->
        AppPreferences(
            onboardingCompleted = stored[Keys.onboardingCompleted] == true,
            palette = enumOrDefault(stored[Keys.palette], FitterPalette.MIDNIGHT),
            themeMode = enumOrDefault(stored[Keys.themeMode], ThemeMode.DARK),
            weightUnit = enumOrDefault(stored[Keys.weightUnit], WeightUnit.KG),
            displayName = stored[Keys.displayName].orEmpty(),
            email = stored[Keys.email].orEmpty()
        )
    }

    override suspend fun setPalette(palette: FitterPalette) {
        dataStore.edit { it[Keys.palette] = palette.name }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.themeMode] = mode.name }
    }

    override suspend fun setWeightUnit(unit: WeightUnit) {
        dataStore.edit { it[Keys.weightUnit] = unit.name }
    }

    override suspend fun setAccount(displayName: String, email: String) {
        dataStore.edit {
            it[Keys.displayName] = displayName
            it[Keys.email] = email
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.onboardingCompleted] = completed }
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(stored: String?, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == stored } ?: fallback
}
