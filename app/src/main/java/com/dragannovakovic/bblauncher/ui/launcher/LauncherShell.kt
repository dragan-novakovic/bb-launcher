package com.dragannovakovic.bblauncher.ui.launcher

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dragannovakovic.bblauncher.R
import com.dragannovakovic.bblauncher.data.apps.LaunchableApp
import com.dragannovakovic.bblauncher.ui.apps.AppsScreen
import com.dragannovakovic.bblauncher.ui.apps.AppsUiState
import com.dragannovakovic.bblauncher.ui.apps.AppsViewModel
import com.dragannovakovic.bblauncher.ui.frames.ActiveFramesScreen
import com.dragannovakovic.bblauncher.ui.theme.BBLauncherTheme
import kotlinx.coroutines.launch

private val DefaultDestination = LauncherDestination.ActiveFrames

@Composable
fun LauncherShell(
    homeRequest: Int,
    modifier: Modifier = Modifier,
) {
    val appsViewModel: AppsViewModel = viewModel()
    val appsUiState by appsViewModel.uiState.collectAsStateWithLifecycle()

    LauncherShellContent(
        homeRequest = homeRequest,
        appsUiState = appsUiState,
        onAppQueryChanged = appsViewModel::updateQuery,
        onClearAppQuery = appsViewModel::clearQuery,
        onAppClicked = appsViewModel::launchApp,
        onRetryApps = appsViewModel::refresh,
        modifier = modifier,
    )
}

@Composable
private fun LauncherShellContent(
    homeRequest: Int,
    appsUiState: AppsUiState,
    onAppQueryChanged: (String) -> Unit,
    onClearAppQuery: () -> Unit,
    onAppClicked: (LaunchableApp) -> Unit,
    onRetryApps: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val destinations = LauncherDestination.entries
    val pagerState = rememberPagerState(
        initialPage = DefaultDestination.ordinal,
        pageCount = destinations::size,
    )
    val scope = rememberCoroutineScope()

    fun returnHome() {
        onClearAppQuery()
        if (pagerState.currentPage != DefaultDestination.ordinal) {
            scope.launch {
                pagerState.animateScrollToPage(DefaultDestination.ordinal)
            }
        }
    }

    BackHandler(onBack = ::returnHome)

    LaunchedEffect(homeRequest) {
        onClearAppQuery()
        if (pagerState.currentPage != DefaultDestination.ordinal) {
            pagerState.animateScrollToPage(DefaultDestination.ordinal)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color(0xFF283034),
                    0.48f to Color(0xFF111517),
                    1f to MaterialTheme.colorScheme.background,
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars),
        ) {
            PageHeading(
                destination = destinations[pagerState.currentPage],
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                key = { page -> destinations[page].name },
            ) { page ->
                when (destinations[page]) {
                    LauncherDestination.Hub -> HubPlaceholder()
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
            LauncherNavigation(
                selectedDestination = destinations[pagerState.currentPage],
                onDestinationSelected = { destination ->
                    scope.launch {
                        pagerState.animateScrollToPage(
                            page = destination.ordinal,
                            animationSpec = tween(durationMillis = 260),
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun PageHeading(
    destination: LauncherDestination,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(destination.labelRes),
        modifier = modifier,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 30.sp,
        fontWeight = FontWeight.Light,
        letterSpacing = 0.6.sp,
    )
}

@Composable
private fun HubPlaceholder(modifier: Modifier = Modifier) {
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
                text = stringResource(R.string.hub_empty_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun LauncherNavigation(
    selectedDestination: LauncherDestination,
    onDestinationSelected: (LauncherDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(Color.Black.copy(alpha = 0.8f))
            .selectableGroup(),
    ) {
        LauncherDestination.entries.forEach { destination ->
            val selected = destination == selectedDestination
            val contentColor by animateColorAsState(
                targetValue = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                label = "navigation color",
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .selectable(
                        selected = selected,
                        role = Role.Tab,
                        onClick = { onDestinationSelected(destination) },
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = if (selected) 22.dp else 16.dp, height = 3.dp)
                        .background(contentColor),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(destination.labelRes),
                    color = contentColor,
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 873)
@Composable
private fun LauncherShellPreview() {
    BBLauncherTheme {
        LauncherShellContent(
            homeRequest = 0,
            appsUiState = remember { AppsUiState(isLoading = false) },
            onAppQueryChanged = {},
            onClearAppQuery = {},
            onAppClicked = {},
            onRetryApps = {},
        )
    }
}
