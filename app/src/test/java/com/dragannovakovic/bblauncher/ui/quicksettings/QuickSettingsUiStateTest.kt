package com.dragannovakovic.bblauncher.ui.quicksettings

import org.junit.Assert.assertEquals
import org.junit.Test

class QuickSettingsUiStateTest {
    @Test
    fun systemSettingToggleUsesBinaryValues() {
        assertEquals(0, toggledSystemSetting(1))
        assertEquals(1, toggledSystemSetting(0))
        assertEquals(1, toggledSystemSetting(5))
    }
}
