package com.acdev.fitter.data.remote

import com.acdev.fitter.domain.model.RemoteError

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_UNPROCESSABLE = 422

/**
 * Traduce codigo HTTP y sobre del servidor a un error de dominio.
 *
 * Funcion pura y por eso la que se prueba: el resto de la capa de red es fontaneria.
 */
fun mapHttpFailure(code: Int, envelope: ApiEnvelope<*>?): RemoteError = when (code) {
    HTTP_UNAUTHORIZED -> RemoteError.Unauthorized(
        envelope.firstFieldMessage() ?: envelope?.message
    )
    HTTP_UNPROCESSABLE -> RemoteError.Validation(envelope?.message, envelope?.errors.orEmpty())
    else -> RemoteError.Http(code, envelope?.message)
}

/**
 * El 401 del login trae el detalle util dentro de `errors.email[0]`, no en `message`, que es
 * generico. Se prefiere el del campo porque es el que el usuario necesita leer.
 */
private fun ApiEnvelope<*>?.firstFieldMessage(): String? =
    this?.errors?.values?.firstOrNull()?.firstOrNull()
