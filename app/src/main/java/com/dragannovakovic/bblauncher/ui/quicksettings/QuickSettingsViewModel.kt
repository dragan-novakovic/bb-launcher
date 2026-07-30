package com.dragannovakovic.bblauncher.ui.quicksettings

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import com.dragannovakovic.bblauncher.R
import com.dragannovakovic.bblauncher.settings.LauncherSettingsActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class QuickSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val cameraManager = appContext.getSystemService(CameraManager::class.java)
    private val mutableUiState = MutableStateFlow(
        QuickSettingsUiState(
            isTorchAvailable = appContext.packageManager.hasSystemFeature(
                PackageManager.FEATURE_CAMERA_FLASH,
            ),
        ),
    )
    private var torchCameraId: String? = null
    private var isTorchCallbackRegistered = false

    val uiState = mutableUiState.asStateFlow()

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeUnavailable(cameraId: String) {
            if (cameraId == torchCameraId) {
                mutableUiState.update { state ->
                    state.copy(isTorchAvailable = false)
                }
            }
        }

        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            if (cameraId == torchCameraId) {
                mutableUiState.update { state ->
                    state.copy(
                        isTorchAvailable = true,
                        isTorchEnabled = enabled,
                    )
                }
            }
        }
    }

    private val rotationObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            refreshSystemSettings()
        }
    }

    init {
        initializeTorch()
        appContext.contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
            false,
            rotationObserver,
        )
        refreshSystemSettings()
    }

    fun refresh() {
        refreshSystemSettings()
    }

    fun openInternetPanel() {
        openIntent(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY))
    }

    fun openBluetoothSettings() {
        openIntent(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
    }

    fun openVolumePanel() {
        openIntent(Intent(Settings.Panel.ACTION_VOLUME))
    }

    fun openDisplaySettings() {
        openIntent(Intent(Settings.ACTION_DISPLAY_SETTINGS))
    }

    fun openSystemSettings() {
        openIntent(Intent(appContext, LauncherSettingsActivity::class.java))
    }

    fun openHomeSettings() {
        openIntent(Intent(Settings.ACTION_HOME_SETTINGS))
    }

    fun toggleTorch() {
        if (torchCameraId == null) {
            initializeTorch()
        }
        val cameraId = torchCameraId
        if (cameraId == null || !mutableUiState.value.isTorchAvailable) {
            mutableUiState.update { state ->
                state.copy(messageRes = R.string.flashlight_unavailable)
            }
            return
        }

        mutableUiState.update { state -> state.copy(messageRes = null) }
        try {
            val enable = !mutableUiState.value.isTorchEnabled
            cameraManager.setTorchMode(cameraId, enable)
            mutableUiState.update { state ->
                state.copy(isTorchEnabled = enable)
            }
        } catch (_: CameraAccessException) {
            mutableUiState.update { state ->
                state.copy(messageRes = R.string.flashlight_failed)
            }
        } catch (_: SecurityException) {
            mutableUiState.update { state ->
                state.copy(messageRes = R.string.flashlight_failed)
            }
        }
    }

    fun toggleAutoRotate() {
        mutableUiState.update { state -> state.copy(messageRes = null) }
        if (!Settings.System.canWrite(appContext)) {
            openIntent(
                Intent(
                    Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    "package:${appContext.packageName}".toUri(),
                ),
            )
            return
        }

        val currentValue = Settings.System.getInt(
            appContext.contentResolver,
            Settings.System.ACCELEROMETER_ROTATION,
            1,
        )
        try {
            val updated = Settings.System.putInt(
                appContext.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                toggledSystemSetting(currentValue),
            )
            if (!updated) {
                mutableUiState.update { state ->
                    state.copy(messageRes = R.string.rotation_update_failed)
                }
            }
        } catch (_: SecurityException) {
            mutableUiState.update { state ->
                state.copy(messageRes = R.string.write_settings_required)
            }
        }
        refreshSystemSettings()
    }

    override fun onCleared() {
        if (isTorchCallbackRegistered) {
            cameraManager.unregisterTorchCallback(torchCallback)
        }
        appContext.contentResolver.unregisterContentObserver(rotationObserver)
        super.onCleared()
    }

    private fun initializeTorch() {
        try {
            torchCameraId = findTorchCameraId()
            if (torchCameraId != null) {
                mutableUiState.update { state ->
                    state.copy(isTorchAvailable = true)
                }
                if (!isTorchCallbackRegistered) {
                    cameraManager.registerTorchCallback(appContext.mainExecutor, torchCallback)
                    isTorchCallbackRegistered = true
                }
            }
        } catch (_: CameraAccessException) {
            mutableUiState.update { state ->
                state.copy(messageRes = R.string.flashlight_unavailable)
            }
        } catch (_: SecurityException) {
            mutableUiState.update { state ->
                state.copy(messageRes = R.string.flashlight_unavailable)
            }
        }
    }

    @Throws(CameraAccessException::class)
    private fun findTorchCameraId(): String? {
        val flashCameras = cameraManager.cameraIdList.filter { cameraId ->
            cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
        return flashCameras.firstOrNull { cameraId ->
            cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.LENS_FACING) ==
                CameraCharacteristics.LENS_FACING_BACK
        } ?: flashCameras.firstOrNull()
    }

    private fun refreshSystemSettings() {
        val rotationEnabled = Settings.System.getInt(
            appContext.contentResolver,
            Settings.System.ACCELEROMETER_ROTATION,
            1,
        ) == 1
        mutableUiState.update { state ->
            state.copy(
                isAutoRotateEnabled = rotationEnabled,
                canWriteSystemSettings = Settings.System.canWrite(appContext),
            )
        }
    }

    private fun openIntent(intent: Intent) {
        mutableUiState.update { state -> state.copy(messageRes = null) }
        try {
            appContext.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) {
            mutableUiState.update { state ->
                state.copy(messageRes = R.string.settings_panel_unavailable)
            }
        } catch (_: SecurityException) {
            mutableUiState.update { state ->
                state.copy(messageRes = R.string.settings_panel_unavailable)
            }
        }
    }
}
