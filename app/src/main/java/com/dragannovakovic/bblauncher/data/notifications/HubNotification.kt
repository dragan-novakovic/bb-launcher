package com.dragannovakovic.bblauncher.data.notifications

import android.app.PendingIntent

data class HubNotification(
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val postedAt: Long,
    val isClearable: Boolean,
    val shouldAutoCancel: Boolean,
    val contentIntent: PendingIntent?,
)
