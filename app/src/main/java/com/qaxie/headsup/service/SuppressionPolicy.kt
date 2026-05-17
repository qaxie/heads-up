package com.qaxie.headsup.service

import android.app.NotificationManager
import android.service.notification.NotificationListenerService
import android.service.notification.NotificationListenerService.RankingMap
import com.qaxie.headsup.data.AppPreferenceRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuppressionPolicy @Inject constructor(
    private val appPreferenceRepository: AppPreferenceRepository
) {
    suspend fun shouldSuppress(packageName: String, rankingMap: RankingMap, key: String): Boolean {
        if (!appPreferenceRepository.isSuppressionEnabled()) return false
        if (appPreferenceRepository.isAllowed(packageName)) return false

        val ranking = NotificationListenerService.Ranking()
        if (!rankingMap.getRanking(key, ranking)) return false

        return ranking.importance >= NotificationManager.IMPORTANCE_HIGH
    }
}
