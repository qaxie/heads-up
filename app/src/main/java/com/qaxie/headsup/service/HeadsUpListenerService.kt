package com.qaxie.headsup.service

import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.qaxie.headsup.data.SuppressedCountRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface HeadsUpListenerServiceEntryPoint {
    fun suppressionPolicy(): SuppressionPolicy
    fun suppressedCountRepository(): SuppressedCountRepository
}

class HeadsUpListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val entryPoint by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            HeadsUpListenerServiceEntryPoint::class.java
        )
    }

    private val suppressionPolicy by lazy { entryPoint.suppressionPolicy() }
    private val suppressedCountRepository by lazy { entryPoint.suppressedCountRepository() }

    override fun onNotificationPosted(sbn: StatusBarNotification, rankingMap: RankingMap) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        serviceScope.launch {
            if (suppressionPolicy.shouldSuppress(sbn.packageName, rankingMap, sbn.key)) {
                suppressHeadsUp(sbn)
                suppressedCountRepository.increment()
            }
        }
    }

    // android.service.notification.Adjustment is @hide in the public SDK stubs, so we use
    // reflection to create the object and invoke adjustNotification at runtime.
    private fun suppressHeadsUp(sbn: StatusBarNotification) {
        try {
            val adjustmentClass = Class.forName("android.service.notification.Adjustment")
            val keyImportance = adjustmentClass.getField("KEY_IMPORTANCE").get(null) as String
            val extras = Bundle().apply {
                putInt(keyImportance, NotificationManager.IMPORTANCE_DEFAULT)
            }
            val ctor = adjustmentClass.getDeclaredConstructor(
                String::class.java,
                String::class.java,
                Bundle::class.java,
                CharSequence::class.java,
                android.os.UserHandle::class.java
            )
            val adjustment = ctor.newInstance(sbn.packageName, sbn.key, extras, null, sbn.user)
            NotificationListenerService::class.java
                .getMethod("adjustNotification", adjustmentClass)
                .invoke(this, adjustment)
        } catch (_: Exception) { }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
