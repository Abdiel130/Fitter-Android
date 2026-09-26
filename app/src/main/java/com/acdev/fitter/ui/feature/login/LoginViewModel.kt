package com.acdev.fitter.ui.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acdev.fitter.core.network.BaseUrlResolver
import com.acdev.fitter.core.network.BaseUrlStore
import com.acdev.fitter.domain.model.LoginFieldIssue
import com.acdev.fitter.domain.model.LoginForm
import com.acdev.fitter.domain.model.RemoteError
import com.acdev.fitter.domain.model.RemoteResult
import com.acdev.fitter.domain.usecase.SignInUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val emailIssue: LoginFieldIssue? = null,
    val passwordIssue: LoginFieldIssue? = null,
    /** Fallo que no pertenece a ningun campo: credenciales, red o servidor. */
    val error: RemoteError? = null,
    val advancedVisible: Boolean = false,
    val detectedBaseUrl: String = "",
    val manualBaseUrl: String = ""
) {
    val canSubmit: Boolean
        get() = !isSubmitting && email.isNotBlank() && password.isNotBlank()
}

/** Acciones de la pantalla, agrupadas para que no dependa del ViewModel. */
data class LoginActions(
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onTogglePasswordVisible: () -> Unit,
    val onSubmit: () -> Unit,
    val onToggleAdvanced: () -> Unit,
    val onManualBaseUrlChange: (String) -> Unit,
    val onSaveManualBaseUrl: () -> Unit
)

/**
 * Formulario de acceso.
 *
 * No expone ningun evento de exito: al entrar se escribe la sesion, `SessionRepository` emite y la
 * raiz de la interfaz cambia de pantalla sola. Un solo origen de verdad.
 */
class LoginViewModel(
    private val signIn: SignInUseCase,
    private val baseUrlResolver: BaseUrlResolver,
    private val baseUrlStore: BaseUrlStore
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(manualBaseUrl = baseUrlStore.manualUrl()) }
            refreshDetectedHost()
        }
    }

    fun updateEmail(value: String) =
        _state.update { it.copy(email = value, emailIssue = null, error = null) }

    fun updatePassword(value: String) =
        _state.update { it.copy(password = value, passwordIssue = null, error = null) }

    fun togglePasswordVisible() =
        _state.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun toggleAdvanced() = _state.update { it.copy(advancedVisible = !it.advancedVisible) }

    fun updateManualBaseUrl(value: String) = _state.update { it.copy(manualBaseUrl = value) }

    /** Guardar una direccion a mano obliga a olvidar la detectada, sea cual fuera. */
    fun saveManualBaseUrl() {
        val manual = _state.value.manualBaseUrl
        viewModelScope.launch {
            baseUrlStore.saveManual(manual)
            baseUrlResolver.invalidate()
            refreshDetectedHost()
        }
    }

    fun signIn() {
        val snapshot = _state.value
        if (snapshot.isSubmitting) return

        val emailIssue = LoginForm.validateEmail(snapshot.email)
        val passwordIssue = LoginForm.validatePassword(snapshot.password)
        if (emailIssue != null || passwordIssue != null) {
            _state.update { it.copy(emailIssue = emailIssue, passwordIssue = passwordIssue) }
            return
        }

        _state.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch { submit(snapshot.email, snapshot.password) }
    }

    private suspend fun submit(email: String, password: String) {
        when (val result = signIn(email, password)) {
            is RemoteResult.Success -> _state.update { it.copy(isSubmitting = false) }
            is RemoteResult.Failure -> handleFailure(result.error)
        }
    }

    private suspend fun handleFailure(error: RemoteError) {
        _state.update { it.copy(isSubmitting = false, error = error) }
        if (!invalidatesHost(error)) return

        baseUrlResolver.invalidate()
        refreshDetectedHost()
    }

    /**
     * Estos tres fallos significan que la direccion recordada puede haber dejado de existir, porque
     * el telefono cambio de red o el servidor se movio. Se olvida para que el siguiente intento
     * vuelva a sondear. Un 401 o un 500, en cambio, prueban que la direccion es la correcta.
     */
    private fun invalidatesHost(error: RemoteError): Boolean = when (error) {
        RemoteError.Offline, RemoteError.ServerUnreachable, RemoteError.Timeout -> true
        else -> false
    }

    private suspend fun refreshDetectedHost() {
        val host = baseUrlResolver.resolve()
        _state.update { it.copy(detectedBaseUrl = host) }
    }
}
