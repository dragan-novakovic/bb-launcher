package com.dragannovakovic.bblauncher.data.apps

import org.junit.Assert.assertEquals
import org.junit.Test

class RecentAppsRepositoryTest {
    @Test
    fun recentComponentsRoundTripInOrder() {
        val components = listOf(
            "com.example.one/.MainActivity",
            "com.example.two/.LauncherActivity",
        )

        assertEquals(components, decodeRecentComponents(encodeRecentComponents(components)))
    }

    @Test
    fun decodingRemovesDuplicatesAndLimitsHistory() {
        val encoded = listOf(
            "one",
            "two",
            "one",
            "three",
            "four",
            "five",
        ).joinToString("\n")

        assertEquals(
            listOf("one", "two", "three", "four", "five"),
            decodeRecentComponents(encoded),
        )
    }
}
