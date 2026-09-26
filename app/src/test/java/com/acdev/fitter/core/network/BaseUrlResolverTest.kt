package com.acdev.fitter.core.network

import com.acdev.fitter.fake.FakeBaseUrlProbe
import com.acdev.fitter.fake.FakeBaseUrlStore
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private const val EMULATOR = "http://10.0.2.2:8000/"
private const val LAN = "http://192.168.1.50:8000/"
private const val PRODUCTION = "https://api.fitter.app/"

class BaseUrlResolverTest {

    @Test
    fun `la direccion manual gana a todo`() = runTest {
        val store = FakeBaseUrlStore(manual = PRODUCTION, lastGood = LAN)
        val probe = FakeBaseUrlProbe(reachable = setOf(EMULATOR))

        val resolver = resolver(store, probe, probeEnabled = true)

        assertEquals(PRODUCTION, resolver.resolve())
        assertEquals(0, probe.probeCount)
    }

    @Test
    fun `sin sondeo devuelve la primera candidata y no toca la red`() = runTest {
        val probe = FakeBaseUrlProbe(reachable = setOf(LAN))

        val resolver = resolver(FakeBaseUrlStore(), probe, probeEnabled = false)

        assertEquals(EMULATOR, resolver.resolve())
        assertEquals(0, probe.probeCount)
    }

    @Test
    fun `la recordada gana a las candidatas`() = runTest {
        val store = FakeBaseUrlStore(lastGood = LAN)
        val probe = FakeBaseUrlProbe(reachable = setOf(EMULATOR))

        val resolver = resolver(store, probe, probeEnabled = true)

        assertEquals(LAN, resolver.resolve())
        assertEquals(0, probe.probeCount)
    }

    @Test
    fun `elige la primera alcanzable y la recuerda`() = runTest {
        val store = FakeBaseUrlStore()
        val probe = FakeBaseUrlProbe(reachable = setOf(LAN, PRODUCTION))

        val resolver = resolver(store, probe, probeEnabled = true)

        assertEquals(LAN, resolver.resolve())
        assertEquals(LAN, store.lastGoodUrl())
    }

    @Test
    fun `si ninguna responde devuelve la primera y no la recuerda`() = runTest {
        val store = FakeBaseUrlStore()
        val probe = FakeBaseUrlProbe(reachable = emptySet())

        val resolver = resolver(store, probe, probeEnabled = true)

        assertEquals(EMULATOR, resolver.resolve())
        assertEquals("", store.lastGoodUrl())
    }

    @Test
    fun `dos llamadas concurrentes sondean una sola vez`() = runTest {
        val probe = FakeBaseUrlProbe(reachable = setOf(PRODUCTION))
        val resolver = resolver(FakeBaseUrlStore(), probe, probeEnabled = true)

        val first = async { resolver.resolve() }
        val second = async { resolver.resolve() }

        assertEquals(PRODUCTION, first.await())
        assertEquals(PRODUCTION, second.await())
        assertEquals(CANDIDATES.size, probe.probeCount)
    }

    @Test
    fun `invalidar olvida la recordada y fuerza un sondeo nuevo`() = runTest {
        val store = FakeBaseUrlStore()
        val probe = FakeBaseUrlProbe(reachable = setOf(EMULATOR))
        val resolver = resolver(store, probe, probeEnabled = true)

        resolver.resolve()
        resolver.invalidate()
        resolver.resolve()

        assertEquals("", store.manualUrl())
        assertEquals(2, probe.probeCount)
    }

    private fun resolver(
        store: FakeBaseUrlStore,
        probe: FakeBaseUrlProbe,
        probeEnabled: Boolean
    ) = DefaultBaseUrlResolver(CANDIDATES, store, probe, probeEnabled)

    private companion object {
        val CANDIDATES = listOf(EMULATOR, LAN, PRODUCTION)
    }
}
