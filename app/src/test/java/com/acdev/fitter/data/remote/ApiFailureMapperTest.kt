package com.acdev.fitter.data.remote

import com.acdev.fitter.domain.model.RemoteError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiFailureMapperTest {

    @Test
    fun `un 401 prefiere el mensaje del campo al generico`() {
        val envelope = ApiEnvelope<Unit>(
            success = false,
            message = "Las credenciales proporcionadas son incorrectas.",
            errors = mapOf("email" to listOf("Credenciales invalidas."))
        )

        val error = mapHttpFailure(401, envelope)

        assertEquals(RemoteError.Unauthorized("Credenciales invalidas."), error)
    }

    @Test
    fun `un 401 sin errores por campo cae en el mensaje general`() {
        val envelope = ApiEnvelope<Unit>(message = "No autenticado o token invalido/expirado.")

        val error = mapHttpFailure(401, envelope)

        assertEquals(RemoteError.Unauthorized("No autenticado o token invalido/expirado."), error)
    }

    @Test
    fun `un 422 conserva todos los errores por campo`() {
        val envelope = ApiEnvelope<Unit>(
            message = "Los datos proporcionados no son validos.",
            errors = mapOf(
                "email" to listOf("The email field is required."),
                "password" to listOf("The password field is required.")
            )
        )

        val error = mapHttpFailure(422, envelope)

        val validation = error as RemoteError.Validation
        assertEquals(2, validation.fields.size)
        assertEquals(listOf("The password field is required."), validation.fields["password"])
    }

    @Test
    fun `un 403 y un 500 conservan su codigo`() {
        assertEquals(
            RemoteError.Http(403, "No autorizado."),
            mapHttpFailure(403, ApiEnvelope<Unit>(message = "No autorizado."))
        )
        assertEquals(RemoteError.Http(500, null), mapHttpFailure(500, null))
    }

    @Test
    fun `un sobre ilegible no rompe el mapeo`() {
        val error = mapHttpFailure(401, null)

        assertTrue(error is RemoteError.Unauthorized)
        assertEquals(null, (error as RemoteError.Unauthorized).message)
    }
}
