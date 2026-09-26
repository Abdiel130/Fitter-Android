package com.acdev.fitter.data.session

import com.acdev.fitter.domain.model.SessionTokens

/**
 * Guarda y recupera los tokens de la sesion.
 *
 * Vive en `data` porque es infraestructura: quien la usa son el autenticador de red y el
 * repositorio de sesion, nunca el dominio.
 */
interface TokenStorage {
    /** Lectura rapida, sin pedir nada al usuario. */
    suspend fun accessToken(): String?

    /**
     * Token de renovacion. Puede abrir el dialogo de huella y devuelve null si el usuario cancela,
     * si la app esta en segundo plano o si la clave quedo invalidada.
     */
    suspend fun refreshToken(): String?

    /** True si se pudo guardar el par completo. */
    suspend fun save(tokens: SessionTokens): Boolean

    suspend fun clear()
}
