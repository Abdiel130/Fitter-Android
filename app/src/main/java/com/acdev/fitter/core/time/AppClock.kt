package com.acdev.fitter.core.time

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Reloj de la app.
 *
 * Nadie llama a `Instant.now()` ni a `LocalDate.now()` directamente: se inyecta este contrato
 * para que las reglas de negocio con fechas (dia actual, semana en curso) se puedan probar sin
 * depender del reloj del dispositivo.
 */
interface AppClock {
    fun now(): Instant
    fun today(): LocalDate
    fun zone(): ZoneId
}

/** Implementacion real, la unica que se usa en produccion. */
class SystemAppClock(private val zoneId: ZoneId = ZoneId.systemDefault()) : AppClock {
    override fun now(): Instant = Instant.now()
    override fun today(): LocalDate = LocalDate.now(zoneId)
    override fun zone(): ZoneId = zoneId
}
