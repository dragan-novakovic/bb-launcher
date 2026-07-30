package com.dragannovakovic.bblauncher.ui.apps

import org.junit.Assert.assertEquals
import org.junit.Test

class AppProfileTest {
    @Test
    fun workFallsBackToPersonalWhenNoWorkProfileExists() {
        assertEquals(
            AppProfile.Personal,
            effectiveAppProfile(
                requestedProfile = AppProfile.Work,
                hasWorkProfile = false,
            ),
        )
    }

    @Test
    fun workRemainsSelectedWhenProfileExists() {
        assertEquals(
            AppProfile.Work,
            effectiveAppProfile(
                requestedProfile = AppProfile.Work,
                hasWorkProfile = true,
            ),
        )
    }
}
