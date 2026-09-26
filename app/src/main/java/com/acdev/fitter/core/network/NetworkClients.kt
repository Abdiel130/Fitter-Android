package com.acdev.fitter.core.network

import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json

/**
 * Configuracion de la capa de red, en un solo sitio.
 *
 * `ignoreUnknownKeys` es deliberado: que el servidor anada un campo no debe romper una version de
 * la app que ya esta instalada.
 */
object NetworkClients {

    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    fun okHttp(
        baseUrlInterceptor: BaseUrlInterceptor,
        authHeaderInterceptor: AuthHeaderInterceptor,
        authenticator: Authenticator?,
        extraInterceptors: List<Interceptor> = emptyList()
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(baseUrlInterceptor)
        .addInterceptor(authHeaderInterceptor)
        .apply {
            extraInterceptors.forEach { addInterceptor(it) }
            authenticator?.let { authenticator(it) }
        }
        .build()

    fun retrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(PLACEHOLDER_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory(APPLICATION_JSON.toMediaType()))
        .build()
}
