package com.acdev.fitter.core.network

import com.acdev.fitter.data.session.TokenStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Pone el token de acceso en cada peticion.
 *
 * No pisa un `Authorization` que ya venga puesto, y eso resuelve los dos casos especiales sin
 * marcadores: el login no lleva ninguno y la renovacion trae el suyo, que es el token de
 * renovacion y no el de acceso.
 */
class AuthHeaderInterceptor(private val tokenStorage: TokenStorage) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val withAccept = request.newBuilder().header(HEADER_ACCEPT, APPLICATION_JSON).build()

        if (withAccept.header(HEADER_AUTHORIZATION) != null) return chain.proceed(withAccept)

        val access = runBlocking { tokenStorage.accessToken() }
            ?: return chain.proceed(withAccept)

        return chain.proceed(
            withAccept.newBuilder()
                .header(HEADER_AUTHORIZATION, "$BEARER_PREFIX $access")
                .build()
        )
    }
}
