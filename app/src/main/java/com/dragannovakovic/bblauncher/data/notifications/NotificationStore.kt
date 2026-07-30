package com.dragannovakovic.bblauncher.data.notifications

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object NotificationStore {
    private val mutableNotifications =
        MutableStateFlow<List<HubNotification>>(emptyList())

    val notifications = mutableNotifications.asStateFlow()

    fun replace(notifications: List<HubNotification>) {
        mutableNotifications.value = notifications.sortedByDescending(HubNotification::postedAt)
    }

    fun upsert(notification: HubNotification) {
        mutableNotifications.update { notifications ->
            buildList {
                add(notification)
                addAll(notifications.filterNot { current -> current.key == notification.key })
            }.sortedByDescending(HubNotification::postedAt)
        }
    }

    fun remove(key: String) {
        mutableNotifications.update { notifications ->
            notifications.filterNot { notification -> notification.key == key }
        }
    }

    fun clear() {
        mutableNotifications.value = emptyList()
    }
}
