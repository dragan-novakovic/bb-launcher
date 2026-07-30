package com.dragannovakovic.bblauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BBColorScheme = darkColorScheme(
    primary = Color(0xFF58C7D6),
    onPrimary = Color(0xFF002F35),
    secondary = Color(0xFF9AE5EE),
    background = Color(0xFF080A0B),
    onBackground = Color(0xFFF2F5F5),
    surface = Color(0xFF15191B),
    onSurface = Color(0xFFF2F5F5),
    surfaceVariant = Color(0xFF242A2D),
    onSurfaceVariant = Color(0xFFB9C2C5),
    outline = Color(0xFF657074),
)

@Composable
fun BBLauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BBColorScheme,
        typography = Typography(),
        content = content,
    )
}
