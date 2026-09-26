package com.acdev.fitter.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

private const val REMOTE_ID = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"

class RootTargetTest {

    @Test
    fun `sin preferencias cargadas se espera en el arranque`() {
        assertEquals(RootTarget.BOOT, resolveRootTarget(SessionState.SignedOut, null))
    }

    @Test
    fun `sesion desconocida se espera en el arranque aunque haya preferencias`() {
        assertEquals(RootTarget.BOOT, resolveRootTarget(SessionState.Unknown, completed()))
    }

    @Test
    fun `sin sesion se pide acceso aunque el asistente ya se hubiera completado`() {
        assertEquals(RootTarget.LOGIN, resolveRootTarget(SessionState.SignedOut, completed()))
        assertEquals(RootTarget.LOGIN, resolveRootTarget(SessionState.SignedOut, pending()))
    }

    @Test
    fun `con sesion y asistente pendiente toca el asistente`() {
        assertEquals(RootTarget.ONBOARDING, resolveRootTarget(signedIn(), pending()))
    }

    @Test
    fun `con sesion y asistente hecho se entra a la app`() {
        assertEquals(RootTarget.MAIN, resolveRootTarget(signedIn(), completed()))
    }

    /**
     * El asistente tambien elige tema y unidades, que el servidor no conoce, asi que reinstalar en
     * otro telefono debe volver a preguntarlos aunque la cuenta ya tenga el setup marcado.
     */
    @Test
    fun `la bandera del servidor no permite saltarse el asistente`() {
        val target = resolveRootTarget(signedIn(setupCompleted = true), pending())

        assertEquals(RootTarget.ONBOARDING, target)
    }

    private fun signedIn(setupCompleted: Boolean = false) =
        SessionState.SignedIn(remoteUserId = REMOTE_ID, setupCompleted = setupCompleted)

    private fun completed() = AppPreferences(onboardingCompleted = true)

    private fun pending() = AppPreferences(onboardingCompleted = false)
}
