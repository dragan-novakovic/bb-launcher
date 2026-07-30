package com.dragannovakovic.bblauncher.ui.quicksettings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dragannovakovic.bblauncher.R
import com.dragannovakovic.bblauncher.data.system.ConnectionType
import com.dragannovakovic.bblauncher.ui.theme.BB10BlueDark

@Composable
fun QuickSettingsPanel(
    connectionType: ConnectionType,
    modifier: Modifier = Modifier,
) {
    val quickSettingsViewModel: QuickSettingsViewModel = viewModel()
    val uiState by quickSettingsViewModel.uiState.collectAsStateWithLifecycle()

    RefreshQuickSettingsOnResume(quickSettingsViewModel::refresh)

    QuickSettingsContent(
        uiState = uiState,
        connectionType = connectionType,
        onInternetClicked = quickSettingsViewModel::openInternetPanel,
        onBluetoothClicked = quickSettingsViewModel::openBluetoothSettings,
        onTorchClicked = quickSettingsViewModel::toggleTorch,
        onRotationClicked = quickSettingsViewModel::toggleAutoRotate,
        onVolumeClicked = quickSettingsViewModel::openVolumePanel,
        onDisplayClicked = quickSettingsViewModel::openDisplaySettings,
        onSettingsClicked = quickSettingsViewModel::openSystemSettings,
        onHomeClicked = quickSettingsViewModel::openHomeSettings,
        modifier = modifier,
    )
}

@Composable
private fun QuickSettingsContent(
    uiState: QuickSettingsUiState,
    connectionType: ConnectionType,
    onInternetClicked: () -> Unit,
    onBluetoothClicked: () -> Unit,
    onTorchClicked: () -> Unit,
    onRotationClicked: () -> Unit,
    onVolumeClicked: () -> Unit,
    onDisplayClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onHomeClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tiles = listOf(
        QuickSettingTile(
            glyph = "\u25C9",
            labelRes = R.string.quick_internet,
            isActive = connectionType != ConnectionType.Offline,
            onClick = onInternetClicked,
        ),
        QuickSettingTile(
            glyph = "B",
            labelRes = R.string.quick_bluetooth,
            onClick = onBluetoothClicked,
        ),
        QuickSettingTile(
            glyph = "\u2600\uFE0E",
            labelRes = R.string.quick_flashlight,
            isActive = uiState.isTorchEnabled,
            isEnabled = uiState.isTorchAvailable,
            onClick = onTorchClicked,
        ),
        QuickSettingTile(
            glyph = "\u21BB",
            labelRes = R.string.quick_rotation,
            isActive = uiState.isAutoRotateEnabled,
            onClick = onRotationClicked,
        ),
        QuickSettingTile(
            glyph = "\u266B",
            labelRes = R.string.quick_volume,
            onClick = onVolumeClicked,
        ),
        QuickSettingTile(
            glyph = "D",
            labelRes = R.string.quick_display,
            onClick = onDisplayClicked,
        ),
        QuickSettingTile(
            glyph = "\u2699",
            labelRes = R.string.quick_settings,
            onClick = onSettingsClicked,
        ),
        QuickSettingTile(
            glyph = "\u2302",
            labelRes = R.string.quick_home,
            onClick = onHomeClicked,
        ),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF171D20))
            .padding(horizontal = 6.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tiles.chunked(4).forEach { rowTiles ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                rowTiles.forEach { tile ->
                    QuickSettingTile(
                        tile = tile,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        uiState.messageRes?.let { messageRes ->
            Text(
                text = stringResource(messageRes),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun QuickSettingTile(
    tile: QuickSettingTile,
    modifier: Modifier = Modifier,
) {
    val fontScale = LocalDensity.current.fontScale
    val tileAspectRatio = (
        1.35f / fontScale.coerceIn(1f, 1.6f)
    ).coerceAtLeast(0.84f)
    val backgroundColor = if (tile.isActive) {
        BB10BlueDark
    } else {
        Color(0xFF2B3337)
    }
    val contentColor = if (tile.isActive) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier
            .aspectRatio(tileAspectRatio)
            .alpha(if (tile.isEnabled) 1f else 0.48f)
            .clip(RoundedCornerShape(1.dp))
            .background(backgroundColor)
            .clickable(
                enabled = tile.isEnabled,
                role = Role.Button,
                onClick = tile.onClick,
            )
            .padding(horizontal = 3.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = tile.glyph,
            color = contentColor,
            fontSize = 17.sp,
            fontWeight = FontWeight.Light,
        )
        Text(
            text = stringResource(tile.labelRes),
            color = contentColor,
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

private data class QuickSettingTile(
    val glyph: String,
    val labelRes: Int,
    val isActive: Boolean = false,
    val isEnabled: Boolean = true,
    val onClick: () -> Unit,
)

@Composable
private fun RefreshQuickSettingsOnResume(onRefresh: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, onRefresh) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onRefresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
