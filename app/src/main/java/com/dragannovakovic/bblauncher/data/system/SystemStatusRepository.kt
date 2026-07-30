package com.dragannovakovic.bblauncher.data.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

class SystemStatusRepository(context: Context) {
    private val appContext = context.applicationContext

    val status: Flow<SystemStatus> = combine(
        observeBattery(),
        observeConnection(),
    ) { battery, connection ->
        SystemStatus(
            batteryLevel = battery.level,
            isCharging = battery.isCharging,
            connectionType = connection,
        )
    }.distinctUntilChanged()

    private fun observeBattery(): Flow<BatteryState> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(intent.toBatteryState())
            }
        }
        val initialIntent = ContextCompat.registerReceiver(
            appContext,
            receiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_EXPORTED,
        )
        initialIntent?.let { intent ->
            trySend(intent.toBatteryState())
        }

        awaitClose {
            appContext.unregisterReceiver(receiver)
        }
    }.distinctUntilChanged()

    private fun observeConnection(): Flow<ConnectionType> = callbackFlow {
        val connectivityManager =
            appContext.getSystemService(ConnectivityManager::class.java)

        fun emitCurrentConnection() {
            val capabilities = connectivityManager.activeNetwork
                ?.let(connectivityManager::getNetworkCapabilities)
            trySend(capabilities.toConnectionType())
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                emitCurrentConnection()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                trySend(networkCapabilities.toConnectionType())
            }

            override fun onLost(network: Network) {
                emitCurrentConnection()
            }

            override fun onUnavailable() {
                trySend(ConnectionType.Offline)
            }
        }

        emitCurrentConnection()
        connectivityManager.registerDefaultNetworkCallback(callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
}

private data class BatteryState(
    val level: Int,
    val isCharging: Boolean,
)

private fun Intent.toBatteryState(): BatteryState {
    val status = getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
    return BatteryState(
        level = calculateBatteryPercentage(
            level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1),
            scale = getIntExtra(BatteryManager.EXTRA_SCALE, -1),
        ),
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL,
    )
}

private fun NetworkCapabilities?.toConnectionType(): ConnectionType = when {
    this == null ||
        !hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> ConnectionType.Offline
    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.Wifi
    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.Cellular
    else -> ConnectionType.Other
}
