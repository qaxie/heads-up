package com.qaxie.headsup.ui.onboarding

import android.app.Application
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _navigateToHome = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateToHome: SharedFlow<Unit> = _navigateToHome.asSharedFlow()

    fun checkPermission() {
        val context = getApplication<Application>()
        val granted = NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)
        if (granted) {
            _navigateToHome.tryEmit(Unit)
        }
    }
}
