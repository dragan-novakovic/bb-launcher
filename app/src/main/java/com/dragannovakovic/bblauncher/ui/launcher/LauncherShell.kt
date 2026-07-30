package com.dragannovakovic.bblauncher.ui.launcher

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dragannovakovic.bblauncher.R
import com.dragannovakovic.bblauncher.data.apps.LaunchableApp
import com.dragannovakovic.bblauncher.data.notifications.HubNotification
import com.dragannovakovic.bblauncher.data.system.SystemStatus
import com.dragannovakovic.bblauncher.ui.apps.AppsScreen
import com.dragannovakovic.bblauncher.ui.apps.AppsUiState
import com.dragannovakovic.bblauncher.ui.apps.AppsViewModel
import com.dragannovakovic.bblauncher.ui.apps.AppProfile
import com.dragannovakovic.bblauncher.ui.frames.ActiveFramesScreen
import com.dragannovakovic.bblauncher.ui.hub.HubScreen
import com.dragannovakovic.bblauncher.ui.hub.HubUiState
import com.dragannovakovic.bblauncher.ui.hub.HubViewModel
import com.dragannovakovic.bblauncher.ui.hub.NotificationShade
import com.dragannovakovic.bblauncher.ui.status.BBStatusBar
import com.dragannovakovic.bblauncher.ui.status.StatusBarViewModel
import com.dragannovakovic.bblauncher.ui.theme.BB10Blue
import com.dragannovakovic.bblauncher.ui.theme.BB10BlueDark
import com.dragannovakovic.bblauncher.ui.theme.BB10Wallpaper
import com.dragannovakovic.bblauncher.ui.theme.BBLauncherTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

private val DefaultDestination = LauncherDestination.ActiveFrames

@Composable
fun LauncherShell(
    homeRequestEvents: Flow<Unit>,
    modifier: Modifier = Modifier,
) {
    val appsViewModel: AppsViewModel = viewModel()
    val statusBarViewModel: StatusBarViewModel = viewModel()
    val hubViewModel: HubViewModel = viewModel()
    val appsUiState by appsViewModel.uiState.collectAsStateWithLifecycle()
    val systemStatus by statusBarViewModel.status.collectAsStateWithLifecycle()
    val hubUiState by hubViewModel.uiState.collectAsStateWithLifecycle()

    LauncherShellContent(
        homeRequestEvents = homeRequestEvents,
        appsUiState = appsUiState,
        systemStatus = systemStatus,
        hubUiState = hubUiState,
        onAppQueryChanged = appsViewModel::updateQuery,
        onClearAppQuery = appsViewModel::clearQuery,
        onAppProfileSelected = appsViewModel::selectProfile,
        onAppClicked = appsViewModel::launchApp,
        onRetryApps = appsViewModel::refresh,
        onRequestNotificationAccess = hubViewModel::openNotificationAccessSettings,
        onRefreshNotificationAccess = hubViewModel::refreshNotificationAccess,
        onNotificationClicked = hubViewModel::openNotification,
        onNotificationDismissed = hubViewModel::dismissNotification,
        onClearNotifications = hubViewModel::dismissAllNotifications,
        modifier = modifier,
    )
}

