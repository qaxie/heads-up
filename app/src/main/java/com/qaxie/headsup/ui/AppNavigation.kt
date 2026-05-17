package com.qaxie.headsup.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.qaxie.headsup.ui.applist.AppListScreen
import com.qaxie.headsup.ui.home.HomeScreen
import com.qaxie.headsup.ui.onboarding.OnboardingScreen

private const val ROUTE_ONBOARDING = "onboarding"
private const val ROUTE_HOME = "home"
private const val ROUTE_APPLIST = "applist"

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val hasPermission = remember {
        NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)
    }
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = if (hasPermission) ROUTE_HOME else ROUTE_ONBOARDING
    ) {
        composable(ROUTE_ONBOARDING) { OnboardingScreen(navController) }
        composable(ROUTE_HOME) { HomeScreen(navController) }
        composable(ROUTE_APPLIST) { AppListScreen(navController) }
    }
}
