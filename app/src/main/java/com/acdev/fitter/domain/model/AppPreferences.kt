package com.acdev.fitter.domain.model

/** Paletas de color disponibles. Cada una define su variante clara y oscura. */
enum class FitterPalette { MIDNIGHT, VOID }

/** Como se resuelve claro/oscuro. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Unidad con la que el usuario introduce y lee pesos. El almacenamiento siempre es en kg. */
enum class WeightUnit(val factorFromKg: Double) {
    KG(1.0),
    LBS(2.2046226218);

    fun fromKg(kg: Double): Double = kg * factorFromKg
    fun toKg(value: Double): Double = value / factorFromKg
}

/**
 * Preferencias del dispositivo. No viajan al servidor: describen como se ve y se mide la app
 * en este telefono concreto.
 */
data class AppPreferences(
    val onboardingCompleted: Boolean = false,
    val palette: FitterPalette = FitterPalette.MIDNIGHT,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val displayName: String = "",
    val email: String = ""
)
