package de.exit.ridetrackr.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ride_trackr_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val KEY_LAST_PARK_ID = stringPreferencesKey("last_park_id")
        val KEY_LAST_PARK_NAME = stringPreferencesKey("last_park_name")
        val KEY_ALERTS_ENABLED = booleanPreferencesKey("alerts_enabled")
        val KEY_ALERT_THRESHOLD = intPreferencesKey("alert_threshold")
        val KEY_COLOR_ACCENT = stringPreferencesKey("color_accent")
        val KEY_LANGUAGE = stringPreferencesKey("language")
    }

    val lastParkId: Flow<String?> = dataStore.data.map { it[KEY_LAST_PARK_ID] }
    val lastParkName: Flow<String?> = dataStore.data.map { it[KEY_LAST_PARK_NAME] }
    val alertsEnabled: Flow<Boolean> = dataStore.data.map { it[KEY_ALERTS_ENABLED] ?: false }
    val alertThresholdMinutes: Flow<Int> = dataStore.data.map { it[KEY_ALERT_THRESHOLD] ?: 30 }
    val colorAccent: Flow<String> = dataStore.data.map { it[KEY_COLOR_ACCENT] ?: "TEAL" }
    val language: Flow<String> = dataStore.data.map { it[KEY_LANGUAGE] ?: "DE" }

    suspend fun saveSelectedPark(parkId: String, parkName: String) {
        dataStore.edit { prefs ->
            prefs[KEY_LAST_PARK_ID] = parkId
            prefs[KEY_LAST_PARK_NAME] = parkName
        }
    }

    suspend fun setAlertsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_ALERTS_ENABLED] = enabled
        }
    }

    suspend fun setAlertThresholdMinutes(minutes: Int) {
        dataStore.edit { prefs ->
            prefs[KEY_ALERT_THRESHOLD] = minutes
        }
    }

    suspend fun setColorAccent(accent: String) {
        dataStore.edit { prefs ->
            prefs[KEY_COLOR_ACCENT] = accent
        }
    }

    suspend fun setLanguage(language: String) {
        dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = language
        }
    }
}
