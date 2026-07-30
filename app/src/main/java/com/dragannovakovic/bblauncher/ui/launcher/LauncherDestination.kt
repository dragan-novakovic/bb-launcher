package com.dragannovakovic.bblauncher.ui.launcher

import androidx.annotation.StringRes
import com.dragannovakovic.bblauncher.R

enum class LauncherDestination(
    @param:StringRes val labelRes: Int,
) {
    Hub(R.string.destination_hub),
    ActiveFrames(R.string.destination_active_frames),
    Apps(R.string.destination_apps),
}
