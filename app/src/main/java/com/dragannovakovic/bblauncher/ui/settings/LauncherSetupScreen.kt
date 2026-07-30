package com.dragannovakovic.bblauncher.ui.settings

import android.Manifest
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dragannovakovic.bblauncher.R

@Composable
fun LauncherSetupScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var refreshCounter by remember { mutableIntStateOf(0) }
    var messageRes by remember { mutableStateOf<Int?>(null) }
    val homeRoleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        refreshCounter++
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        refreshCounter++
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshCounter++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionState = remember(refreshCounter) {
        LauncherPermissionState(
            isDefaultHome = context.isDefaultHome(),
            hasNotificationAccess = NotificationManagerCompat
                .getEnabledListenerPackages(context)
                .contains(context.packageName),
            canWriteSettings = Settings.System.canWrite(context),
            hasCameraPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    fun openIntent(intent: Intent) {
        messageRes = null
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            messageRes = R.string.setup_action_unavailable
        } catch (_: SecurityException) {
            messageRes = R.string.setup_action_unavailable
        }
    }

    fun requestHomeRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (
                roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            ) {
                homeRoleLauncher.launch(
                    roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME),
                )
                return
            }
        }
        openIntent(Intent(Settings.ACTION_HOME_SETTINGS))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF263034),
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            )
            .windowInsetsPadding(
                WindowInsets.displayCutout.only(WindowInsetsSides.Top),
            )
            .windowInsetsPadding(
                WindowInsets.navigationBars.only(WindowInsetsSides.Bottom),
            ),
    ) {
        item(key = "header") {
            SetupHeader(onClose = onClose)
        }

        item(key = "intro") {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = stringResource(R.string.setup_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Light,
                )
                Text(
                    text = stringResource(R.string.setup_description),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                messageRes?.let { currentMessageRes ->
                    Text(
                        text = stringResource(currentMessageRes),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                    )
                }
            }
        }

        item(key = "home-role") {
            SetupPermissionCard(
                title = stringResource(R.string.setup_home_title),
                description = stringResource(R.string.setup_home_description),
                isGranted = permissionState.isDefaultHome,
                actionLabel = stringResource(R.string.setup_home_action),
                onAction = ::requestHomeRole,
            )
        }

        item(key = "notifications") {
            SetupPermissionCard(
                title = stringResource(R.string.setup_notifications_title),
                description = stringResource(R.string.setup_notifications_description),
                isGranted = permissionState.hasNotificationAccess,
                actionLabel = stringResource(R.string.setup_notifications_action),
                onAction = {
                    openIntent(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                },
            )
        }

        item(key = "write-settings") {
            SetupPermissionCard(
                title = stringResource(R.string.setup_rotation_title),
                description = stringResource(R.string.setup_rotation_description),
                isGranted = permissionState.canWriteSettings,
                actionLabel = stringResource(R.string.setup_rotation_action),
                onAction = {
                    openIntent(
                        Intent(
                            Settings.ACTION_MANAGE_WRITE_SETTINGS,
                            "package:${context.packageName}".toUri(),
                        ),
                    )
                },
            )
        }

        item(key = "camera") {
            SetupPermissionCard(
                title = stringResource(R.string.setup_flashlight_title),
                description = stringResource(R.string.setup_flashlight_description),
                isGranted = permissionState.hasCameraPermission,
                actionLabel = stringResource(R.string.setup_flashlight_action),
                onAction = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
            )
        }

        item(key = "hyperos") {
            SetupInformationCard(
                title = stringResource(R.string.setup_hyperos_title),
                description = stringResource(R.string.setup_hyperos_description),
                actionLabel = stringResource(R.string.setup_hyperos_action),
                onAction = {
                    openIntent(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            "package:${context.packageName}".toUri(),
                        ),
                    )
                },
            )
        }

        item(key = "system-boundary") {
            SetupInformationCard(
                title = stringResource(R.string.setup_system_ui_title),
                description = stringResource(R.string.setup_system_ui_description),
                actionLabel = stringResource(R.string.setup_android_settings),
                onAction = {
                    openIntent(Intent(Settings.ACTION_SETTINGS))
                },
            )
        }

        item(key = "bottom-space") {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SetupHeader(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color.Black)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.close_setup),
            modifier = Modifier
                .clickable(role = Role.Button, onClick = onClose)
                .padding(10.dp),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SetupPermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SetupCard(
        title = title,
        description = description,
        statusLabel = stringResource(
            if (isGranted) R.string.setup_granted else R.string.setup_required,
        ),
        statusColor = if (isGranted) {
            Color(0xFF79D69B)
        } else {
            Color(0xFFE4B564)
        },
        actionLabel = actionLabel,
        onAction = onAction,
        modifier = modifier,
    )
}

@Composable
private fun SetupInformationCard(
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SetupCard(
        title = title,
        description = description,
        statusLabel = null,
        statusColor = MaterialTheme.colorScheme.primary,
        actionLabel = actionLabel,
        onAction = onAction,
        modifier = modifier,
    )
}

@Composable
private fun SetupCard(
    title: String,
    description: String,
    statusLabel: String?,
    statusColor: Color,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            statusLabel?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor),
                    )
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Text(
            text = description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            lineHeight = 18.sp,
        )
        Text(
            text = actionLabel,
            modifier = Modifier
                .align(Alignment.End)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
                .clickable(role = Role.Button, onClick = onAction)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

private data class LauncherPermissionState(
    val isDefaultHome: Boolean,
    val hasNotificationAccess: Boolean,
    val canWriteSettings: Boolean,
    val hasCameraPermission: Boolean,
)

private fun Context.isDefaultHome(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = getSystemService(RoleManager::class.java)
        if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
            return roleManager.isRoleHeld(RoleManager.ROLE_HOME)
        }
    }

    val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    val resolvedActivity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.resolveActivity(
            homeIntent,
            PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()),
        )
    } else {
        @Suppress("DEPRECATION")
        packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
    }
    return resolvedActivity?.activityInfo?.packageName == packageName
}
