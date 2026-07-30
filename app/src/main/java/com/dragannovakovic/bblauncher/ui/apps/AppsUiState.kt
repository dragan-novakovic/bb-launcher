package com.dragannovakovic.bblauncher.ui.apps

import androidx.annotation.StringRes
import com.dragannovakovic.bblauncher.data.apps.LaunchableApp

data class AppsUiState(
    val apps: List<LaunchableApp> = emptyList(),
    val recentApps: List<LaunchableApp> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true,
    @param:StringRes val messageRes: Int? = null,
)
