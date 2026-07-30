package com.dragannovakovic.bblauncher.ui.frames

import android.content.Context
import android.os.BatteryManager
import android.text.format.DateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.platform.LocalDensity
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
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
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

    FrameSurface(
        title = stringResource(R.string.frame_clock),
        accent = Color(0xFF159CC8),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "\u25F7",
                    color = Color.White,
                    fontSize = 18.sp,
                )
            }
            Column {
                Text(
                    text = time,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 34.sp,
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

    FrameSurface(
        title = stringResource(R.string.frame_device),
        accent = Color(0xFF58A943),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 28.dp, height = 14.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.82f))
                        .padding(2.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(batteryLevel.coerceIn(0, 100) / 100f)
                            .fillMaxHeight()
                            .background(Color.White),
                    )
                }
                Text(
                    text = stringResource(R.string.battery_remaining),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                )
            }
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
        title = app.label,
        accent = MaterialTheme.colorScheme.primary,
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
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color.Black.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        bitmap = icon,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = stringResource(R.string.frame_recent_app),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                )
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (index == 2) 0.58f else 0.88f)
                            .height(2.dp)
                            .background(Color.White.copy(alpha = 0.16f)),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyRecentAppsFrame(modifier: Modifier = Modifier) {
    FrameSurface(
        title = stringResource(R.string.frame_recent),
        accent = Color(0xFF6E7680),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "\u25A6",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 24.sp,
                )
            }
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
    title: String,
    accent: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val fontScale = LocalDensity.current.fontScale
    val frameHeight = 188.dp + ((fontScale - 1f).coerceAtLeast(0f) * 90f).dp
    val footerHeight = 29.dp + ((fontScale - 1f).coerceAtLeast(0f) * 8f).dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(frameHeight)
            .clip(RoundedCornerShape(1.dp))
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(1.dp),
            ),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xEE303A3F),
                            Color(0xF21A2023),
                        ),
                    ),
                )
                .padding(12.dp),
        ) {
            content()
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(footerHeight)
                .background(Color.Black.copy(alpha = 0.82f)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .size(width = 3.dp, height = footerHeight)
                    .background(accent),
            )
            Text(
                text = title,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                color = Color.White,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(5.dp)
                    .background(accent, CircleShape),
            )
        }
    }
}

private fun Context.currentBatteryLevel(): Int {
    val batteryManager = getSystemService(BatteryManager::class.java)
    return batteryManager
        .getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        .coerceIn(0, 100)
}
