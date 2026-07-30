package com.dragannovakovic.bblauncher.ui.frames

import android.content.Context
import android.os.BatteryManager
import android.text.format.DateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dragannovakovic.bblauncher.R
import com.dragannovakovic.bblauncher.data.apps.LaunchableApp
import kotlinx.coroutines.delay
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Duration.Companion.minutes

@Composable
fun ActiveFramesScreen(
    recentApps: List<LaunchableApp>,
    onAppClicked: (LaunchableApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "clock") {
            ClockFrame()
        }
        item(key = "battery") {
            BatteryFrame()
        }
        items(
            items = recentApps,
            key = LaunchableApp::id,
        ) { app ->
            RecentAppFrame(
                app = app,
                onClick = { onAppClicked(app) },
            )
        }
        if (recentApps.isEmpty()) {
            item(key = "recent-apps-empty") {
                EmptyRecentAppsFrame()
            }
        }
    }
}

@Composable
private fun ClockFrame(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
    var now by remember { mutableStateOf(ZonedDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = ZonedDateTime.now()
            delay(1.minutes)
        }
    }

    val timePattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm"
    val time = remember(now, timePattern) {
        now.format(DateTimeFormatter.ofPattern(timePattern))
    }
    val date = remember(now, locale) {
        now.format(
            DateTimeFormatter
                .ofLocalizedDate(FormatStyle.FULL)
                .withLocale(locale),
        )
    }

    FrameSurface(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            FrameLabel(text = stringResource(R.string.frame_clock))
            Column {
                Text(
                    text = time,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Light,
                )
                Text(
                    text = date,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
private fun BatteryFrame(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var batteryLevel by remember {
        mutableIntStateOf(context.currentBatteryLevel())
    }

    LaunchedEffect(Unit) {
        while (true) {
            batteryLevel = context.currentBatteryLevel()
            delay(1.minutes)
        }
    }

    FrameSurface(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            FrameLabel(text = stringResource(R.string.frame_device))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.battery_percentage, batteryLevel),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(batteryLevel.coerceIn(0, 100) / 100f)
                            .height(5.dp)
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
                Text(
                    text = stringResource(R.string.battery_remaining),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun RecentAppFrame(
    app: LaunchableApp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon = remember(app.icon) { app.icon.asImageBitmap() }

    FrameSurface(
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Image(
                    bitmap = icon,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = app.label,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.frame_recent_app),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun EmptyRecentAppsFrame(modifier: Modifier = Modifier) {
    FrameSurface(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            FrameLabel(text = stringResource(R.string.frame_recent))
            Text(
                text = stringResource(R.string.frame_recent_empty),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 17.sp,
            )
        }
    }
}

@Composable
private fun FrameSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF354044),
                        MaterialTheme.colorScheme.surface,
                    ),
                ),
            )
            .padding(14.dp),
    ) {
        content()
    }
}

@Composable
private fun FrameLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = MaterialTheme.colorScheme.primary,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
    )
}

private fun Context.currentBatteryLevel(): Int {
    val batteryManager = getSystemService(BatteryManager::class.java)
    return batteryManager
        .getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        .coerceIn(0, 100)
}
