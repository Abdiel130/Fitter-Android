package com.acdev.fitter.data.auth

import com.acdev.fitter.core.network.NetworkClients
import com.acdev.fitter.data.remote.api.AuthApi
import com.acdev.fitter.data.remote.api.ProfileApi
import com.acdev.fitter.domain.model.RemoteError
import com.acdev.fitter.domain.model.RemoteResult
import java.time.Instant
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val JSON = "application/json"

/**
 * Comprueba la traduccion de las respuestas literales de la documentacion del backend.
 *
 * `MockWebServer` es JVM puro, asi que la prueba vive con las demas y no necesita dispositivo. Se
 * apunta Retrofit directamente al servidor de prueba: lo que se valida aqui es el contrato con el
 * servidor, no la resolucion de direccion.
 */
class AuthRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun start() {
        server = MockWebServer()
        server.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(NetworkClients.json.asConverterFactory(JSON.toMediaType()))
            .build()

        repository = AuthRepositoryImpl(
            authApi = retrofit.create(AuthApi::class.java),
            profileApi = retrofit.create(ProfileApi::class.java),
            deviceNameProvider = { "Pixel de prueba" },
            json = NetworkClients.json
        )
    }

    @After
    fun stop() {
        server.close()
    }

    @Test
    fun `un login correcto entrega usuario y tokens`() = runTest {
        server.enqueue(response(200, LOGIN_OK))

        val result = repository.signIn("usuario@ejemplo.com", "miPasswordSeguro123")

        val account = (result as RemoteResult.Success).value
        assertEquals("9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d", account.user.id)
        assertEquals("Juan Perez", account.user.name)
        assertEquals(false, account.user.setupCompleted)
        assertEquals("1|qW89d", account.tokens.accessToken)
        assertEquals(Instant.parse("2026-10-10T20:30:00Z"), account.tokens.accessExpiresAt)
        assertEquals(Instant.parse("2027-03-09T20:30:00Z"), account.tokens.refreshExpiresAt)
    }

    @Test
    fun `el cuerpo del login lleva el nombre del dispositivo`() = runTest {
        server.enqueue(response(200, LOGIN_OK))

        repository.signIn("usuario@ejemplo.com", "miPasswordSeguro123")

        val body = server.takeRequest().body?.utf8().orEmpty()
        assertTrue(body.contains("\"device_name\":\"Pixel de prueba\""))
    }

    @Test
    fun `un 401 se cuenta con el mensaje del campo`() = runTest {
        server.enqueue(response(401, LOGIN_REJECTED))

        val result = repository.signIn("usuario@ejemplo.com", "mala")

        val error = (result as RemoteResult.Failure).error
        assertEquals(
            RemoteError.Unauthorized("Credenciales invalidas. Verifica tu correo y contrasena."),
            error
        )
    }

    @Test
    fun `un 422 conserva los errores por campo`() = runTest {
        server.enqueue(response(422, LOGIN_INVALID))

        val result = repository.signIn("", "")

        val error = (result as RemoteResult.Failure).error as RemoteError.Validation
        assertEquals(listOf("The email field is required."), error.fields["email"])
        assertEquals(listOf("The password field is required."), error.fields["password"])
    }

    @Test
    fun `el perfil devuelve el estado del asistente`() = runTest {
        server.enqueue(response(200, SETUP_OK))

        val result = repository.pushSetupCompleted(true)

        assertEquals(RemoteResult.Success(true), result)
    }

    /**
     * El servidor escribe las fechas del perfil con desplazamiento explicito y las de los tokens con
     * zeta. Las dos formas tienen que entrar.
     */
    @Test
    fun `un perfil con desplazamiento explicito se lee igual`() = runTest {
        server.enqueue(response(200, SETUP_OFFSET_DATES))

        val result = repository.pushSetupCompleted(true)

        assertEquals(RemoteResult.Success(true), result)
    }

    @Test
    fun `un cuerpo que no es del contrato no revienta`() = runTest {
        server.enqueue(response(502, "<html>Bad gateway</html>"))

        val result = repository.me()

        assertEquals(RemoteError.Http(502, null), (result as RemoteResult.Failure).error)
    }

    /**
     * El servidor se apaga antes de la llamada, asi que el puerto queda cerrado. Es lo que ve la app
     * cuando el backend no esta levantado: hay red, pero nadie contesta.
     */
    @Test
    fun `un servidor apagado no se confunde con falta de red`() = runTest {
        server.close()

        val result = repository.me()

        assertEquals(RemoteError.ServerUnreachable, (result as RemoteResult.Failure).error)
    }

    private fun response(code: Int, body: String): MockResponse = MockResponse.Builder()
        .code(code)
        .setHeader("Content-Type", JSON)
        .body(body)
        .build()

    private companion object {
        const val LOGIN_OK = """
        {
          "success": true,
          "message": "Inicio de sesion exitoso.",
          "data": {
            "user": {
              "id": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
              "name": "Juan Perez",
              "email": "usuario@ejemplo.com",
              "current_routine": null,
              "active_sequence_index": 1,
              "is_setup_completed": false,
              "setup_completed_at": null,
              "created_at": "2026-09-06T12:00:00.000000Z"
            },
            "tokens": {
              "token_type": "Bearer",
              "access_token": "1|qW89d",
              "access_token_expires_at": "2026-10-10T20:30:00.000000Z",
              "refresh_token": "2|zK41a",
              "refresh_token_expires_at": "2027-03-09T20:30:00.000000Z"
            }
          }
        }
        """

        const val LOGIN_REJECTED = """
        {
          "success": false,
          "message": "Las credenciales proporcionadas son incorrectas.",
          "errors": {
            "email": ["Credenciales invalidas. Verifica tu correo y contrasena."]
          }
        }
        """

        const val LOGIN_INVALID = """
        {
          "success": false,
          "message": "Los datos proporcionados no son validos.",
          "errors": {
            "email": ["The email field is required."],
            "password": ["The password field is required."]
          }
        }
        """

        const val SETUP_OK = """
        {
          "success": true,
          "message": "Configuracion inicial marcada como completada.",
          "data": {
            "profile": {
              "id": "01a0deeb-534d-713b-9550-07403231cc17",
              "name": "Juan Perez",
              "email": "usuario@ejemplo.com",
              "current_routine": null,
              "active_sequence_index": 1,
              "is_setup_completed": true,
              "setup_completed_at": "2026-09-26T18:12:47+00:00",
              "created_at": "2026-09-26T18:12:39+00:00"
            }
          }
        }
        """

        const val SETUP_OFFSET_DATES = """
        {
          "success": true,
          "data": {
            "profile": {
              "id": "01a0deeb-534d-713b-9550-07403231cc17",
              "name": "Juan Perez",
              "email": "usuario@ejemplo.com",
              "active_sequence_index": 2,
              "is_setup_completed": true,
              "setup_completed_at": "2026-09-26T20:12:47+02:00",
              "created_at": "2026-09-26T20:12:39+02:00"
            }
          }
        }
        """
    }
}
