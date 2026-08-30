package com.acdev.fitter.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Colores que Material 3 no cubre y que Fitter necesita en toda la app.
 *
 * Regla del proyecto: ningun composable declara un Color literal. Si hace falta un color nuevo,
 * se anade aqui y se define en las cuatro combinaciones (Midnight/Void x claro/oscuro).
 */
@Immutable
data class FitterExtendedColors(
    val hairline: Color,
    val ringTrack: Color,
    val record: Color,
    val onRecord: Color,
    val positive: Color,
    val warning: Color,
    val syncSynced: Color,
    val syncPending: Color,
    val syncError: Color,
    val syncOffline: Color,
    val islandBackground: Color,
    val islandBorder: Color,
    val islandContent: Color,
    val islandContentMuted: Color,
    val islandTrack: Color,
    val islandButton: Color
)

// ---------------------------------------------------------------------------
// Paleta A - Midnight
// ---------------------------------------------------------------------------

internal val MidnightDarkScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF4DE1C1),
    onPrimary = Color(0xFF08120F),
    primaryContainer = Color(0xFF0F3B35),
    onPrimaryContainer = Color(0xFFA8F5E6),
    secondary = Color(0xFF7A8CFF),
    onSecondary = Color(0xFF0A0F2E),
    secondaryContainer = Color(0xFF23285C),
    onSecondaryContainer = Color(0xFFD3D9FF),
    tertiary = Color(0xFFFFB454),
    onTertiary = Color(0xFF2A1A00),
    tertiaryContainer = Color(0xFF4A3312),
    onTertiaryContainer = Color(0xFFFFE0B2),
    error = Color(0xFFFF6B7A),
    onError = Color(0xFF2A0409),
    errorContainer = Color(0xFF5C1420),
    onErrorContainer = Color(0xFFFFD5DA),
    background = Color(0xFF080A12),
    onBackground = Color(0xFFEDF1FA),
    surface = Color(0xFF080A12),
    onSurface = Color(0xFFEDF1FA),
    surfaceVariant = Color(0xFF1C2233),
    onSurfaceVariant = Color(0xFFA8B2C8),
    surfaceContainerLowest = Color(0xFF05070D),
    surfaceContainerLow = Color(0xFF0F131C),
    surfaceContainer = Color(0xFF141824),
    surfaceContainerHigh = Color(0xFF1C2233),
    surfaceContainerHighest = Color(0xFF232A3D),
    outline = Color(0xFF3A425A),
    outlineVariant = Color(0xFF262D40),
    inverseSurface = Color(0xFFEDF1FA),
    inverseOnSurface = Color(0xFF0F131C),
    inversePrimary = Color(0xFF00A388),
    scrim = Color(0xFF000000)
)

internal val MidnightLightScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF00A388),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB6F2E5),
    onPrimaryContainer = Color(0xFF00201A),
    secondary = Color(0xFF4A5BD6),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDDE1FF),
    onSecondaryContainer = Color(0xFF06105C),
    tertiary = Color(0xFFB57A12),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFE0B2),
    onTertiaryContainer = Color(0xFF3A2500),
    error = Color(0xFFBA1A2A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD8),
    onErrorContainer = Color(0xFF410008),
    background = Color(0xFFF2F4FA),
    onBackground = Color(0xFF101625),
    surface = Color(0xFFF2F4FA),
    onSurface = Color(0xFF101625),
    surfaceVariant = Color(0xFFE9EDF7),
    onSurfaceVariant = Color(0xFF4A5265),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFAFBFE),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFE9EDF7),
    surfaceContainerHighest = Color(0xFFE1E7F4),
    outline = Color(0xFF7B849B),
    outlineVariant = Color(0xFFD8DEEC),
    inverseSurface = Color(0xFF1B2233),
    inverseOnSurface = Color(0xFFF2F4FA),
    inversePrimary = Color(0xFF4DE1C1),
    scrim = Color(0xFF000000)
)

internal val MidnightDarkExtended = FitterExtendedColors(
    hairline = Color(0xFF262D40),
    ringTrack = Color(0xFF1C2233),
    record = Color(0xFFFFB454),
    onRecord = Color(0xFF2A1A00),
    positive = Color(0xFF4DE1C1),
    warning = Color(0xFFFFB454),
    syncSynced = Color(0xFF4DE1C1),
    syncPending = Color(0xFFFFB454),
    syncError = Color(0xFFFF6B7A),
    syncOffline = Color(0xFF7C86A0),
    islandBackground = Color(0xFF000000),
    islandBorder = Color(0x00000000),
    islandContent = Color(0xFFFFFFFF),
    islandContentMuted = Color(0xFFB8C0D4),
    islandTrack = Color(0xFF2A3040),
    islandButton = Color(0xFF1E2432)
)

