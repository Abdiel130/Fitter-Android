package com.acdev.fitter.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * La conversion de unidades es la unica aritmetica que toca todos los registros de peso de la
 * app, asi que se prueba de forma explicita.
 */
class WeightUnitTest {

    @Test
    fun `kilogramos no cambia el valor`() {
        assertEquals(82.5, WeightUnit.KG.fromKg(82.5), TOLERANCE)
        assertEquals(82.5, WeightUnit.KG.toKg(82.5), TOLERANCE)
    }

    @Test
    fun `libras convierte en ambos sentidos`() {
        assertEquals(220.462, WeightUnit.LBS.fromKg(100.0), TOLERANCE)
        assertEquals(100.0, WeightUnit.LBS.toKg(220.462), TOLERANCE)
    }

    @Test
    fun `ida y vuelta conserva el valor introducido`() {
        val entered = 185.0
        val roundTrip = WeightUnit.LBS.fromKg(WeightUnit.LBS.toKg(entered))
        assertEquals(entered, roundTrip, TOLERANCE)
    }

    private companion object {
        const val TOLERANCE = 0.001
    }
}
