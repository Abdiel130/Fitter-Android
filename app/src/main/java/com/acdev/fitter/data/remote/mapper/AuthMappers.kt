package com.acdev.fitter.data.remote.mapper

import android.util.Log
import com.acdev.fitter.data.remote.dto.RemoteUserDto
import com.acdev.fitter.data.remote.dto.TokensDto
import com.acdev.fitter.domain.model.RemoteUser
import com.acdev.fitter.domain.model.SessionTokens
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

private const val TAG = "AuthMappers"

fun RemoteUserDto.toDomain(): RemoteUser = RemoteUser(
    id = id,
    name = name,
    email = email,
    currentRoutineId = currentRoutine,
    activeSequenceIndex = activeSequenceIndex,
    setupCompleted = isSetupCompleted
)

/**
 * Convierte el par de tokens. Devuelve null si una fecha no es ISO 8601: sin vencimiento fiable no
 * se puede decidir cuando renovar, y entrar con datos inventados seria peor que fallar.
 */
fun TokensDto.toDomainOrNull(): SessionTokens? {
    val accessExpiry = accessTokenExpiresAt.toInstantOrNull() ?: return null
    val refreshExpiry = refreshTokenExpiresAt.toInstantOrNull() ?: return null
    return SessionTokens(
        accessToken = accessToken,
        accessExpiresAt = accessExpiry,
        refreshToken = refreshToken,
        refreshExpiresAt = refreshExpiry
    )
}

/**
 * El servidor emite ISO 8601 con zona, y la escribe de dos formas: `...Z` en los tokens y
 * `...+00:00` en el perfil. `OffsetDateTime` acepta las dos; `Instant.parse` no siempre.
 */
private fun String.toInstantOrNull(): Instant? = try {
    OffsetDateTime.parse(this).toInstant()
} catch (error: DateTimeParseException) {
    Log.w(TAG, "Fecha de vencimiento no reconocida: $this", error)
    null
}
