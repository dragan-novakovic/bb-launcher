package com.dragannovakovic.bblauncher.ui.apps

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSearchTest {
    @Test
    fun emptyQueryMatchesEveryApp() {
        assertTrue(matchesAppQuery("Camera", "com.android.camera", "  "))
    }

    @Test
    fun queryMatchesLabelIgnoringCase() {
        assertTrue(matchesAppQuery("BlackBerry Hub", "com.example.hub", "berry"))
    }

    @Test
    fun queryMatchesPackageName() {
        assertTrue(matchesAppQuery("Settings", "com.android.settings", "android.set"))
    }

    @Test
    fun unrelatedQueryDoesNotMatch() {
        assertFalse(matchesAppQuery("Calculator", "com.android.calculator", "camera"))
    }
}
