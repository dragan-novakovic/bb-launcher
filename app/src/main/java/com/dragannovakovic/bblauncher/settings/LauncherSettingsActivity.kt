package com.dragannovakovic.bblauncher.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dragannovakovic.bblauncher.ui.settings.LauncherSetupScreen
import com.dragannovakovic.bblauncher.ui.system.hideLauncherStatusBar
import com.dragannovakovic.bblauncher.ui.theme.BBLauncherTheme

class LauncherSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideLauncherStatusBar()
        setContent {
            BBLauncherTheme {
                LauncherSetupScreen(onClose = ::finish)
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideLauncherStatusBar()
        }
    }
}
