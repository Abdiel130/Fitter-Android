package com.acdev.fitter.core.network

private const val SEPARATOR = ','
private const val TRAILING_SLASH = "/"

/**
 * Hosts candidatos a los que conectarse.
 *
 * La lista llega de `BuildConfig.API_BASE_URLS` como texto separado por comas, porque es la unica
 * forma de tener valores distintos por variante: en depuracion el emulador y la LAN, en produccion
 * un solo dominio.
 */
object BaseUrlCandidates {

    /** Normaliza la lista: recorta espacios, garantiza barra final y descarta entradas vacias. */
    fun from(raw: String): List<String> = raw
        .split(SEPARATOR)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { it.ensureTrailingSlash() }

    private fun String.ensureTrailingSlash(): String =
        if (endsWith(TRAILING_SLASH)) this else this + TRAILING_SLASH
}
