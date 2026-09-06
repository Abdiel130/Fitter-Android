package com.acdev.fitter.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Lectura de `exercises.iconKey`.
 *
 * La clave se guarda por nombre y puede venir de una version anterior o de un servidor que ya
 * conoce iconos que este cliente no tiene: leerla nunca debe dejar un ejercicio sin dibujo.
 */
class ExerciseIconTest {

    @Test
    fun `una clave conocida devuelve su icono`() {
        assertEquals(ExerciseIcon.BARBELL, ExerciseIcon.fromKey("BARBELL"))
    }

    @Test
    fun `una clave nula cae en el icono por defecto`() {
        assertEquals(ExerciseIcon.DEFAULT, ExerciseIcon.fromKey(null))
    }

    @Test
    fun `una clave desconocida cae en el icono por defecto`() {
        assertEquals(ExerciseIcon.DEFAULT, ExerciseIcon.fromKey("TRAPECIO_VOLADOR"))
    }

    @Test
    fun `la clave distingue mayusculas para no confundirse con otro catalogo`() {
        assertEquals(ExerciseIcon.DEFAULT, ExerciseIcon.fromKey("barbell"))
    }

    @Test
    fun `todos los iconos saben volver de su propia clave`() {
        ExerciseIcon.entries.forEach { icon ->
            assertEquals(icon, ExerciseIcon.fromKey(icon.name))
        }
    }
}
