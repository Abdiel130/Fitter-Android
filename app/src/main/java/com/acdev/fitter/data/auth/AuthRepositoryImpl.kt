package com.acdev.fitter.data.auth

import com.acdev.fitter.core.network.DeviceNameProvider
import com.acdev.fitter.data.remote.api.AuthApi
import com.acdev.fitter.data.remote.api.ProfileApi
import com.acdev.fitter.data.remote.apiCall
import com.acdev.fitter.data.remote.dto.LoginDataDto
import com.acdev.fitter.data.remote.dto.LoginRequestDto
import com.acdev.fitter.data.remote.dto.LogoutRequestDto
import com.acdev.fitter.data.remote.dto.SetupRequestDto
import com.acdev.fitter.data.remote.mapper.toDomain
import com.acdev.fitter.data.remote.mapper.toDomainOrNull
import com.acdev.fitter.domain.model.RemoteError
import com.acdev.fitter.domain.model.RemoteResult
import com.acdev.fitter.domain.model.RemoteUser
import com.acdev.fitter.domain.model.SignedInAccount
import com.acdev.fitter.domain.model.map
import com.acdev.fitter.domain.repository.AuthRepository
import kotlinx.serialization.json.Json

private const val UNREADABLE_TOKENS = "Los tokens llegaron con fechas ilegibles"

/**
 * Autenticacion contra el servidor.
 *
 * Solo habla: no guarda nada. Quien persiste la sesion es `SessionRepository` y quien orquesta a
 * los dos es `SignInUseCase`. Asi esta clase se puede probar contra un servidor de prueba sin
 * arrastrar DataStore ni el Keystore.
 */
class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val profileApi: ProfileApi,
    private val deviceNameProvider: DeviceNameProvider,
    private val json: Json
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): RemoteResult<SignedInAccount> {
        val body = LoginRequestDto(
            email = email,
            password = password,
            deviceName = deviceNameProvider.deviceName()
        )

        return when (val result = apiCall(json) { authApi.login(body) }) {
            is RemoteResult.Failure -> result
            is RemoteResult.Success -> result.value.toAccount()
        }
    }

    override suspend fun signOut(allDevices: Boolean): RemoteResult<Unit> =
        apiCall(json) { authApi.logout(LogoutRequestDto(allDevices)) }

    override suspend fun me(): RemoteResult<RemoteUser> =
        apiCall(json) { authApi.me() }.map { it.user.toDomain() }

    override suspend fun pushSetupCompleted(completed: Boolean): RemoteResult<Boolean> =
        apiCall(json) { profileApi.setup(SetupRequestDto(completed)) }
            .map { it.profile.isSetupCompleted }

    /**
     * Sin fechas de vencimiento fiables no se puede decidir cuando renovar, asi que se prefiere
     * fallar a entrar con una sesion cuyo final no se conoce.
     */
    private fun LoginDataDto.toAccount(): RemoteResult<SignedInAccount> {
        val domainTokens = tokens.toDomainOrNull()
            ?: return RemoteResult.Failure(RemoteError.Unexpected(UNREADABLE_TOKENS))
        return RemoteResult.Success(SignedInAccount(user.toDomain(), domainTokens))
    }
}