@Composable
private fun LauncherShellContent(
    homeRequestEvents: Flow<Unit>,
    appsUiState: AppsUiState,
    systemStatus: SystemStatus,
    hubUiState: HubUiState,
    onAppQueryChanged: (String) -> Unit,
    onClearAppQuery: () -> Unit,
    onAppProfileSelected: (AppProfile) -> Unit,
    onAppClicked: (LaunchableApp) -> Unit,
    onRetryApps: () -> Unit,
    onRequestNotificationAccess: () -> Unit,
    onRefreshNotificationAccess: () -> Unit,
    onNotificationClicked: (HubNotification) -> Unit,
    onNotificationDismissed: (HubNotification) -> Unit,
    onClearNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val destinations = LauncherDestination.entries
    val pagerState = rememberPagerState(
        initialPage = DefaultDestination.ordinal,
        pageCount = destinations::size,
    )
    val scope = rememberCoroutineScope()
    var isShadeOpen by rememberSaveable { mutableStateOf(false) }

    fun navigateTo(destination: LauncherDestination) {
        scope.launch {
            pagerState.animateScrollToPage(
                page = destination.ordinal,
                animationSpec = tween(durationMillis = 260),
            )
        }
    }

    fun returnHome() {
        onClearAppQuery()
        if (pagerState.currentPage != DefaultDestination.ordinal) {
            scope.launch {
                pagerState.animateScrollToPage(DefaultDestination.ordinal)
            }
        }
    }

    BackHandler {
        if (isShadeOpen) {
            isShadeOpen = false
        } else {
            returnHome()
        }
    }

    LaunchedEffect(homeRequestEvents) {
        homeRequestEvents.collect {
            isShadeOpen = false
            onClearAppQuery()
            if (pagerState.currentPage != DefaultDestination.ordinal) {
                scope.launch {
                    pagerState.animateScrollToPage(DefaultDestination.ordinal)
                }
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        BB10Wallpaper(modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                    ),
                ),
        ) {
            BBStatusBar(
                status = systemStatus,
                notificationCount = hubUiState.notificationCount,
                onOpenHub = { isShadeOpen = true },
            )
            LauncherPageHeader(
                destination = destinations[pagerState.currentPage],
                appsUiState = appsUiState,
                onAppProfileSelected = onAppProfileSelected,
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                key = { page -> destinations[page].name },
            ) { page ->
                when (destinations[page]) {
                    LauncherDestination.Hub -> HubScreen(
                        uiState = hubUiState,
                        onRequestAccess = onRequestNotificationAccess,
                        onRefreshAccess = onRefreshNotificationAccess,
                        onNotificationClicked = onNotificationClicked,
                        onNotificationDismissed = onNotificationDismissed,
                        onClearAll = onClearNotifications,
                    )
                    LauncherDestination.ActiveFrames -> ActiveFramesScreen(
                        recentApps = appsUiState.recentApps,
                        onAppClicked = onAppClicked,
                    )
                    LauncherDestination.Apps -> AppsScreen(
                        uiState = appsUiState,
                        onQueryChanged = onAppQueryChanged,
                        onClearQuery = onClearAppQuery,
                        onAppClicked = onAppClicked,
                        onRetry = onRetryApps,
                    )
                }
            }
            BB10PageNavigation(
                selectedDestination = destinations[pagerState.currentPage],
                onDestinationSelected = ::navigateTo,
            )
        }

        AnimatedVisibility(
            visible = isShadeOpen,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = slideInVertically(initialOffsetY = { fullHeight -> -fullHeight }),
            exit = slideOutVertically(targetOffsetY = { fullHeight -> -fullHeight }),
        ) {
            NotificationShade(
                uiState = hubUiState,
                connectionType = systemStatus.connectionType,
                onClose = { isShadeOpen = false },
                onRequestAccess = onRequestNotificationAccess,
                onRefreshAccess = onRefreshNotificationAccess,
                onNotificationClicked = onNotificationClicked,
                onNotificationDismissed = onNotificationDismissed,
                onClearAll = onClearNotifications,
            )
        }
    }
}

@Composable
private fun LauncherPageHeader(
    destination: LauncherDestination,
    appsUiState: AppsUiState,
    onAppProfileSelected: (AppProfile) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fontScale = LocalDensity.current.fontScale
    when (destination) {
        LauncherDestination.Apps -> Box(
            modifier = modifier
                .fillMaxWidth()
                .height(
                    46.dp + ((fontScale - 1f).coerceAtLeast(0f) * 16f).dp,
                )
                .background(Color.Black.copy(alpha = 0.34f)),
        ) {
            WorkspaceTabs(
                selectedProfile = appsUiState.selectedProfile,
                hasWorkProfile = appsUiState.hasWorkProfile,
                onProfileSelected = onAppProfileSelected,
            )
        }
        LauncherDestination.Hub -> Box(
            modifier = modifier
                .fillMaxWidth()
                .height(
                    36.dp + ((fontScale - 1f).coerceAtLeast(0f) * 14f).dp,
                )
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(BB10Blue, BB10BlueDark),
                    ),
                ),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = stringResource(R.string.blackberry_hub),
                modifier = Modifier.padding(horizontal = 12.dp),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.2.sp,
            )
        }
        LauncherDestination.ActiveFrames -> Spacer(
            modifier = modifier
                .fillMaxWidth()
                .height(5.dp),
        )
    }
}

