package com.acdev.fitter.core.time

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Limite de la semana de entreno.
 *
 * Es el borde del que depende el bloqueo de la rutina activa: si el lunes se calcula mal, o se
 * bloquea un cambio que ya deberia estar permitido, o se permite uno a mitad de semana.
 */
class WeekBoundaryTest {

    private class FixedClock(
        private val date: LocalDate,
        private val zoneId: ZoneId = ZoneId.of("America/Mexico_City")
    ) : AppClock {
        override fun now(): Instant = date.atStartOfDay(zoneId).toInstant()
        override fun today(): LocalDate = date
        override fun zone(): ZoneId = zoneId
    }

    @Test
    fun `el lunes es su propio inicio de semana`() {
        val clock = FixedClock(LocalDate.of(2026, 8, 31))

        assertEquals(LocalDate.of(2026, 8, 31), clock.weekStart())
    }

    @Test
    fun `el domingo cierra la semana que empezo el lunes anterior`() {
        val clock = FixedClock(LocalDate.of(2026, 8, 30))

        assertEquals(LocalDate.of(2026, 8, 24), clock.weekStart())
    }

    @Test
    fun `el desbloqueo cae siempre el lunes siguiente`() {
        val clock = FixedClock(LocalDate.of(2026, 8, 26))

        assertEquals(LocalDate.of(2026, 8, 31), clock.nextWeekStart())
    }

    @Test
    fun `el inicio de semana en millis es la medianoche local de ese lunes`() {
        val zone = ZoneId.of("America/Mexico_City")
        val clock = FixedClock(LocalDate.of(2026, 8, 27), zone)

        val expected = LocalDate.of(2026, 8, 24).atStartOfDay(zone).toInstant().toEpochMilli()
        assertEquals(expected, clock.weekStartMillis())
    }

    @Test
    fun `un cambio de zona horaria mueve el inicio de semana`() {
        val date = LocalDate.of(2026, 8, 27)
        val mexico = FixedClock(date, ZoneId.of("America/Mexico_City")).weekStartMillis()
        val madrid = FixedClock(date, ZoneId.of("Europe/Madrid")).weekStartMillis()

        // Madrid amanece antes: su lunes empieza antes en la linea del tiempo absoluta.
        assertEquals(true, madrid < mexico)
    }
}
