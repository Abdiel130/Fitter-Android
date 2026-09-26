package com.acdev.fitter.core.network

import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Apunta cada peticion al servidor resuelto.
 *
 * Retrofit fija su base al construirse, asi que en lugar de rehacer el cliente cada vez que cambia
 * la direccion se reescriben esquema, host y puerto aqui. El `runBlocking` es legitimo: corre en un
 * hilo de despacho de OkHttp, nunca en el principal.
 */
class BaseUrlInterceptor(private val resolver: BaseUrlResolver) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val target = runBlocking { resolver.resolve() }.toHttpUrlOrNull()
            ?: return chain.proceed(request)

        val rewritten = request.url.newBuilder()
            .scheme(target.scheme)
            .host(target.host)
            .port(target.port)
            .build()

        return chain.proceed(request.newBuilder().url(rewritten).build())
    }
}
