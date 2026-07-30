package com.dragannovakovic.bblauncher.data.apps

import android.content.ComponentName
import android.graphics.Bitmap
import android.os.UserHandle

data class LaunchableApp(
    val id: String,
    val label: String,
    val componentName: ComponentName,
    val user: UserHandle,
    val isWorkProfile: Boolean,
    val icon: Bitmap,
)
