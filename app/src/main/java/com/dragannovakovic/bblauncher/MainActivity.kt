package com.dragannovakovic.bblauncher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dragannovakovic.bblauncher.ui.launcher.LauncherShell
import com.dragannovakovic.bblauncher.ui.system.hideLauncherStatusBar
import com.dragannovakovic.bblauncher.ui.theme.BBLauncherTheme
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class MainActivity : ComponentActivity() {
    private val homeRequestChannel = Channel<Unit>(capacity = Channel.CONFLATED)
    private val homeRequestEvents = homeRequestChannel.receiveAsFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideLauncherStatusBar()
        setContent {
            BBLauncherTheme {
                LauncherShell(homeRequestEvents = homeRequestEvents)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        homeRequestChannel.trySend(Unit)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideLauncherStatusBar()
        }
    }
}
