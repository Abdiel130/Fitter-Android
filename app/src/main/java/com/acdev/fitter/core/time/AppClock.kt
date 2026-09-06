package com.acdev.fitter.core.time

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

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

/**
 * Lunes de la semana en curso.
 *
 * La semana de entreno empieza el lunes en todo el producto: el volumen semanal del dashboard y
 * el bloqueo de la rutina activa comparten este limite, asi que vive en un solo sitio.
 */
fun AppClock.weekStart(): LocalDate =
    today().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

/** Lunes siguiente: el dia en que se libera el bloqueo de la rutina activa. */
fun AppClock.nextWeekStart(): LocalDate = weekStart().plusWeeks(1)

/** Instante en que empezo la semana en curso, en epoch millis, para comparar contra la base. */
fun AppClock.weekStartMillis(): Long =
    weekStart().atStartOfDay(zone()).toInstant().toEpochMilli()
