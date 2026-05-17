package com.qaxie.headsup.ui.applist

import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qaxie.headsup.data.AppInfo
import com.qaxie.headsup.data.AppPreferenceRepository
import com.qaxie.headsup.data.InstalledAppSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppListItem(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    val isAllowed: Boolean,
    val isSystem: Boolean
)

sealed interface AppListState {
    object Loading : AppListState
    data class Populated(
        val userApps: List<AppListItem>,
        val systemApps: List<AppListItem>
    ) : AppListState
}

@HiltViewModel
class AppListViewModel @Inject constructor(
    private val installedAppSource: InstalledAppSource,
    private val appPreferenceRepository: AppPreferenceRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    private val installedApps: Flow<List<AppInfo>> = flow {
        emit(installedAppSource.getInstalledApps())
    }.flowOn(Dispatchers.IO)

    val state: StateFlow<AppListState> = combine(
        installedApps,
        appPreferenceRepository.getAllPreferences(),
        searchQuery
    ) { apps, preferences, query ->
        val suppressedPackages = preferences.map { it.packageName }.toSet()
        val items = apps.map { app ->
            AppListItem(
                packageName = app.packageName,
                label = app.label,
                icon = app.icon,
                isAllowed = !suppressedPackages.contains(app.packageName),
                isSystem = app.isSystem
            )
        }
        val filtered = if (query.isBlank()) items
                       else items.filter { it.label.contains(query, ignoreCase = true) }
        AppListState.Populated(
            userApps = filtered.sortedBy { it.label.lowercase() },
            systemApps = emptyList()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppListState.Loading)

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onToggle(packageName: String, allowed: Boolean) {
        viewModelScope.launch {
            appPreferenceRepository.setAllowed(packageName, allowed)
        }
    }
}
