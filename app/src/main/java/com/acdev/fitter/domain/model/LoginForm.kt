package com.acdev.fitter.domain.model

private const val MIN_PASSWORD_LENGTH = 8
private val EMAIL_PATTERN = Regex("""^[^@\s]+@[^@\s.]+(\.[^@\s.]+)+$""")

/** Lo que puede estar mal en un campo del formulario. La interfaz decide como se cuenta. */
enum class LoginFieldIssue { REQUIRED, MALFORMED_EMAIL, TOO_SHORT }

/**
 * Validacion del formulario de acceso.
 *
 * Vive en el dominio y sin dependencias de Android para poder probarse en la JVM. Se valida antes
 * de pedir nada al servidor para no gastar una peticion en un correo mal escrito.
 */
object LoginForm {

    fun validateEmail(value: String): LoginFieldIssue? {
        val trimmed = value.trim()
        return when {
            trimmed.isEmpty() -> LoginFieldIssue.REQUIRED
            !EMAIL_PATTERN.matches(trimmed) -> LoginFieldIssue.MALFORMED_EMAIL
            else -> null
        }
    }

    /** La contrasena no se recorta: los espacios pueden ser parte de ella. */
    fun validatePassword(value: String): LoginFieldIssue? = when {
        value.isEmpty() -> LoginFieldIssue.REQUIRED
        value.length < MIN_PASSWORD_LENGTH -> LoginFieldIssue.TOO_SHORT
        else -> null
    }
}
