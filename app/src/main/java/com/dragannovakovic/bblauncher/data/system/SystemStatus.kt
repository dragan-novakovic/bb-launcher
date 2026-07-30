package com.dragannovakovic.bblauncher.data.system

data class SystemStatus(
    val batteryLevel: Int = 0,
    val isCharging: Boolean = false,
    val connectionType: ConnectionType = ConnectionType.Offline,
)

enum class ConnectionType {
    Wifi,
    Cellular,
    Other,
    Offline,
}

internal fun calculateBatteryPercentage(
    level: Int,
    scale: Int,
): Int {
    if (level < 0 || scale <= 0) {
        return 0
    }
    return ((level * 100f) / scale).toInt().coerceIn(0, 100)
}
