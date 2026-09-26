package com.acdev.fitter.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val PROVIDER = "AndroidKeyStore"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val KEY_SIZE_BITS = 256
private const val GCM_TAG_BITS = 128
private const val IV_BYTES = 12
private const val AUTH_TIMEOUT_PER_USE = 0

/**
 * Cifrado AES-GCM con claves que nunca salen del Keystore.
 *
 * El vector de inicializacion se guarda delante del texto cifrado porque GCM lo genera nuevo en
 * cada operacion y hace falta para descifrar; no es secreto.
 */
class KeystoreCryptoBox : CryptoBox {

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(PROVIDER).apply { load(null) }
    }

    override fun seal(alias: CryptoAlias, plain: String): String =
        sealWith(encryptCipher(alias), plain)

    override fun open(alias: CryptoAlias, sealed: String): String =
        openWith(decryptCipher(alias, sealed), sealed)

    override fun encryptCipher(alias: CryptoAlias): Cipher =
        Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, keyOf(alias)) }

    override fun decryptCipher(alias: CryptoAlias, sealed: String): Cipher {
        val iv = decode(sealed).copyOfRange(0, IV_BYTES)
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, keyOf(alias), GCMParameterSpec(GCM_TAG_BITS, iv))
        }
    }

    override fun sealWith(cipher: Cipher, plain: String): String {
        val cipherText = cipher.doFinal(plain.toByteArray())
        return encode(cipher.iv + cipherText)
    }

    override fun openWith(cipher: Cipher, sealed: String): String {
        val payload = decode(sealed)
        return String(cipher.doFinal(payload, IV_BYTES, payload.size - IV_BYTES))
    }

    override fun deleteKey(alias: CryptoAlias) {
        if (keyStore.containsAlias(alias.keyName)) keyStore.deleteEntry(alias.keyName)
    }

    private fun keyOf(alias: CryptoAlias): SecretKey {
        val existing = keyStore.getKey(alias.keyName, null) as? SecretKey
        return existing ?: generateKey(alias)
    }

    private fun generateKey(alias: CryptoAlias): SecretKey {
        val builder = KeyGenParameterSpec.Builder(
            alias.keyName,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE_BITS)
            .setRandomizedEncryptionRequired(true)

        if (alias.requiresUserAuth) builder.requireUserVerification()

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
        generator.init(builder.build())
        return generator.generateKey()
    }

    /**
     * El margen de cero segundos significa que cada uso de la clave exige una verificacion nueva:
     * es lo que convierte el sensor del telefono en el candado del token de renovacion. Si el
     * usuario cambia sus huellas, la clave queda invalidada y el token deja de poder abrirse.
     */
    private fun KeyGenParameterSpec.Builder.requireUserVerification() {
        setUserAuthenticationRequired(true)
        setUserAuthenticationParameters(
            AUTH_TIMEOUT_PER_USE,
            KeyProperties.AUTH_BIOMETRIC_STRONG
        )
        setInvalidatedByBiometricEnrollment(true)
    }

    private fun encode(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

    private fun decode(value: String): ByteArray = Base64.getDecoder().decode(value)
}