@Composable
private fun WorkspaceTabs(
    selectedProfile: AppProfile,
    hasWorkProfile: Boolean,
    onProfileSelected: (AppProfile) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        WorkspaceTab(
            label = stringResource(R.string.workspace_personal),
            selected = selectedProfile == AppProfile.Personal,
            enabled = true,
            onClick = { onProfileSelected(AppProfile.Personal) },
            modifier = Modifier.weight(1f),
        )
        WorkspaceTab(
            label = stringResource(R.string.workspace_work),
            selected = selectedProfile == AppProfile.Work,
            enabled = hasWorkProfile,
            onClick = { onProfileSelected(AppProfile.Work) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun WorkspaceTab(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.Black.copy(alpha = 0.48f)
                },
                shape = RoundedCornerShape(2.dp),
            )
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.Tab,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = when {
                selected -> MaterialTheme.colorScheme.onPrimary
                enabled -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            },
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun BB10PageNavigation(
    selectedDestination: LauncherDestination,
    onDestinationSelected: (LauncherDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color.Black.copy(alpha = 0.88f)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .height(14.dp)
                .padding(top = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LauncherDestination.entries.forEach { destination ->
                val selected = destination == selectedDestination
                Box(
                    modifier = Modifier
                        .size(if (selected) 6.dp else 4.dp)
                        .background(
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                            },
                            shape = RoundedCornerShape(50),
                        ),
                )
            }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .selectableGroup(),
        ) {
            LauncherDestination.entries.forEach { destination ->
                val selected = destination == selectedDestination
                val label = stringResource(destination.labelRes)
                val contentColor by animateColorAsState(
                    targetValue = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    label = "navigation color",
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(
                            selected = selected,
                            role = Role.Tab,
                            onClick = { onDestinationSelected(destination) },
                        )
                        .semantics {
                            contentDescription = label
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    DestinationIcon(
                        destination = destination,
                        color = contentColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun DestinationIcon(
    destination: LauncherDestination,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(25.dp)) {
        when (destination) {
            LauncherDestination.Hub -> {
                val stroke = 2.dp.toPx()
                repeat(3) { index ->
                    val y = size.height * (0.27f + index * 0.23f)
                    drawLine(
                        color = color,
                        start = androidx.compose.ui.geometry.Offset(size.width * 0.16f, y),
                        end = androidx.compose.ui.geometry.Offset(
                            size.width * (if (index == 1) 0.86f else 0.72f),
                            y,
                        ),
                        strokeWidth = stroke,
                        cap = StrokeCap.Square,
                    )
                }
                drawRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        size.width * 0.05f,
                        size.height * 0.18f,
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        size.width * 0.05f,
                        size.height * 0.64f,
                    ),
                )
            }
            LauncherDestination.ActiveFrames -> {
                val square = size.width * 0.31f
                val gap = size.width * 0.10f
                repeat(2) { row ->
                    repeat(2) { column ->
                        drawRect(
                            color = color,
                            topLeft = androidx.compose.ui.geometry.Offset(
                                size.width * 0.14f + column * (square + gap),
                                size.height * 0.14f + row * (square + gap),
                            ),
                            size = androidx.compose.ui.geometry.Size(square, square),
                        )
                    }
                }
            }
            LauncherDestination.Apps -> {
                repeat(3) { row ->
                    repeat(3) { column ->
                        drawCircle(
                            color = color,
                            radius = size.width * 0.075f,
                            center = androidx.compose.ui.geometry.Offset(
                                size.width * (0.25f + column * 0.25f),
                                size.height * (0.25f + row * 0.25f),
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 873)
@Composable
private fun LauncherShellPreview() {
    BBLauncherTheme {
        LauncherShellContent(
            homeRequestEvents = emptyFlow(),
            appsUiState = remember { AppsUiState(isLoading = false) },
            systemStatus = remember {
                SystemStatus(
                    batteryLevel = 73,
                    isCharging = true,
                    connectionType = com.dragannovakovic.bblauncher.data.system.ConnectionType.Wifi,
                )
            },
            hubUiState = remember {
                HubUiState(hasNotificationAccess = true)
            },
            onAppQueryChanged = {},
            onClearAppQuery = {},
            onAppProfileSelected = {},
            onAppClicked = {},
            onRetryApps = {},
            onRequestNotificationAccess = {},
            onRefreshNotificationAccess = {},
            onNotificationClicked = {},
            onNotificationDismissed = {},
            onClearNotifications = {},
        )
    }
}
