package com.qaxie.headsup.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class SuppressedCountRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SuppressedCountRepository {

    private val COUNT_DATE = stringPreferencesKey("suppressed_count_date")
    private val COUNT_VALUE = intPreferencesKey("suppressed_count_value")

    private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    override fun getTodayCount(): Flow<Int> =
        dataStore.data.map { prefs ->
            if (prefs[COUNT_DATE] == today()) prefs[COUNT_VALUE] ?: 0 else 0
        }

    override suspend fun increment() {
        dataStore.edit { prefs ->
            val today = today()
            if (prefs[COUNT_DATE] != today) {
                prefs[COUNT_DATE] = today
                prefs[COUNT_VALUE] = 1
            } else {
                prefs[COUNT_VALUE] = (prefs[COUNT_VALUE] ?: 0) + 1
            }
        }
    }

    override suspend fun resetIfNewDay() {
        dataStore.edit { prefs ->
            if (prefs[COUNT_DATE] != today()) {
                prefs[COUNT_DATE] = today()
                prefs[COUNT_VALUE] = 0
            }
        }
    }
}
