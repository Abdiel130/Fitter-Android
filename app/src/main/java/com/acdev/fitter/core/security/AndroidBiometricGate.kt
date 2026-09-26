package com.acdev.fitter.core.security

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.acdev.fitter.R
import javax.crypto.Cipher
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

private const val TAG = "BiometricGate"
private const val PROMPT_TIMEOUT_MILLIS = 60_000L
private const val STRONG = BiometricManager.Authenticators.BIOMETRIC_STRONG

/**
 * Pide la huella y devuelve el `Cipher` autorizado.
 *
 * Dos detalles que no son opcionales:
 *
 * El dialogo solo existe en el hilo principal y con una actividad viva, asi que se salta ahi con
 * `withContext`; quien llama puede estar en un hilo de OkHttp.
 *
 * El margen de un minuto evita que una app enviada a segundo plano con el dialogo abierto deje
 * bloqueado para siempre el hilo que espera, y el `Mutex` evita que dos peticiones que caducan a la
 * vez pidan dos huellas seguidas.
 */
class AndroidBiometricGate(
    private val context: Context,
    private val activityProvider: CurrentActivityProvider
) : BiometricGate {

    private val mutex = Mutex()

    override fun canVerify(): Boolean =
        BiometricManager.from(context).canAuthenticate(STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS

    override suspend fun authorize(cipher: Cipher, reason: BiometricReason): Cipher? =
        mutex.withLock {
            withTimeoutOrNull(PROMPT_TIMEOUT_MILLIS) { prompt(cipher, reason) }
        }

    private suspend fun prompt(cipher: Cipher, reason: BiometricReason): Cipher? =
        withContext(Dispatchers.Main.immediate) {
            val activity = activityProvider.current()
            if (activity == null) {
                Log.i(TAG, "Sin actividad en primer plano: no se puede pedir la huella")
                return@withContext null
            }

            suspendCancellableCoroutine { continuation ->
                val prompt = BiometricPrompt(
                    activity,
                    ContextCompat.getMainExecutor(context),
                    continuation.asCallback()
                )
                prompt.authenticate(
                    promptInfo(reason),
                    BiometricPrompt.CryptoObject(cipher)
                )
                continuation.invokeOnCancellation { prompt.cancelAuthentication() }
            }
        }

    /**
     * Un intento fallido no resuelve nada: el dialogo sigue abierto y el usuario puede repetir. Solo
     * el exito y el error definitivo terminan la espera.
     */
    private fun CancellableContinuation<Cipher?>.asCallback() =
        object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                if (isActive) resume(result.cryptoObject?.cipher)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Log.i(TAG, "Verificacion interrumpida ($errorCode): $errString")
                if (isActive) resume(null)
            }
        }

    private fun promptInfo(reason: BiometricReason): BiometricPrompt.PromptInfo {
        val titleRes = when (reason) {
            BiometricReason.PROTECT_SESSION -> R.string.auth_biometric_protect_title
            BiometricReason.RENEW_SESSION -> R.string.auth_biometric_renew_title
        }
        val subtitleRes = when (reason) {
            BiometricReason.PROTECT_SESSION -> R.string.auth_biometric_protect_subtitle
            BiometricReason.RENEW_SESSION -> R.string.auth_biometric_renew_subtitle
        }

        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(titleRes))
            .setSubtitle(context.getString(subtitleRes))
            .setNegativeButtonText(context.getString(R.string.auth_biometric_cancel))
            .setAllowedAuthenticators(STRONG)
            .setConfirmationRequired(false)
            .build()
    }
}
