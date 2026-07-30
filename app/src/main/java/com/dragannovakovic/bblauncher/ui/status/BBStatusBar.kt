package com.dragannovakovic.bblauncher.ui.status

import android.text.format.DateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dragannovakovic.bblauncher.R
import com.dragannovakovic.bblauncher.data.system.ConnectionType
import com.dragannovakovic.bblauncher.data.system.SystemStatus
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.minutes

@Composable
fun BBStatusBar(
    status: SystemStatus,
    notificationCount: Int,
    onOpenHub: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val fontScale = density.fontScale
    val barHeight = 24.dp + ((fontScale - 1f).coerceAtLeast(0f) * 14f).dp
    var now by remember { mutableStateOf(LocalTime.now()) }
    var downwardDrag by remember { mutableFloatStateOf(0f) }
    val openThreshold = with(density) { 28.dp.toPx() }
    val dragState = rememberDraggableState { delta ->
        if (delta > 0f) {
            downwardDrag += delta
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalTime.now()
            delay(1.minutes)
        }
    }

    val timePattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm"
    val formattedTime = remember(now, timePattern) {
        now.format(DateTimeFormatter.ofPattern(timePattern))
    }
    val connectionDescription = stringResource(status.connectionType.descriptionRes)
    val accessibilityDescription = pluralStringResource(
        R.plurals.status_bar_description,
        notificationCount,
        formattedTime,
        connectionDescription,
        status.batteryLevel,
        notificationCount,
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .windowInsetsPadding(
                WindowInsets.displayCutout.only(WindowInsetsSides.Top),
            )
            .height(barHeight)
            .clickable(role = Role.Button, onClick = onOpenHub)
            .draggable(
                state = dragState,
                orientation = Orientation.Vertical,
                onDragStarted = { downwardDrag = 0f },
                onDragStopped = {
                    if (downwardDrag >= openThreshold) {
                        onOpenHub()
                    }
                    downwardDrag = 0f
                },
            )
            .semantics {
                contentDescription = accessibilityDescription
            }
            .padding(horizontal = 7.dp),
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (notificationCount > 0) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = notificationCount.coerceAtMost(99).toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            ConnectionIcon(connectionType = status.connectionType)
        }

        Text(
            text = formattedTime,
            modifier = Modifier.align(Alignment.Center),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.3.sp,
        )

        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (status.isCharging) {
                Text(
                    text = "\u26A1\uFE0E",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 9.sp,
                )
            }
            Text(
                text = "${status.batteryLevel}%",
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 9.sp,
            )
            BatteryIcon(level = status.batteryLevel)
        }
    }
}

@Composable
private fun ConnectionIcon(
    connectionType: ConnectionType,
    modifier: Modifier = Modifier,
) {
    when (connectionType) {
        ConnectionType.Wifi -> WifiIcon(modifier)
        ConnectionType.Cellular -> CellularIcon(modifier)
        ConnectionType.Other -> ConnectedIcon(modifier)
        ConnectionType.Offline -> OfflineIcon(modifier)
    }
}

@Composable
private fun WifiIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 14.dp, height = 10.dp)) {
        val stroke = Stroke(width = 1.3.dp.toPx(), cap = StrokeCap.Round)
        drawArc(
            color = Color.White,
            startAngle = 225f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(0f, -1.dp.toPx()),
            size = Size(size.width, size.height * 1.45f),
            style = stroke,
        )
        drawArc(
            color = Color.White,
            startAngle = 225f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(size.width * 0.24f, size.height * 0.28f),
            size = Size(size.width * 0.52f, size.height * 0.72f),
            style = stroke,
        )
        drawCircle(
            color = Color.White,
            radius = 1.dp.toPx(),
            center = Offset(size.width / 2f, size.height - 0.8.dp.toPx()),
        )
    }
}

@Composable
private fun CellularIcon(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.size(width = 14.dp, height = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        listOf(3.dp, 5.dp, 7.dp, 10.dp).forEach { height ->
            Box(
                modifier = Modifier
                    .size(width = 2.dp, height = height)
                    .background(Color.White),
            )
        }
    }
}

@Composable
private fun ConnectedIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(10.dp)) {
        drawCircle(
            color = Color.White,
            radius = size.minDimension / 2f,
            style = Stroke(width = 1.2.dp.toPx()),
        )
        drawCircle(
            color = Color.White,
            radius = 1.5.dp.toPx(),
        )
    }
}

@Composable
private fun OfflineIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(10.dp)) {
        drawCircle(
            color = Color.White.copy(alpha = 0.65f),
            radius = size.minDimension / 2f,
            style = Stroke(width = 1.2.dp.toPx()),
        )
        drawLine(
            color = Color.White.copy(alpha = 0.65f),
            start = Offset(2.dp.toPx(), size.height - 2.dp.toPx()),
            end = Offset(size.width - 2.dp.toPx(), 2.dp.toPx()),
            strokeWidth = 1.2.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun BatteryIcon(
    level: Int,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(width = 18.dp, height = 9.dp)) {
        val terminalWidth = 2.dp.toPx()
        val bodyWidth = size.width - terminalWidth - 1.dp.toPx()
        drawRoundRect(
            color = Color.White,
            size = Size(bodyWidth, size.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5.dp.toPx()),
            style = Stroke(width = 1.dp.toPx()),
        )
        drawRect(
            color = Color.White,
            topLeft = Offset(bodyWidth + 1.dp.toPx(), size.height * 0.3f),
            size = Size(terminalWidth, size.height * 0.4f),
        )
        val innerPadding = 2.dp.toPx()
        val availableWidth = (bodyWidth - innerPadding * 2).coerceAtLeast(0f)
        drawRect(
            color = if (level <= 15) Color(0xFFE35D5D) else Color.White,
            topLeft = Offset(innerPadding, innerPadding),
            size = Size(
                width = availableWidth * (level.coerceIn(0, 100) / 100f),
                height = (size.height - innerPadding * 2).coerceAtLeast(0f),
            ),
        )
    }
}

private val ConnectionType.descriptionRes: Int
    get() = when (this) {
        ConnectionType.Wifi -> R.string.connection_wifi
        ConnectionType.Cellular -> R.string.connection_mobile
        ConnectionType.Other -> R.string.connection_online
        ConnectionType.Offline -> R.string.connection_offline
    }
