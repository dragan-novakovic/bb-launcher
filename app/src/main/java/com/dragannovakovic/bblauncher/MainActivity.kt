package com.dragannovakovic.bblauncher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableIntStateOf
import com.dragannovakovic.bblauncher.ui.launcher.LauncherShell
import com.dragannovakovic.bblauncher.ui.theme.BBLauncherTheme

class MainActivity : ComponentActivity() {
    private val homeRequest = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BBLauncherTheme {
                LauncherShell(homeRequest = homeRequest.intValue)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        homeRequest.intValue++
    }
}
