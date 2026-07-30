package com.dragannovakovic.bblauncher.ui.quicksettings

import androidx.annotation.StringRes

data class QuickSettingsUiState(
    val isTorchAvailable: Boolean = false,
    val isTorchEnabled: Boolean = false,
    val isAutoRotateEnabled: Boolean = true,
    val canWriteSystemSettings: Boolean = false,
    @param:StringRes val messageRes: Int? = null,
)

internal fun toggledSystemSetting(currentValue: Int): Int =
    if (currentValue == 1) 0 else 1
