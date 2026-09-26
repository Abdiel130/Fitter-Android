package com.acdev.fitter.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Frontera con el servidor. Es el unico lugar del proyecto donde existe snake_case.
 *
 * Las fechas se quedan como texto y se convierten en `AuthMappers`: asi un formato inesperado
 * rompe solo ese campo al mapear, no la deserializacion entera.
 */
@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
    @SerialName("device_name") val deviceName: String
)

@Serializable
data class LogoutRequestDto(
    @SerialName("all_devices") val allDevices: Boolean = false
)

@Serializable
data class TokensDto(
    @SerialName("token_type") val tokenType: String = "Bearer",
    @SerialName("access_token") val accessToken: String,
    @SerialName("access_token_expires_at") val accessTokenExpiresAt: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("refresh_token_expires_at") val refreshTokenExpiresAt: String
)

@Serializable
data class RemoteUserDto(
    val id: String,
    val name: String,
    val email: String,
    @SerialName("current_routine") val currentRoutine: String? = null,
    @SerialName("active_sequence_index") val activeSequenceIndex: Int = 1,
    @SerialName("is_setup_completed") val isSetupCompleted: Boolean = false,
    @SerialName("setup_completed_at") val setupCompletedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class LoginDataDto(val user: RemoteUserDto, val tokens: TokensDto)

@Serializable
data class TokensDataDto(val tokens: TokensDto)

@Serializable
data class UserDataDto(val user: RemoteUserDto)
