package com.acdev.fitter.data.session

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Claves de sesion y de red dentro del DataStore de la app.
 *
 * Viven en el mismo archivo que las preferencias a proposito: son datos del dispositivo con el
 * mismo ciclo de vida, y abrir un segundo DataStore solo anadiria un descriptor mas.
 * `AppPreferences` no las mapea, asi que no se mezclan con los ajustes visibles.
 */
internal object SessionKeys {
    val accessSealed = stringPreferencesKey("auth_access_sealed")
    val accessExpiresAt = longPreferencesKey("auth_access_expires_at")
    val refreshSealed = stringPreferencesKey("auth_refresh_sealed")
    val refreshExpiresAt = longPreferencesKey("auth_refresh_expires_at")
    val refreshKeyAlias = stringPreferencesKey("auth_refresh_key_alias")
    val remoteUserId = stringPreferencesKey("auth_remote_user_id")
    val setupCompleted = booleanPreferencesKey("auth_setup_completed")
    val baseUrlLastGood = stringPreferencesKey("api_base_url_last_good")
    val baseUrlManual = stringPreferencesKey("api_base_url_manual")
}
