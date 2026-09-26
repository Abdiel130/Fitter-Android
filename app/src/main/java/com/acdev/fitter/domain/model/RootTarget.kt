package com.acdev.fitter.domain.model

/** Lo que pinta la raiz de la interfaz. Anadir un estado obliga al compilador a cubrirlo. */
enum class RootTarget { BOOT, LOGIN, ONBOARDING, MAIN }

/**
 * Decide la pantalla raiz a partir de sesion y preferencias.
 *
 * Mira la bandera local del asistente y no la del servidor a proposito: el asistente tambien elige
 * tema y unidades, que el servidor no conoce, asi que reinstalar en otro telefono debe volver a
 * preguntarlos aunque la cuenta ya tenga el setup marcado. La bandera remota sirve para
 * reconciliar y reenviar el PATCH, no para saltarse pasos.
 */
fun resolveRootTarget(session: SessionState, preferences: AppPreferences?): RootTarget = when {
    preferences == null || session is SessionState.Unknown -> RootTarget.BOOT
    session is SessionState.SignedOut -> RootTarget.LOGIN
    preferences.onboardingCompleted -> RootTarget.MAIN
    else -> RootTarget.ONBOARDING
}
