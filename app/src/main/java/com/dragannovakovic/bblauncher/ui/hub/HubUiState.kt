package com.dragannovakovic.bblauncher.ui.hub

import androidx.annotation.StringRes
import com.dragannovakovic.bblauncher.data.notifications.HubNotification

data class HubUiState(
    val hasNotificationAccess: Boolean = false,
    val groups: List<HubNotificationGroup> = emptyList(),
    @param:StringRes val messageRes: Int? = null,
) {
    val notificationCount: Int
        get() = groups.sumOf { group -> group.notifications.size }

    val hasClearableNotifications: Boolean
        get() = groups.any { group ->
            group.notifications.any(HubNotification::isClearable)
        }
}

data class HubNotificationGroup(
    val packageName: String,
    val appName: String,
    val notifications: List<HubNotification>,
)

internal fun groupNotifications(
    notifications: List<HubNotification>,
): List<HubNotificationGroup> =
    notifications
        .groupBy(HubNotification::packageName)
        .map { (packageName, appNotifications) ->
            HubNotificationGroup(
                packageName = packageName,
                appName = appNotifications.first().appName,
                notifications = appNotifications.sortedByDescending(HubNotification::postedAt),
            )
        }
        .sortedByDescending { group ->
            group.notifications.maxOf(HubNotification::postedAt)
        }
