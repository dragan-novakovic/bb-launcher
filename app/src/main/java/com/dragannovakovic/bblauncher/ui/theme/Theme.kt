package com.dragannovakovic.bblauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val BB10Blue = Color(0xFF00A8E0)
val BB10BlueDark = Color(0xFF007CA8)
val BB10Panel = Color(0xFF141A1D)
val BB10PanelRaised = Color(0xFF222A2E)
val BB10TextSecondary = Color(0xFFAEB9BD)
val BB10Paper = Color(0xFFF1F3F3)
val BB10Ink = Color(0xFF202629)
val BB10PaperSecondary = Color(0xFF657075)
val BB10Divider = Color(0xFFD6DCDE)

private val BBColorScheme = darkColorScheme(
    primary = BB10Blue,
    onPrimary = Color.White,
    secondary = Color(0xFF72D7F2),
    background = Color(0xFF050809),
    onBackground = Color(0xFFF5F7F7),
    surface = BB10Panel,
    onSurface = Color(0xFFF5F7F7),
    surfaceVariant = BB10PanelRaised,
    onSurfaceVariant = BB10TextSecondary,
    outline = Color(0xFF69757A),
)

private val BB10Typography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 24.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
)

@Composable
fun BBLauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BBColorScheme,
        typography = BB10Typography,
        content = content,
    )
}
