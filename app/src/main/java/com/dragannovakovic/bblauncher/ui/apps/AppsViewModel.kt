package com.dragannovakovic.bblauncher.ui.apps

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.pm.LauncherApps
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dragannovakovic.bblauncher.R
import com.dragannovakovic.bblauncher.data.apps.AppCatalogRepository
import com.dragannovakovic.bblauncher.data.apps.LaunchableApp
import com.dragannovakovic.bblauncher.data.apps.RecentAppsRepository
import java.io.IOException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppCatalogRepository(application)
    private val recentAppsRepository = RecentAppsRepository(application)
    private val catalog = MutableStateFlow<List<LaunchableApp>>(emptyList())
    private val query = MutableStateFlow("")
    private val isLoading = MutableStateFlow(true)
    private val messageRes = MutableStateFlow<Int?>(null)
    private var refreshJob: Job? = null
    private val packageCallback: LauncherApps.Callback =
        repository.registerPackageChangeCallback(::refresh)

    private val catalogWithRecents = combine(
        catalog,
        recentAppsRepository.recentComponentNames,
    ) { apps, recentComponentNames ->
        val appsByComponent = apps.associateBy { app ->
            app.componentName.flattenToString()
        }
        apps to recentComponentNames.mapNotNull(appsByComponent::get)
    }

    val uiState = combine(
        catalogWithRecents,
        query,
        isLoading,
        messageRes,
    ) { (apps, recentApps), currentQuery, loading, message ->
        AppsUiState(
            apps = apps.filter { app ->
                matchesAppQuery(
                    label = app.label,
                    packageName = app.componentName.packageName,
                    query = currentQuery,
                )
            },
            recentApps = recentApps,
            query = currentQuery,
            isLoading = loading,
            messageRes = message,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = AppsUiState(),
    )

    init {
        refresh()
    }

    fun updateQuery(value: String) {
        query.value = value
        messageRes.value = null
    }

    fun clearQuery() {
        updateQuery("")
    }

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            isLoading.value = true
            messageRes.value = null

            try {
                catalog.value = repository.loadApps()
            } catch (_: SecurityException) {
                messageRes.value = R.string.apps_load_failed
            }

            isLoading.value = false
        }
    }

    fun launchApp(app: LaunchableApp) {
        messageRes.value = null
        try {
            repository.launch(app)
            viewModelScope.launch {
                try {
                    recentAppsRepository.record(app.componentName.flattenToString())
                } catch (_: IOException) {
                    messageRes.value = R.string.recent_apps_save_failed
                }
            }
        } catch (_: ActivityNotFoundException) {
            messageRes.value = R.string.app_launch_failed
        } catch (_: SecurityException) {
            messageRes.value = R.string.app_launch_failed
        }
    }

    override fun onCleared() {
        repository.unregisterPackageChangeCallback(packageCallback)
        super.onCleared()
    }
}
