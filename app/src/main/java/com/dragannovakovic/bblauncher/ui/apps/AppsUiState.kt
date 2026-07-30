package com.dragannovakovic.bblauncher.ui.apps

import androidx.annotation.StringRes
import com.dragannovakovic.bblauncher.data.apps.LaunchableApp

data class AppsUiState(
    val apps: List<LaunchableApp> = emptyList(),
    val recentApps: List<LaunchableApp> = emptyList(),
    val selectedProfile: AppProfile = AppProfile.Personal,
    val hasWorkProfile: Boolean = false,
    val query: String = "",
    val isLoading: Boolean = true,
    @param:StringRes val messageRes: Int? = null,
)

enum class AppProfile {
    Personal,
    Work,
}

internal fun effectiveAppProfile(
    requestedProfile: AppProfile,
    hasWorkProfile: Boolean,
): AppProfile =
    if (requestedProfile == AppProfile.Work && !hasWorkProfile) {
        AppProfile.Personal
    } else {
        requestedProfile
    }
