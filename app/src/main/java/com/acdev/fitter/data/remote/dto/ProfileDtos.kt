package com.acdev.fitter.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SetupRequestDto(
    @SerialName("is_setup_completed") val isSetupCompleted: Boolean
)

/** `/profile` devuelve el mismo objeto base que el login, bajo otra llave. */
@Serializable
data class ProfileDataDto(val profile: RemoteUserDto)