internal val MidnightLightExtended = FitterExtendedColors(
    hairline = Color(0xFFD8DEEC),
    ringTrack = Color(0xFFE1E7F4),
    record = Color(0xFFB57A12),
    onRecord = Color(0xFFFFFFFF),
    positive = Color(0xFF00A388),
    warning = Color(0xFFB57A12),
    syncSynced = Color(0xFF00A388),
    syncPending = Color(0xFFB57A12),
    syncError = Color(0xFFBA1A2A),
    syncOffline = Color(0xFF5C6580),
    islandBackground = Color(0xFF0B0E16),
    islandBorder = Color(0x00000000),
    islandContent = Color(0xFFFFFFFF),
    islandContentMuted = Color(0xFFA9B2C6),
    islandTrack = Color(0xFF2A3040),
    islandButton = Color(0xFF232936)
)

// ---------------------------------------------------------------------------
// Paleta B - Void (AMOLED)
// ---------------------------------------------------------------------------

internal val VoidDarkScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF37D6B4),
    onPrimary = Color(0xFF00120E),
    primaryContainer = Color(0xFF0B322C),
    onPrimaryContainer = Color(0xFF9DEFDD),
    secondary = Color(0xFF6E80EE),
    onSecondary = Color(0xFF070B24),
    secondaryContainer = Color(0xFF1B2050),
    onSecondaryContainer = Color(0xFFC9D0FF),
    tertiary = Color(0xFFE8A340),
    onTertiary = Color(0xFF231500),
    tertiaryContainer = Color(0xFF3D2A0D),
    onTertiaryContainer = Color(0xFFFFDDA8),
    error = Color(0xFFF25F6E),
    onError = Color(0xFF230307),
    errorContainer = Color(0xFF4E101B),
    onErrorContainer = Color(0xFFFFD0D6),
    background = Color(0xFF000000),
    onBackground = Color(0xFFEAEEF5),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFEAEEF5),
    surfaceVariant = Color(0xFF12161D),
    onSurfaceVariant = Color(0xFF98A2B5),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF06080B),
    surfaceContainer = Color(0xFF000000),
    surfaceContainerHigh = Color(0xFF0B0D12),
    surfaceContainerHighest = Color(0xFF12161D),
    outline = Color(0xFF2B3341),
    outlineVariant = Color(0xFF1C222E),
    inverseSurface = Color(0xFFEAEEF5),
    inverseOnSurface = Color(0xFF06080B),
    inversePrimary = Color(0xFF00947C),
    scrim = Color(0xFF000000)
)

internal val VoidLightScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF00947C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFACEFE0),
    onPrimaryContainer = Color(0xFF001C17),
    secondary = Color(0xFF3F50C8),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD9DEFF),
    onSecondaryContainer = Color(0xFF040D52),
    tertiary = Color(0xFFA96F0E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDDA8),
    onTertiaryContainer = Color(0xFF322100),
    error = Color(0xFFB3132A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFD8DC),
    onErrorContainer = Color(0xFF3D0008),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFF4F6FA),
    onSurfaceVariant = Color(0xFF5A6272),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFCFDFE),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFF4F6FA),
    surfaceContainerHighest = Color(0xFFECEFF5),
    outline = Color(0xFF6A7280),
    outlineVariant = Color(0xFFE0E4EC),
    inverseSurface = Color(0xFF14181F),
    inverseOnSurface = Color(0xFFFFFFFF),
    inversePrimary = Color(0xFF37D6B4),
    scrim = Color(0xFF000000)
)

internal val VoidDarkExtended = FitterExtendedColors(
    hairline = Color(0xFF1C222E),
    ringTrack = Color(0xFF141922),
    record = Color(0xFFE8A340),
    onRecord = Color(0xFF231500),
    positive = Color(0xFF37D6B4),
    warning = Color(0xFFE8A340),
    syncSynced = Color(0xFF37D6B4),
    syncPending = Color(0xFFE8A340),
    syncError = Color(0xFFF25F6E),
    syncOffline = Color(0xFF6E7788),
    islandBackground = Color(0xFF000000),
    islandBorder = Color(0xFF232B39),
    islandContent = Color(0xFFFFFFFF),
    islandContentMuted = Color(0xFF98A2B5),
    islandTrack = Color(0xFF1E2431),
    islandButton = Color(0xFF161B24)
)

internal val VoidLightExtended = FitterExtendedColors(
    hairline = Color(0xFFE0E4EC),
    ringTrack = Color(0xFFEAEDF3),
    record = Color(0xFFA96F0E),
    onRecord = Color(0xFFFFFFFF),
    positive = Color(0xFF00947C),
    warning = Color(0xFFA96F0E),
    syncSynced = Color(0xFF00947C),
    syncPending = Color(0xFFA96F0E),
    syncError = Color(0xFFB3132A),
    syncOffline = Color(0xFF5A6272),
    islandBackground = Color(0xFF000000),
    islandBorder = Color(0x00000000),
    islandContent = Color(0xFFFFFFFF),
    islandContentMuted = Color(0xFFA9B2C6),
    islandTrack = Color(0xFF2A3040),
    islandButton = Color(0xFF232936)
)
