package com.dragannovakovic.bblauncher.ui.apps

import android.app.Application
import android.content.ActivityNotFoundException
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
    private val selectedProfile = MutableStateFlow(AppProfile.Personal)
    private val isLoading = MutableStateFlow(true)
    private val messageRes = MutableStateFlow<Int?>(null)
    private var refreshJob: Job? = null
    private val packageCallback =
        repository.registerPackageChangeCallback(::refresh)

    private val catalogWithRecents = combine(
        catalog,
        recentAppsRepository.recentComponentNames,
    ) { apps, recentComponentNames ->
        val appsById = apps.associateBy(LaunchableApp::id)
        apps to recentComponentNames.mapNotNull(appsById::get)
    }

    private val profiledCatalog = combine(
        catalogWithRecents,
        selectedProfile,
    ) { (apps, recentApps), requestedProfile ->
        val hasWorkProfile = apps.any(LaunchableApp::isWorkProfile)
        val effectiveProfile = effectiveAppProfile(requestedProfile, hasWorkProfile)
        ProfiledCatalog(
            apps = apps.filter { app ->
                when (effectiveProfile) {
                    AppProfile.Personal -> !app.isWorkProfile
                    AppProfile.Work -> app.isWorkProfile
                }
            },
            recentApps = recentApps,
            selectedProfile = effectiveProfile,
            hasWorkProfile = hasWorkProfile,
        )
    }

    val uiState = combine(
        profiledCatalog,
        query,
        isLoading,
        messageRes,
    ) { profiled, currentQuery, loading, message ->
        AppsUiState(
            apps = profiled.apps.filter { app ->
                matchesAppQuery(
                    label = app.label,
                    packageName = app.componentName.packageName,
                    query = currentQuery,
                )
            },
            recentApps = profiled.recentApps,
            selectedProfile = profiled.selectedProfile,
            hasWorkProfile = profiled.hasWorkProfile,
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

    fun selectProfile(profile: AppProfile) {
        selectedProfile.value = profile
        clearQuery()
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

    private data class ProfiledCatalog(
        val apps: List<LaunchableApp>,
        val recentApps: List<LaunchableApp>,
        val selectedProfile: AppProfile,
        val hasWorkProfile: Boolean,
    )

    fun launchApp(app: LaunchableApp) {
        messageRes.value = null
        try {
            repository.launch(app)
            viewModelScope.launch {
                try {
                    recentAppsRepository.record(app.id)
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
