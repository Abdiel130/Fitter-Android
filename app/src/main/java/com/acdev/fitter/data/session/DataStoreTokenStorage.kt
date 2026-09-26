package com.acdev.fitter.data.session

import android.security.keystore.KeyPermanentlyInvalidatedException
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.acdev.fitter.core.security.BiometricGate
import com.acdev.fitter.core.security.BiometricReason
import com.acdev.fitter.core.security.CryptoAlias
import com.acdev.fitter.core.security.CryptoBox
import com.acdev.fitter.domain.model.SessionTokens
import java.security.GeneralSecurityException
import kotlinx.coroutines.flow.first

private const val TAG = "TokenStorage"

/**
 * Tokens cifrados sobre DataStore.
 *
 * El de acceso se sella con una clave que no pide nada al usuario: hace falta en cada peticion y
 * pedir la huella cada vez seria inusable. El de renovacion se sella con la clave protegida por el
 * chip, de modo que abrirlo exige verificar al usuario aunque alguien se lleve el archivo.
 *
 * Si el dispositivo no tiene biometria fuerte inscrita se cae a la clave sin verificacion y se
 * anota cual se uso. Es la unica concesion del esquema, y existe porque la alternativa seria una
 * sesion que no se puede guardar en ese telefono.
 */
class DataStoreTokenStorage(
    private val dataStore: DataStore<Preferences>,
    private val cryptoBox: CryptoBox,
    private val biometricGate: BiometricGate
) : TokenStorage {

    override suspend fun accessToken(): String? {
        val sealed = dataStore.data.first()[SessionKeys.accessSealed] ?: return null
        return openQuietly(CryptoAlias.ACCESS, sealed)
    }

    override suspend fun refreshToken(): String? {
        val stored = dataStore.data.first()
        val sealed = stored[SessionKeys.refreshSealed] ?: return null
        val alias = CryptoAlias.byKeyName(stored[SessionKeys.refreshKeyAlias]) ?: return null

        return when (alias.requiresUserAuth) {
            true -> openVerified(alias, sealed)
            false -> openQuietly(alias, sealed)
        }
    }

    override suspend fun save(tokens: SessionTokens): Boolean {
        val access = sealQuietly(CryptoAlias.ACCESS, tokens.accessToken) ?: return false
        val refresh = sealRefresh(tokens.refreshToken) ?: return false

        dataStore.edit { stored ->
            stored[SessionKeys.accessSealed] = access
            stored[SessionKeys.accessExpiresAt] = tokens.accessExpiresAt.toEpochMilli()
            stored[SessionKeys.refreshSealed] = refresh.sealed
            stored[SessionKeys.refreshExpiresAt] = tokens.refreshExpiresAt.toEpochMilli()
            stored[SessionKeys.refreshKeyAlias] = refresh.alias.keyName
        }
        return true
    }

    /**
     * Borra los tokens y destruye las claves. Room no se toca: los entrenamientos son del usuario y
     * siguen en el dispositivo hasta que vuelva a entrar.
     */
    override suspend fun clear() {
        dataStore.edit { stored ->
            stored.remove(SessionKeys.accessSealed)
            stored.remove(SessionKeys.accessExpiresAt)
            stored.remove(SessionKeys.refreshSealed)
            stored.remove(SessionKeys.refreshExpiresAt)
            stored.remove(SessionKeys.refreshKeyAlias)
        }
        CryptoAlias.entries.forEach { alias -> deleteQuietly(alias) }
    }

    private data class SealedRefresh(val sealed: String, val alias: CryptoAlias)

    /**
     * Con la clave protegida, cifrar tambien exige verificacion: por eso se pide la huella al
     * guardar y no solo al leer. Si el usuario la rechaza se cae a la clave sin verificacion, para
     * no dejar la sesion sin poder persistirse.
     */
    private suspend fun sealRefresh(token: String): SealedRefresh? {
        val protected = sealProtected(token)
        if (protected != null) return protected

        val plain = sealQuietly(CryptoAlias.REFRESH_PLAIN, token) ?: return null
        return SealedRefresh(plain, CryptoAlias.REFRESH_PLAIN)
    }

    private suspend fun sealProtected(token: String): SealedRefresh? {
        if (!biometricGate.canVerify()) return null

        return try {
            val cipher = cryptoBox.encryptCipher(CryptoAlias.REFRESH_PROTECTED)
            val authorized = biometricGate.authorize(cipher, BiometricReason.PROTECT_SESSION)
                ?: return null
            SealedRefresh(cryptoBox.sealWith(authorized, token), CryptoAlias.REFRESH_PROTECTED)
        } catch (error: GeneralSecurityException) {
            Log.w(TAG, "No se pudo usar la clave protegida por el chip", error)
            null
        }
    }

    private suspend fun openVerified(alias: CryptoAlias, sealed: String): String? = try {
        val cipher = cryptoBox.decryptCipher(alias, sealed)
        val authorized = biometricGate.authorize(cipher, BiometricReason.RENEW_SESSION)
            ?: return null
        cryptoBox.openWith(authorized, sealed)
    } catch (error: KeyPermanentlyInvalidatedException) {
        Log.w(TAG, "La clave quedo invalidada al cambiar las huellas del dispositivo", error)
        null
    } catch (error: GeneralSecurityException) {
        Log.w(TAG, "No se pudo abrir el token de renovacion", error)
        null
    }

    private fun openQuietly(alias: CryptoAlias, sealed: String): String? = try {
        cryptoBox.open(alias, sealed)
    } catch (error: GeneralSecurityException) {
        Log.w(TAG, "No se pudo descifrar ${alias.keyName}", error)
        null
    } catch (error: IllegalArgumentException) {
        Log.w(TAG, "Contenido cifrado ilegible en ${alias.keyName}", error)
        null
    }

    private fun sealQuietly(alias: CryptoAlias, plain: String): String? = try {
        cryptoBox.seal(alias, plain)
    } catch (error: GeneralSecurityException) {
        Log.w(TAG, "No se pudo cifrar con ${alias.keyName}", error)
        null
    }

    private fun deleteQuietly(alias: CryptoAlias) = try {
        cryptoBox.deleteKey(alias)
    } catch (error: GeneralSecurityException) {
        Log.w(TAG, "No se pudo borrar la clave ${alias.keyName}", error)
    }
}
