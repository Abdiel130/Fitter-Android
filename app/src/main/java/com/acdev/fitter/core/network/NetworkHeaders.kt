package com.acdev.fitter.core.network

/** Cabeceras y marcadores que comparten interceptor, autenticador y APIs. */
internal const val HEADER_AUTHORIZATION = "Authorization"
internal const val HEADER_ACCEPT = "Accept"
internal const val HEADER_RETRIED = "X-Fitter-Retried"
internal const val BEARER_PREFIX = "Bearer"
internal const val APPLICATION_JSON = "application/json"

/**
 * Base con la que se construye Retrofit. No se usa nunca de verdad: `BaseUrlInterceptor` reescribe
 * esquema, host y puerto en cada peticion, de modo que cambiar de servidor no obliga a reconstruir
 * el cliente.
 */
internal const val PLACEHOLDER_BASE_URL = "http://placeholder.invalid/"
