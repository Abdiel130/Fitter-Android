package com.acdev.fitter.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoginFormTest {

    @Test
    fun `un correo vacio o en blanco es obligatorio`() {
        assertEquals(LoginFieldIssue.REQUIRED, LoginForm.validateEmail(""))
        assertEquals(LoginFieldIssue.REQUIRED, LoginForm.validateEmail("   "))
    }

    @Test
    fun `un correo sin dominio completo esta mal formado`() {
        assertEquals(LoginFieldIssue.MALFORMED_EMAIL, LoginForm.validateEmail("atleta"))
        assertEquals(LoginFieldIssue.MALFORMED_EMAIL, LoginForm.validateEmail("atleta@local"))
        assertEquals(LoginFieldIssue.MALFORMED_EMAIL, LoginForm.validateEmail("a b@fitter.app"))
    }

    @Test
    fun `los espacios de los bordes no invalidan un correo correcto`() {
        assertNull(LoginForm.validateEmail("  atleta@fitter.app  "))
    }

    @Test
    fun `una contrasena vacia es obligatoria y una corta no vale`() {
        assertEquals(LoginFieldIssue.REQUIRED, LoginForm.validatePassword(""))
        assertEquals(LoginFieldIssue.TOO_SHORT, LoginForm.validatePassword("corta1"))
    }

    /** Los espacios pueden ser parte de la contrasena, asi que no se recortan ni se cuentan aparte. */
    @Test
    fun `una contrasena de espacios suficientemente larga se acepta`() {
        assertNull(LoginForm.validatePassword("        "))
        assertNull(LoginForm.validatePassword("miPasswordSeguro123"))
    }
}
