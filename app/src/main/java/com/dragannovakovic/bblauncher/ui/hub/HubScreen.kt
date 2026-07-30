package com.dragannovakovic.bblauncher.ui.hub

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dragannovakovic.bblauncher.R
import com.dragannovakovic.bblauncher.data.notifications.HubNotification
import com.dragannovakovic.bblauncher.data.system.ConnectionType
import com.dragannovakovic.bblauncher.ui.quicksettings.QuickSettingsPanel

@Composable
fun HubScreen(
    uiState: HubUiState,
    onRequestAccess: () -> Unit,
    onRefreshAccess: () -> Unit,
    onNotificationClicked: (HubNotification) -> Unit,
    onNotificationDismissed: (HubNotification) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RefreshAccessOnResume(onRefreshAccess)

    Column(modifier = modifier.fillMaxSize()) {
        uiState.messageRes?.let { messageRes ->
            Text(
                text = stringResource(messageRes),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
        }

        when {
            !uiState.hasNotificationAccess -> NotificationAccessCard(
                onRequestAccess = onRequestAccess,
            )
            uiState.notificationCount == 0 -> EmptyHub()
            else -> NotificationGroups(
                uiState = uiState,
                onNotificationClicked = onNotificationClicked,
                onNotificationDismissed = onNotificationDismissed,
                onClearAll = onClearAll,
            )
        }
    }
}

@Composable
fun NotificationShade(
    uiState: HubUiState,
    connectionType: ConnectionType,
    onClose: () -> Unit,
    onRequestAccess: () -> Unit,
    onRefreshAccess: () -> Unit,
    onNotificationClicked: (HubNotification) -> Unit,
    onNotificationDismissed: (HubNotification) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF20282B),
                        Color(0xFF080A0B),
                    ),
                ),
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Final)
                        event.changes
                            .filterNot { change -> change.isConsumed }
                            .forEach { change -> change.consume() }
                    }
                }
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(Color.Black)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.notification_shade_title),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Light,
            )
            Text(
                text = stringResource(R.string.close_shade),
                modifier = Modifier
                    .clickable(role = Role.Button, onClick = onClose)
                    .padding(10.dp),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        QuickSettingsPanel(connectionType = connectionType)
        HubScreen(
            uiState = uiState,
            onRequestAccess = onRequestAccess,
            onRefreshAccess = onRefreshAccess,
            onNotificationClicked = onNotificationClicked,
            onNotificationDismissed = onNotificationDismissed,
            onClearAll = onClearAll,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun NotificationAccessCard(
    onRequestAccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "!",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light,
                )
            }
            Text(
                text = stringResource(R.string.notification_access_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.notification_access_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.enable_notification_access),
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(role = Role.Button, onClick = onRequestAccess)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun EmptyHub(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
            Text(
                text = stringResource(R.string.hub_empty_title),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.hub_empty_enabled_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun NotificationGroups(
    uiState: HubUiState,
    onNotificationClicked: (HubNotification) -> Unit,
    onNotificationDismissed: (HubNotification) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "notification-summary") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = pluralStringResource(
                        R.plurals.notification_count,
                        uiState.notificationCount,
                        uiState.notificationCount,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                )
                if (uiState.hasClearableNotifications) {
                    Text(
                        text = stringResource(R.string.clear_all),
                        modifier = Modifier
                            .clickable(role = Role.Button, onClick = onClearAll)
                            .padding(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        items(
            items = uiState.groups,
            key = HubNotificationGroup::packageName,
        ) { group ->
            NotificationGroup(
                group = group,
                onNotificationClicked = onNotificationClicked,
                onNotificationDismissed = onNotificationDismissed,
            )
        }

        item(key = "notification-bottom-space") {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun NotificationGroup(
    group: HubNotificationGroup,
    onNotificationClicked: (HubNotification) -> Unit,
    onNotificationDismissed: (HubNotification) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.42f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = group.appName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = group.appName,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = group.notifications.size.toString(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
            )
        }

        group.notifications.forEach { notification ->
            NotificationCard(
                notification = notification,
                onClick = { onNotificationClicked(notification) },
                onDismiss = { onNotificationDismissed(notification) },
            )
        }
    }
}

@Composable
private fun NotificationCard(
    notification: HubNotification,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val relativeTime = remember(notification.postedAt) {
        DateUtils.getRelativeTimeSpanString(
            notification.postedAt,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE,
        ).toString()
    }
    val clickModifier = if (notification.contentIntent != null) {
        Modifier.clickable(role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
            .then(clickModifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = notification.title,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = relativeTime,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                )
            }
            if (notification.text.isNotBlank()) {
                Text(
                    text = notification.text,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (notification.isClearable) {
            Text(
                text = "\u00D7",
                modifier = Modifier
                    .clickable(
                        role = Role.Button,
                        onClickLabel = stringResource(R.string.dismiss_notification),
                        onClick = onDismiss,
                    )
                    .padding(start = 4.dp),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Light,
            )
        }
    }
}

@Composable
private fun RefreshAccessOnResume(onRefreshAccess: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, onRefreshAccess) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onRefreshAccess()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
