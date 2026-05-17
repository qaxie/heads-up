package com.qaxie.headsup.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstalledAppSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @Suppress("DEPRECATION")
    fun getInstalledApps(): List<AppInfo> {
        val pm = context.packageManager

        val launcherPackages = pm.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0
        ).map { it.activityInfo.packageName }.toSet()

        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.packageName in launcherPackages }
            .map { info ->
                AppInfo(
                    packageName = info.packageName,
                    label = pm.getApplicationLabel(info).toString(),
                    icon = pm.getApplicationIcon(info),
                    isSystem = info.flags and ApplicationInfo.FLAG_SYSTEM != 0
                        && info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 0
                )
            }
    }
}
