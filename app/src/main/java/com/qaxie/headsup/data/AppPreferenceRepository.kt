package com.qaxie.headsup.data

import kotlinx.coroutines.flow.Flow

interface AppPreferenceRepository {
    fun getAllPreferences(): Flow<List<AppPreference>>
    suspend fun setAllowed(packageName: String, allowed: Boolean)
    suspend fun isAllowed(packageName: String): Boolean
    fun getSuppressionEnabled(): Flow<Boolean>
    suspend fun setSuppressionEnabled(enabled: Boolean)
    suspend fun isSuppressionEnabled(): Boolean
}
