package com.dragannovakovic.bblauncher.data.system

import org.junit.Assert.assertEquals
import org.junit.Test

class SystemStatusTest {
    @Test
    fun batteryPercentageUsesReportedScale() {
        assertEquals(50, calculateBatteryPercentage(level = 25, scale = 50))
    }

    @Test
    fun invalidBatteryReadingReturnsZero() {
        assertEquals(0, calculateBatteryPercentage(level = -1, scale = 100))
        assertEquals(0, calculateBatteryPercentage(level = 10, scale = 0))
    }

    @Test
    fun batteryPercentageIsClamped() {
        assertEquals(100, calculateBatteryPercentage(level = 150, scale = 100))
    }
}
