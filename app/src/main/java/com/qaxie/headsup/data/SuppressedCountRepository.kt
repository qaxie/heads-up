package com.qaxie.headsup.data

import kotlinx.coroutines.flow.Flow

interface SuppressedCountRepository {
    fun getTodayCount(): Flow<Int>
    suspend fun increment()
    suspend fun resetIfNewDay()
}
