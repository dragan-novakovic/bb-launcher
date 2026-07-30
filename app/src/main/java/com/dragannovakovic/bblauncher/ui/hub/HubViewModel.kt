package com.dragannovakovic.bblauncher.ui.hub

import android.app.Application
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dragannovakovic.bblauncher.R
import com.dragannovakovic.bblauncher.data.notifications.HubNotification
import com.dragannovakovic.bblauncher.data.notifications.NotificationCommandBridge
import com.dragannovakovic.bblauncher.data.notifications.NotificationStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HubViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val hasNotificationAccess =
        MutableStateFlow(isNotificationAccessEnabled())
    private val messageRes = MutableStateFlow<Int?>(null)

    val uiState = combine(
        NotificationStore.notifications,
        hasNotificationAccess,
        messageRes,
    ) { notifications, hasAccess, message ->
        HubUiState(
            hasNotificationAccess = hasAccess,
            groups = groupNotifications(notifications),
            messageRes = message,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = HubUiState(),
    )

    fun refreshNotificationAccess() {
        val enabled = isNotificationAccessEnabled()
        hasNotificationAccess.value = enabled
        if (!enabled) {
            NotificationStore.clear()
        }
    }

    fun openNotificationAccessSettings() {
        messageRes.value = null
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            appContext.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            messageRes.value = R.string.notification_settings_unavailable
        }
    }

    fun openNotification(notification: HubNotification) {
        val contentIntent = notification.contentIntent
        if (contentIntent == null) {
            messageRes.value = R.string.notification_has_no_action
            return
        }

        messageRes.value = null
        try {
            contentIntent.send()
            if (notification.shouldAutoCancel && notification.isClearable) {
                NotificationCommandBridge.dismiss(notification.key)
            }
        } catch (_: PendingIntent.CanceledException) {
            messageRes.value = R.string.notification_open_failed
        } catch (_: SecurityException) {
            messageRes.value = R.string.notification_open_failed
        }
    }

    fun dismissNotification(notification: HubNotification) {
        messageRes.value = null
        if (!NotificationCommandBridge.dismiss(notification.key)) {
            messageRes.value = R.string.notification_action_failed
        }
    }

    fun dismissAllNotifications() {
        messageRes.value = null
        if (!NotificationCommandBridge.dismissAll()) {
            messageRes.value = R.string.notification_action_failed
        }
    }

    private fun isNotificationAccessEnabled(): Boolean =
        NotificationManagerCompat
            .getEnabledListenerPackages(appContext)
            .contains(appContext.packageName)
}
