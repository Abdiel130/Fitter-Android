package com.acdev.fitter.core.security

import javax.crypto.Cipher

/** Motivo por el que se pide la huella. Determina el texto del dialogo, que vive en strings.xml. */
enum class BiometricReason { PROTECT_SESSION, RENEW_SESSION }

/**
 * Puerta biometrica.
 *
 * Devuelve el mismo `Cipher` ya autorizado por el chip, o null si el usuario cancelo, si no hay
 * actividad en primer plano o si el dispositivo no puede verificar. Nunca lanza: quien la llama
 * decide que hacer con un null, y en la app eso siempre significa "no se renueva, no se echa a
 * nadie".
 */
interface BiometricGate {
    suspend fun authorize(cipher: Cipher, reason: BiometricReason): Cipher?

    /** True si el dispositivo tiene biometria fuerte inscrita y usable ahora mismo. */
    fun canVerify(): Boolean
}
