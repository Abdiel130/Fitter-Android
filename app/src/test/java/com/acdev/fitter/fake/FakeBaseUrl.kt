package com.acdev.fitter.fake

import com.acdev.fitter.core.network.BaseUrlProbe
import com.acdev.fitter.core.network.BaseUrlStore

class FakeBaseUrlProbe(private val reachable: Set<String>) : BaseUrlProbe {

    var probeCount: Int = 0
        private set

    override suspend fun isReachable(baseUrl: String): Boolean {
        probeCount++
        return baseUrl in reachable
    }
}

class FakeBaseUrlStore(
    private var manual: String = "",
    private var lastGood: String = ""
) : BaseUrlStore {

    override suspend fun manualUrl(): String = manual

    override suspend fun lastGoodUrl(): String = lastGood

    override suspend fun saveManual(url: String?) {
        manual = url?.trim().orEmpty()
    }

    override suspend fun saveLastGood(url: String) {
        lastGood = url
    }

    override suspend fun clearLastGood() {
        lastGood = ""
    }
}
