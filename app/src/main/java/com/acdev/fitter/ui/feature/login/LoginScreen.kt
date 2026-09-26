package com.acdev.fitter.ui.feature.login

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.acdev.fitter.BuildConfig
import com.acdev.fitter.R
import com.acdev.fitter.domain.model.LoginFieldIssue
import com.acdev.fitter.domain.model.RemoteError
import com.acdev.fitter.ui.components.FitterCard
import com.acdev.fitter.ui.components.FitterIconButton
import com.acdev.fitter.ui.components.FitterPrimaryButton
import com.acdev.fitter.ui.components.FitterTextButton
import com.acdev.fitter.ui.components.FitterTextField
import com.acdev.fitter.ui.components.ProgressRing
import com.acdev.fitter.ui.components.PulseLoader
import com.acdev.fitter.ui.components.SectionHeader
import com.acdev.fitter.ui.icons.FitterIcons
import com.acdev.fitter.ui.theme.FitterMotion
import com.acdev.fitter.ui.theme.FitterTheme

private const val SEAL_IDLE_PROGRESS = 0.15f
private const val SEAL_ERROR_PROGRESS = 0.5f
private const val SEAL_BUSY_PROGRESS = 0.75f
private const val EMPTY_LABEL = ""

/**
 * Puerta de entrada a la app.
 *
 * Es la unica pantalla sin armazon: ni `FitterScreen`, ni barra inferior, ni isla. En su lugar, un
 * fondo que respira y un sello que cuenta el estado con el mismo lenguaje que la isla dinamica, de
 * modo que entrar se sienta distinto a todo lo demas sin inventar ni un color ni una medida.
 */
@Composable
fun LoginScreen(
    state: LoginUiState,
    actions: LoginActions,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        AuroraBackdrop(Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = FitterTheme.spacing.screenHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Seal(state)
            Brand(actions)
            FormError(state.error)
            Fields(state, actions)

            FitterPrimaryButton(
                text = stringResource(R.string.auth_sign_in),
                onClick = actions.onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = FitterTheme.spacing.lg),
                enabled = state.canSubmit,
                loading = state.isSubmitting
            )

            Text(
                text = stringResource(R.string.auth_no_account),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = FitterTheme.spacing.lg)
            )

            ServerSection(state, actions)
        }
    }
}

/**
 * Sello de estado.
 *
 * El anillo no mide progreso hacia una meta: cuenta como va el intento, y por eso cambia de color
 * en lugar de llenarse.
 */
