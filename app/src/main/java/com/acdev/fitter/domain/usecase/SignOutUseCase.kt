package com.acdev.fitter.domain.usecase

import com.acdev.fitter.domain.repository.AuthRepository
import com.acdev.fitter.domain.repository.SessionRepository

/**
 * Cierra la sesion de este dispositivo.
 *
 * Avisar al servidor es una cortesia: si no hay red, la sesion local se cierra igual. Lo que no se
 * toca nunca es la base de datos: los entrenamientos son del usuario y siguen aqui, atados a su
 * cuenta, hasta que vuelva a entrar.
 */
class SignOutUseCase(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) {

    suspend operator fun invoke() {
        authRepository.signOut(allDevices = false)
        sessionRepository.clearSession()
    }
}
