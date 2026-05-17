package com.qaxie.headsup.ui.home

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qaxie.headsup.data.AppPreferenceRepository
import com.qaxie.headsup.data.SuppressedCountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appPreferenceRepository: AppPreferenceRepository,
    private val suppressedCountRepository: SuppressedCountRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val suppressionEnabled: StateFlow<Boolean> = appPreferenceRepository
        .getSuppressionEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val todayCount: StateFlow<Int> = suppressedCountRepository
        .getTodayCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _navigateToOnboarding = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateToOnboarding: SharedFlow<Unit> = _navigateToOnboarding.asSharedFlow()

    init {
        viewModelScope.launch { suppressedCountRepository.resetIfNewDay() }
    }

    fun onToggleChanged(enabled: Boolean) {
        viewModelScope.launch { appPreferenceRepository.setSuppressionEnabled(enabled) }
    }

    fun checkPermission() {
        val granted = NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)
        if (!granted) _navigateToOnboarding.tryEmit(Unit)
    }
}
