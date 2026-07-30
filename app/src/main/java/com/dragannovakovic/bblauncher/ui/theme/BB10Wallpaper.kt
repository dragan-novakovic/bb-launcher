package com.dragannovakovic.bblauncher.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap

@Composable
fun BB10Wallpaper(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1A2630),
                    Color(0xFF10151C),
                    Color(0xFF06090B),
                ),
            ),
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xAA722261),
                    Color(0x446B1F63),
                    Color.Transparent,
                ),
                center = Offset(size.width * 0.82f, size.height * 0.28f),
                radius = size.maxDimension * 0.62f,
            ),
            radius = size.maxDimension * 0.62f,
            center = Offset(size.width * 0.82f, size.height * 0.28f),
        )

        val cyanRibbon = Path().apply {
            moveTo(-size.width * 0.18f, size.height * 1.03f)
            cubicTo(
                size.width * 0.12f,
                size.height * 0.78f,
                size.width * 0.50f,
                size.height * 0.62f,
                size.width * 1.05f,
                -size.height * 0.08f,
            )
            lineTo(size.width * 1.22f, size.height * 0.05f)
            cubicTo(
                size.width * 0.74f,
                size.height * 0.67f,
                size.width * 0.36f,
                size.height * 0.88f,
                size.width * 0.02f,
                size.height * 1.12f,
            )
            close()
        }
        drawPath(
            path = cyanRibbon,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xAA00B8D9),
                    Color(0x771484D2),
                    Color(0x114A5BFF),
                ),
                start = Offset.Zero,
                end = Offset(size.width, size.height),
            ),
        )

        val purpleRibbon = Path().apply {
            moveTo(size.width * 0.48f, -size.height * 0.08f)
            cubicTo(
                size.width * 0.62f,
                size.height * 0.22f,
                size.width * 0.70f,
                size.height * 0.54f,
                size.width * 0.60f,
                size.height * 1.08f,
            )
            lineTo(size.width * 0.88f, size.height * 1.08f)
            cubicTo(
                size.width * 0.92f,
                size.height * 0.56f,
                size.width * 0.82f,
                size.height * 0.16f,
                size.width * 0.66f,
                -size.height * 0.08f,
            )
            close()
        }
        drawPath(
            path = purpleRibbon,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0x447C2A91),
                    Color(0x887A1F76),
                    Color(0x117A1F76),
                ),
                start = Offset(size.width * 0.5f, 0f),
                end = Offset(size.width * 0.8f, size.height),
            ),
        )

        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xAA69E5F4),
                    Color.Transparent,
                ),
            ),
            start = Offset(-size.width * 0.1f, size.height * 0.93f),
            end = Offset(size.width * 1.05f, size.height * 0.06f),
            strokeWidth = 1.4f,
            cap = StrokeCap.Round,
        )

        drawRect(Color.Black.copy(alpha = 0.2f))
    }
}
