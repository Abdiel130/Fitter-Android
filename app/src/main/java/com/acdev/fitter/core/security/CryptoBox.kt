package com.acdev.fitter.core.security

import javax.crypto.Cipher

/** Claves del Keystore que usa la app. */
enum class CryptoAlias(val keyName: String, val requiresUserAuth: Boolean) {
    /** Token de acceso: se cifra para que no quede en claro en disco, pero sin pedir huella. */
    ACCESS("fitter_access", requiresUserAuth = false),

    /** Token de renovacion protegido por el chip: descifrarlo exige verificar al usuario. */
    REFRESH_PROTECTED("fitter_refresh_bio", requiresUserAuth = true),

    /** Respaldo para dispositivos sin biometria ni credencial inscrita. */
    REFRESH_PLAIN("fitter_refresh_plain", requiresUserAuth = false);

    companion object {
        fun byKeyName(keyName: String?): CryptoAlias? = entries.firstOrNull { it.keyName == keyName }
    }
}

/**
 * Cifrado simetrico respaldado por hardware.
 *
 * Las claves que no exigen verificacion se usan con `seal`/`open` de un tiro. Las que si la exigen
 * necesitan que alguien autentique el `Cipher` antes de usarlo, y por eso se entregan primero:
 * quien pide la huella es `BiometricGate`, no este objeto.
 */
interface CryptoBox {
    fun seal(alias: CryptoAlias, plain: String): String
    fun open(alias: CryptoAlias, sealed: String): String

    fun encryptCipher(alias: CryptoAlias): Cipher
    fun decryptCipher(alias: CryptoAlias, sealed: String): Cipher
    fun sealWith(cipher: Cipher, plain: String): String
    fun openWith(cipher: Cipher, sealed: String): String

    /** Borra la clave. Lo que estuviera cifrado con ella queda inservible, que es la intencion. */
    fun deleteKey(alias: CryptoAlias)
}
