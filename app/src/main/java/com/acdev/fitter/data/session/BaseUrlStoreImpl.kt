package com.acdev.fitter.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.acdev.fitter.core.network.BaseUrlStore
import kotlinx.coroutines.flow.first

class BaseUrlStoreImpl(
    private val dataStore: DataStore<Preferences>
) : BaseUrlStore {

    override suspend fun manualUrl(): String =
        dataStore.data.first()[SessionKeys.baseUrlManual].orEmpty()

    override suspend fun lastGoodUrl(): String =
        dataStore.data.first()[SessionKeys.baseUrlLastGood].orEmpty()

    /** Un valor en blanco borra la direccion manual y devuelve el control al sondeo. */
    override suspend fun saveManual(url: String?) {
        val normalized = url?.trim().orEmpty()
        dataStore.edit { stored ->
            when {
                normalized.isEmpty() -> stored.remove(SessionKeys.baseUrlManual)
                else -> stored[SessionKeys.baseUrlManual] = normalized
            }
        }
    }

    override suspend fun saveLastGood(url: String) {
        dataStore.edit { it[SessionKeys.baseUrlLastGood] = url }
    }

    override suspend fun clearLastGood() {
        dataStore.edit { it.remove(SessionKeys.baseUrlLastGood) }
    }
}
