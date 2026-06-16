package com.example.data

import androidx.compose.runtime.Immutable

@Immutable
data class CpuState(
    val totalUsagePercent: Float,
    val coreCount: Int,
    val coreFrequencies: List<Long>, // In MHz
    val temperatureCelsius: Float?
)

@Immutable
data class RamState(
    val totalBytes: Long,
    val availableBytes: Long,
    val thresholdBytes: Long,
    val usedBytes: Long = totalBytes - availableBytes,
    val usagePercent: Float = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes * 100f) else 0f
)

@Immutable
data class StorageState(
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long = totalBytes - freeBytes,
    val usagePercent: Float = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes * 100f) else 0f
)

@Immutable
data class NetworkSpeedState(
    val rxBytesPerSec: Long, // Download
    val txBytesPerSec: Long, // Upload
    val activeInterface: String // WiFi, Cellular, None
)

@Immutable
data class BatteryState(
    val percentage: Int,
    val isCharging: Boolean,
    val voltageMv: Int,
    val temperatureCelsius: Float,
    val health: String,
    val currentNowMicroAmperes: Int, // Current flow in microamps
    val averageNowMicroAmperes: Int,
    val remainingCapacityMicroAmpHours: Int,
    val capacityPercent: Int
)

@Immutable
data class SystemOverviewState(
    val cpu: CpuState,
    val ram: RamState,
    val storage: StorageState,
    val network: NetworkSpeedState,
    val battery: BatteryState
)
