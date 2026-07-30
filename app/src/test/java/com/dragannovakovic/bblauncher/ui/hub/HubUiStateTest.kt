package com.dragannovakovic.bblauncher.ui.hub

import com.dragannovakovic.bblauncher.data.notifications.HubNotification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HubUiStateTest {
    @Test
    fun notificationsAreGroupedByPackageAndNewestGroupFirst() {
        val groups = groupNotifications(
            listOf(
                notification("mail-old", "mail", "Mail", postedAt = 100),
                notification("chat", "chat", "Chat", postedAt = 300),
                notification("mail-new", "mail", "Mail", postedAt = 200),
            ),
        )

        assertEquals(listOf("chat", "mail"), groups.map(HubNotificationGroup::packageName))
        assertEquals(
            listOf("mail-new", "mail-old"),
            groups[1].notifications.map(HubNotification::key),
        )
    }

    @Test
    fun stateCountsAndFindsClearableNotifications() {
        val state = HubUiState(
            hasNotificationAccess = true,
            groups = groupNotifications(
                listOf(
                    notification("one", "mail", "Mail", postedAt = 100),
                    notification(
                        key = "two",
                        packageName = "chat",
                        appName = "Chat",
                        postedAt = 200,
                        isClearable = false,
                    ),
                ),
            ),
        )

        assertEquals(2, state.notificationCount)
        assertTrue(state.hasClearableNotifications)
    }

    private fun notification(
        key: String,
        packageName: String,
        appName: String,
        postedAt: Long,
        isClearable: Boolean = true,
    ) = HubNotification(
        key = key,
        packageName = packageName,
        appName = appName,
        title = key,
        text = "",
        postedAt = postedAt,
        isClearable = isClearable,
        shouldAutoCancel = false,
        contentIntent = null,
    )
}
