package com.acdev.fitter.core.network

import org.junit.Assert.assertEquals
import org.junit.Test

class BaseUrlCandidatesTest {

    @Test
    fun `garantiza barra final y conserva el orden`() {
        val parsed = BaseUrlCandidates.from("http://10.0.2.2:8000,https://api.fitter.app/")

        assertEquals(listOf("http://10.0.2.2:8000/", "https://api.fitter.app/"), parsed)
    }

    @Test
    fun `recorta espacios y descarta entradas vacias`() {
        val parsed = BaseUrlCandidates.from(" http://a/ , ,, http://b ")

        assertEquals(listOf("http://a/", "http://b/"), parsed)
    }

    @Test
    fun `una cadena vacia no produce candidatas`() {
        assertEquals(emptyList<String>(), BaseUrlCandidates.from("   "))
    }
}
