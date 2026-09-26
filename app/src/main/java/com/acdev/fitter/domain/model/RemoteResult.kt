package com.acdev.fitter.domain.model

/**
 * Motivos por los que una peticion no dio el dato esperado.
 *
 * Es un catalogo cerrado y sin rastro de la libreria de red: el dominio decide que hacer con un
 * fallo sin saber si por debajo hay Retrofit, OkHttp o un socket pelado.
 */
sealed interface RemoteError {
    /** El dispositivo no tiene red. El dato se queda en la cola local, que es lo correcto. */
    data object Offline : RemoteError

    /**
     * Hay red, pero el servidor no contesta: nombre que no resuelve, puerto cerrado, backend
     * apagado. Se separa de `Offline` porque el usuario no tiene nada que revisar en su conexion y
     * pedirle que lo haga le manda a buscar donde no esta el problema.
     */
    data object ServerUnreachable : RemoteError

    data object Timeout : RemoteError

    /** 401: la credencial o el token ya no valen. */
    data class Unauthorized(val message: String?) : RemoteError

    /** 422: el servidor devuelve un mensaje por campo, tal cual lo manda Laravel. */
    data class Validation(
        val message: String?,
        val fields: Map<String, List<String>>
    ) : RemoteError

    data class Http(val code: Int, val message: String?) : RemoteError

    /** Respuesta ilegible o fallo no previsto. Se registra antes de construirse, nunca se traga. */
    data class Unexpected(val detail: String) : RemoteError
}

sealed interface RemoteResult<out T> {
    data class Success<T>(val value: T) : RemoteResult<T>
    data class Failure(val error: RemoteError) : RemoteResult<Nothing>
}

inline fun <T, R> RemoteResult<T>.map(transform: (T) -> R): RemoteResult<R> = when (this) {
    is RemoteResult.Success -> RemoteResult.Success(transform(value))
    is RemoteResult.Failure -> this
}

/** Valor si la llamada fue bien, o null. Util cuando el fallo ya se trato en otro sitio. */
fun <T> RemoteResult<T>.valueOrNull(): T? = (this as? RemoteResult.Success)?.value

/**
 * Unico motivo para dar la sesion por perdida: que el servidor diga explicitamente que el token no
 * vale. Sin red, o con un error del servidor, no se echa a nadie de la app.
 */
fun shouldClearSession(error: RemoteError): Boolean = error is RemoteError.Unauthorized
