package com.acdev.fitter.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Los modelos del dashboard calculan progresos que la UI pinta sin revisar. Interesa sobre todo
 * el comportamiento en los bordes: meta a cero, consumo por encima del objetivo y cola vacia.
 */
class DashboardModelsTest {

    @Test
    fun `hidratacion sin meta no divide entre cero`() {
        assertEquals(0f, Hydration(intakeMl = 500, targetMl = 0).progress, TOLERANCE)
    }

    @Test
    fun `hidratacion por encima de la meta se queda en uno`() {
        assertEquals(1f, Hydration(intakeMl = 4000, targetMl = 3000).progress, TOLERANCE)
    }

    @Test
    fun `nutricion sin objetivo definido no muestra progreso`() {
        val summary = NutritionSummary(
            calories = 2000.0,
            protein = 150.0,
            carbs = 200.0,
            fat = 70.0,
            caloriesTarget = null,
            proteinTarget = null
        )
        assertEquals(0f, summary.caloriesProgress, TOLERANCE)
        assertEquals(0f, summary.proteinProgress, TOLERANCE)
    }

    @Test
    fun `la cola esta al dia solo si no hay nada pendiente ni fallido`() {
        assertTrue(SyncStatus().isUpToDate)
        assertFalse(SyncStatus(pendingCount = 1).isUpToDate)
        assertFalse(SyncStatus(failedCount = 1).isUpToDate)
        assertFalse(SyncStatus(conflictCount = 1).isUpToDate)
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}
