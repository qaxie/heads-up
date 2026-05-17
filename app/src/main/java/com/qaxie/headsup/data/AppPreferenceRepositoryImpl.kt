package com.qaxie.headsup.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppPreferenceRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AppPreferenceRepository {

    private val SUPPRESSED_PACKAGES = stringSetPreferencesKey("suppressed_packages")
    private val SUPPRESSION_ENABLED = booleanPreferencesKey("suppression_enabled")

    // Returns only explicitly suppressed apps; absence from this list means allowed (default ON).
    override fun getAllPreferences(): Flow<List<AppPreference>> =
        dataStore.data.map { prefs ->
            (prefs[SUPPRESSED_PACKAGES] ?: emptySet()).map { AppPreference(it, false) }
        }

    override suspend fun setAllowed(packageName: String, allowed: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[SUPPRESSED_PACKAGES]?.toMutableSet() ?: mutableSetOf()
            if (!allowed) current.add(packageName) else current.remove(packageName)
            prefs[SUPPRESSED_PACKAGES] = current
        }
    }

    override suspend fun isAllowed(packageName: String): Boolean =
        dataStore.data.map { prefs ->
            val suppressed = prefs[SUPPRESSED_PACKAGES] ?: emptySet()
            !suppressed.contains(packageName)
        }.first()

    override fun getSuppressionEnabled(): Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[SUPPRESSION_ENABLED] ?: true }

    override suspend fun setSuppressionEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[SUPPRESSION_ENABLED] = enabled }
    }

    override suspend fun isSuppressionEnabled(): Boolean =
        dataStore.data.map { prefs -> prefs[SUPPRESSION_ENABLED] ?: true }.first()
}
