package com.dragannovakovic.bblauncher.notifications

import android.app.Notification
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.dragannovakovic.bblauncher.data.notifications.HubNotification
import com.dragannovakovic.bblauncher.data.notifications.NotificationCommandBridge
import com.dragannovakovic.bblauncher.data.notifications.NotificationCommands
import com.dragannovakovic.bblauncher.data.notifications.NotificationStore

class BBNotificationListenerService :
    NotificationListenerService(),
    NotificationCommands {

    private val appNameCache = mutableMapOf<String, String>()

    override fun onListenerConnected() {
        super.onListenerConnected()
        NotificationCommandBridge.attach(this)
        NotificationStore.replace(
            activeNotifications
                .orEmpty()
                .mapNotNull(::toHubNotification),
        )
    }

    override fun onListenerDisconnected() {
        NotificationCommandBridge.detach(this)
        NotificationStore.clear()
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(notification: StatusBarNotification) {
        val hubNotification = toHubNotification(notification)
        if (hubNotification == null) {
            NotificationStore.remove(notification.key)
        } else {
            NotificationStore.upsert(hubNotification)
        }
    }

    override fun onNotificationRemoved(notification: StatusBarNotification) {
        NotificationStore.remove(notification.key)
    }

    override fun dismissNotification(key: String) {
        cancelNotification(key)
    }

    override fun dismissAllNotifications() {
        cancelAllNotifications()
    }

    override fun onDestroy() {
        NotificationCommandBridge.detach(this)
        NotificationStore.clear()
        super.onDestroy()
    }

    private fun toHubNotification(
        statusBarNotification: StatusBarNotification,
    ): HubNotification? {
        val notification = statusBarNotification.notification
        if (
            statusBarNotification.packageName == packageName ||
            notification.flags and Notification.FLAG_GROUP_SUMMARY != 0
        ) {
            return null
        }

        val appName = loadApplicationName(statusBarNotification.packageName)
        val title = notification.extras
            .getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)
            ?.toString()
            ?.takeIf(String::isNotBlank)
            ?: notification.extras
                .getCharSequence(Notification.EXTRA_TITLE)
                ?.toString()
                ?.takeIf(String::isNotBlank)
            ?: appName
        val text = notification.extras
            .getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?.toString()
            ?.takeIf(String::isNotBlank)
            ?: notification.extras
                .getCharSequence(Notification.EXTRA_TEXT)
                ?.toString()
                ?.takeIf(String::isNotBlank)
            ?: notification.extras
                .getCharSequence(Notification.EXTRA_SUB_TEXT)
                ?.toString()
                .orEmpty()

        return HubNotification(
            key = statusBarNotification.key,
            packageName = statusBarNotification.packageName,
            appName = appName,
            title = title,
            text = text,
            postedAt = statusBarNotification.postTime,
            isClearable = statusBarNotification.isClearable,
            shouldAutoCancel = notification.flags and Notification.FLAG_AUTO_CANCEL != 0,
            contentIntent = notification.contentIntent,
        )
    }

    private fun loadApplicationName(packageName: String): String =
        appNameCache.getOrPut(packageName) {
            try {
                val applicationInfo =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        packageManager.getApplicationInfo(
                            packageName,
                            PackageManager.ApplicationInfoFlags.of(0),
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getApplicationInfo(packageName, 0)
                    }
                packageManager.getApplicationLabel(applicationInfo).toString()
            } catch (_: PackageManager.NameNotFoundException) {
                packageName
            }
        }
}
