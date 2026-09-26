package com.acdev.fitter.data.remote.api

import com.acdev.fitter.data.remote.ApiEnvelope
import com.acdev.fitter.data.remote.dto.LoginDataDto
import com.acdev.fitter.data.remote.dto.LoginRequestDto
import com.acdev.fitter.data.remote.dto.LogoutRequestDto
import com.acdev.fitter.data.remote.dto.TokensDataDto
import com.acdev.fitter.data.remote.dto.UserDataDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequestDto): Response<ApiEnvelope<LoginDataDto>>

    /**
     * El encabezado va a mano porque aqui viaja el token de renovacion, no el de acceso que pone
     * el interceptor. El servidor destruye el que se presenta y devuelve un par nuevo.
     */
    @POST("api/auth/refresh")
    suspend fun refresh(@Header("Authorization") bearer: String):
        Response<ApiEnvelope<TokensDataDto>>

    @POST("api/auth/logout")
    suspend fun logout(@Body body: LogoutRequestDto): Response<ApiEnvelope<Unit>>

    @GET("api/auth/me")
    suspend fun me(): Response<ApiEnvelope<UserDataDto>>
}