@Composable
private fun Seal(state: LoginUiState) {
    val accent by animateColorAsState(
        targetValue = when (state.error) {
            null -> MaterialTheme.colorScheme.primary
            else -> FitterTheme.colors.syncError
        },
        animationSpec = FitterMotion.standardTween(),
        label = "sealAccent"
    )

    val progress = when {
        state.error != null -> SEAL_ERROR_PROGRESS
        state.isSubmitting -> SEAL_BUSY_PROGRESS
        else -> SEAL_IDLE_PROGRESS
    }

    ProgressRing(
        progress = progress,
        label = EMPTY_LABEL,
        size = FitterTheme.sizes.ringMedium,
        color = accent,
        center = {
            AnimatedContent(targetState = state.isSubmitting, label = "sealCenter") { busy ->
                when (busy) {
                    true -> PulseLoader(contentDescription = null, color = accent)
                    false -> Icon(
                        imageVector = FitterIcons.Lock,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(FitterTheme.sizes.iconMedium)
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Brand(actions: LoginActions) {
    // La pulsacion larga revela la direccion del servidor, y solo en depuracion: nadie debe poder
    // redirigir el trafico de una app publicada desde su propia pantalla de acceso.
    val interactionSource = remember { MutableInteractionSource() }
    val revealModifier = when {
        BuildConfig.DEBUG -> Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onLongClick = actions.onToggleAdvanced,
            onClick = {}
        )
        else -> Modifier
    }

    Text(
        text = stringResource(R.string.auth_title),
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = revealModifier.padding(top = FitterTheme.spacing.xl)
    )
    Text(
        text = stringResource(R.string.auth_subtitle),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(
            top = FitterTheme.spacing.xs,
            bottom = FitterTheme.spacing.xxl
        )
    )
}

@Composable
private fun Fields(state: LoginUiState, actions: LoginActions) {
    Column(verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.lg)) {
        FitterTextField(
            value = state.email,
            onValueChange = actions.onEmailChange,
            label = stringResource(R.string.auth_email),
            keyboardType = KeyboardType.Email,
            isError = state.emailIssue != null,
            supportingText = state.emailIssue?.let { stringResource(it.messageRes()) },
            imeAction = ImeAction.Next
        )

        FitterTextField(
            value = state.password,
            onValueChange = actions.onPasswordChange,
            label = stringResource(R.string.auth_password),
            keyboardType = KeyboardType.Password,
            isError = state.passwordIssue != null,
            supportingText = state.passwordIssue?.let { stringResource(it.messageRes()) },
            obscured = !state.passwordVisible,
            imeAction = ImeAction.Done,
            onImeAction = actions.onSubmit,
            trailing = { PasswordToggle(state.passwordVisible, actions.onTogglePasswordVisible) }
        )
    }
}

@Composable
private fun PasswordToggle(visible: Boolean, onToggle: () -> Unit) {
    FitterIconButton(
        icon = when {
            visible -> FitterIcons.EyeOff
            else -> FitterIcons.Eye
        },
        contentDescription = stringResource(
            when {
                visible -> R.string.cd_password_hide
                else -> R.string.cd_password_show
            }
        ),
        onClick = onToggle
    )
}

@Composable
private fun FormError(error: RemoteError?) {
    AnimatedVisibility(
        visible = error != null,
        enter = fadeIn(FitterMotion.standardTween()) +
            expandVertically(FitterMotion.gentleSpring())
    ) {
        // Durante la animacion de salida el error ya es nulo, asi que se conserva el ultimo visto.
        val shown = remember(error) { error } ?: return@AnimatedVisibility

        FitterCard(modifier = Modifier.padding(bottom = FitterTheme.spacing.lg)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = shown.icon(),
                    contentDescription = null,
                    tint = FitterTheme.colors.syncError,
                    modifier = Modifier.size(FitterTheme.sizes.iconMedium)
                )
                Text(
                    text = shown.describe(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Panel de servidor, solo en depuracion.
 *
 * No aparece en reposo: la direccion a la que apunta la app es informacion de desarrollo y no parte
 * de la pantalla de acceso. Lo abre la pulsacion larga sobre el titulo, y en una compilacion de
 * produccion no existe.
 */
@Composable
private fun ServerSection(state: LoginUiState, actions: LoginActions) {
    if (!BuildConfig.DEBUG) return

    AnimatedVisibility(
        visible = state.advancedVisible,
        enter = fadeIn(FitterMotion.standardTween()) + expandVertically(
            FitterMotion.emphasizedTween(FitterMotion.Duration.ISLAND)
        )
    ) {
        Column(
            modifier = Modifier.padding(top = FitterTheme.spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)
        ) {
            SectionHeader(title = stringResource(R.string.auth_advanced_title))

            Text(
                text = when {
                    state.detectedBaseUrl.isBlank() ->
                        stringResource(R.string.auth_advanced_searching)
                    else ->
                        stringResource(R.string.auth_advanced_detected, state.detectedBaseUrl)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FitterTextField(
                value = state.manualBaseUrl,
                onValueChange = actions.onManualBaseUrlChange,
                label = stringResource(R.string.auth_advanced_url),
                keyboardType = KeyboardType.Uri,
                supportingText = stringResource(R.string.auth_advanced_hint)
            )
            FitterTextButton(
                text = stringResource(R.string.auth_advanced_save),
                onClick = actions.onSaveManualBaseUrl
            )
        }
    }
}

@StringRes
private fun LoginFieldIssue.messageRes(): Int = when (this) {
    LoginFieldIssue.REQUIRED -> R.string.auth_error_required
    LoginFieldIssue.MALFORMED_EMAIL -> R.string.auth_error_email_format
    LoginFieldIssue.TOO_SHORT -> R.string.auth_error_password_short
}

@Composable
private fun RemoteError.describe(): String = when (this) {
    RemoteError.Offline -> stringResource(R.string.auth_error_offline)
    RemoteError.ServerUnreachable -> stringResource(R.string.auth_error_unreachable)
    RemoteError.Timeout -> stringResource(R.string.auth_error_timeout)
    is RemoteError.Unauthorized -> message ?: stringResource(R.string.auth_error_credentials)
    is RemoteError.Validation -> message ?: stringResource(R.string.auth_error_unexpected)
    is RemoteError.Http -> stringResource(R.string.auth_error_server, code)
    is RemoteError.Unexpected -> stringResource(R.string.auth_error_unexpected)
}

@Composable
private fun RemoteError.icon(): ImageVector = when (this) {
    RemoteError.Offline, RemoteError.ServerUnreachable, RemoteError.Timeout -> FitterIcons.Offline
    else -> FitterIcons.Shield
}
