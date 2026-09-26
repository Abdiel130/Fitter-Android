package com.acdev.fitter.data.remote.api

import com.acdev.fitter.data.remote.ApiEnvelope
import com.acdev.fitter.data.remote.dto.ProfileDataDto
import com.acdev.fitter.data.remote.dto.SetupRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface ProfileApi {

    @GET("api/profile")
    suspend fun profile(): Response<ApiEnvelope<ProfileDataDto>>

    /** Idempotente: repetir la misma llamada no mueve `setup_completed_at`. */
    @PATCH("api/profile/setup")
    suspend fun setup(@Body body: SetupRequestDto): Response<ApiEnvelope<ProfileDataDto>>
}
