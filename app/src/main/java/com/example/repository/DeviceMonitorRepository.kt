package com.example.repository

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import com.example.model.DeviceHardwareInfo
import com.example.utils.PerformanceHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeviceMonitorRepository(
    private val context: Context
) {
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val packageManager = context.packageManager

    fun getDeviceHardwareInfoFlow(pollIntervalMs: Long = 2000L): Flow<DeviceHardwareInfo> = flow {
        while (true) {
            emit(readCurrentHardwareMetrics())
            delay(pollIntervalMs)
        }
    }

    fun readCurrentHardwareMetrics(): DeviceHardwareInfo {
        // 1. RAM Information (Real ActivityManager.MemoryInfo)
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        val ramTotal = memInfo.totalMem
        val ramAvail = memInfo.availMem
        val ramUsed = (ramTotal - ramAvail).coerceAtLeast(0L)
        val ramPercent = if (ramTotal > 0) ((ramUsed.toDouble() / ramTotal.toDouble()) * 100).toInt() else 0

        // Launcher Internal RAM
        val runtime = Runtime.getRuntime()
        val appUsedRam = (runtime.totalMemory() - runtime.freeMemory()).coerceAtLeast(0L)
        val appMaxRam = runtime.maxMemory()

        // 2. Battery Information (Real BatteryManager Intent Broadcast)
        val batteryIntent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val batteryLevel = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
        val batteryScale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryPercent = if (batteryScale > 0) ((batteryLevel.toFloat() / batteryScale.toFloat()) * 100).toInt() else 0

        val batteryStatusInt = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = batteryStatusInt == BatteryManager.BATTERY_STATUS_CHARGING ||
                batteryStatusInt == BatteryManager.BATTERY_STATUS_FULL

        val statusText = when (batteryStatusInt) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Active"
        }

        val healthInt = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val healthText = when (healthInt) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Good"
        }

        val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val tempCelsius = tempTenths / 10.0f
        val voltageMv = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val technology = batteryIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

        // 3. Storage Information (Real StatFs)
        var storageTotal: Long = 0L
        var storageAvail: Long = 0L
        try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            storageTotal = stat.blockSizeLong * stat.blockCountLong
            storageAvail = stat.blockSizeLong * stat.availableBlocksLong
        } catch (_: Exception) {
            // Fallback
        }
        val storageUsed = (storageTotal - storageAvail).coerceAtLeast(0L)
        val storagePercent = if (storageTotal > 0) ((storageUsed.toDouble() / storageTotal.toDouble()) * 100).toInt() else 0

        // 4. Display Information (Real Display metrics & refresh rate)
        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        val densityDpi = displayMetrics.densityDpi
        val refreshRate = PerformanceHelper.getDisplayRefreshRate(context)

        // 5. Network Status (Real ConnectivityManager Capabilities)
        var isNetConnected = false
        var netType = "Offline"
        var downlinkKbps = 0
        var uplinkKbps = 0
        var netDetails = "No active connection"

        try {
            connectivityManager?.let { cm ->
                val activeNetwork = cm.activeNetwork
                val capabilities = cm.getNetworkCapabilities(activeNetwork)
                if (capabilities != null) {
                    isNetConnected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    downlinkKbps = capabilities.linkDownstreamBandwidthKbps
                    uplinkKbps = capabilities.linkUpstreamBandwidthKbps

                    netType = when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
                        else -> "Connected"
                    }

                    netDetails = if (downlinkKbps > 0) {
                        val mbps = downlinkKbps / 1000.0
                        if (mbps >= 1.0) "$netType (${String.format("%.1f", mbps)} Mbps)" else "$netType ($downlinkKbps Kbps)"
                    } else {
                        netType
                    }
                }
            }
        } catch (_: Exception) {
            // Permission or system state exception
        }

        // 6. Device Health & Warning Analysis
        val isStorageLow = storageAvail > 0 && (storageAvail < (2L * 1024 * 1024 * 1024) || storagePercent >= 90)
        val isBatteryLow = batteryPercent in 1..19 && !isCharging
        val isThermalHigh = tempCelsius >= 42.0f
        val isHighRamLoad = ramPercent >= 85

        val healthSummary = when {
            isThermalHigh -> "Thermal Warning (${String.format("%.1f", tempCelsius)}°C)"
            isBatteryLow -> "Low Battery ($batteryPercent%)"
            isStorageLow -> "Low Storage (${storagePercent}% used)"
            isHighRamLoad -> "High RAM Load (${ramPercent}%)"
            else -> "Healthy & Ready for Gaming"
        }

        // 7. Free Fire Package Detection
        var isFreeFireInstalled = false
        var freeFirePackage: String? = null
        var freeFireAppName: String? = null
        var freeFireVersion: String? = null

        val freeFirePackages = listOf("com.dts.freefireth", "com.dts.freefiremax")
        for (pkg in freeFirePackages) {
            try {
                val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0L))
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getPackageInfo(pkg, 0)
                }
                isFreeFireInstalled = true
                freeFirePackage = pkg
                val appInfo = pInfo.applicationInfo
                freeFireAppName = appInfo?.loadLabel(packageManager)?.toString() ?: if (pkg.contains("max")) "Free Fire MAX" else "Free Fire"
                freeFireVersion = pInfo.versionName ?: "Installed"
                break
            } catch (_: PackageManager.NameNotFoundException) {
                // Not installed
            } catch (_: Exception) {
                // Ignore
            }
        }

        // 8. SoC & System Information
        val androidVersion = Build.VERSION.RELEASE ?: "Unknown"
        val apiLevel = Build.VERSION.SDK_INT
        val model = Build.MODEL ?: "Android Device"
        val manufacturer = Build.MANUFACTURER ?: "Generic"
        val board = Build.HARDWARE ?: Build.BOARD ?: "Unknown"
        val cpuAbi = if (Build.SUPPORTED_ABIS.isNotEmpty()) Build.SUPPORTED_ABIS[0] else "arm64-v8a"
        val cores = Runtime.getRuntime().availableProcessors()
        val uptimeSeconds = SystemClock.elapsedRealtime() / 1000
        val uptimeFormatted = formatUptime(uptimeSeconds)

        return DeviceHardwareInfo(
            ramAvailableBytes = ramAvail,
            ramTotalBytes = ramTotal,
            ramUsedBytes = ramUsed,
            ramUsedPercentage = ramPercent,
            isLowMemory = memInfo.lowMemory,
            lowMemoryThresholdBytes = memInfo.threshold,
            appUsedRamBytes = appUsedRam,
            appMaxRamBytes = appMaxRam,
            batteryPercentage = batteryPercent,
            isCharging = isCharging,
            batteryStatus = statusText,
            batteryHealth = healthText,
            batteryTemperatureCelsius = tempCelsius,
            batteryVoltageMv = voltageMv,
            batteryTechnology = technology,
            storageTotalBytes = storageTotal,
            storageAvailableBytes = storageAvail,
            storageUsedBytes = storageUsed,
            storageUsedPercentage = storagePercent,
            screenResolution = "${width}x${height} px",
            screenWidthPx = width,
            screenHeightPx = height,
            refreshRateHz = refreshRate,
            screenDensityDpi = densityDpi,
            isNetworkConnected = isNetConnected,
            networkType = netType,
            downlinkBandwidthKbps = downlinkKbps,
            uplinkBandwidthKbps = uplinkKbps,
            networkDetails = netDetails,
            isStorageLow = isStorageLow,
            isBatteryLow = isBatteryLow,
            isThermalHigh = isThermalHigh,
            isHighRamLoad = isHighRamLoad,
            healthSummary = healthSummary,
            androidVersion = "Android $androidVersion",
            apiLevel = apiLevel,
            deviceModel = model,
            deviceManufacturer = manufacturer,
            hardwareBoard = board,
            cpuAbi = cpuAbi,
            availableCpuCores = cores,
            systemUptimeFormatted = uptimeFormatted,
            fpsSupportedByApi = false,
            frameStatusMessage = "Display panel synced at ${refreshRate.toInt()}Hz. (Per-game live FPS overlay requires Root/ADB debug access on Android).",
            isFreeFireInstalled = isFreeFireInstalled,
            freeFirePackage = freeFirePackage,
            freeFireAppName = freeFireAppName,
            freeFireVersion = freeFireVersion
        )
    }

    private fun formatUptime(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return "${hrs}h ${mins}m ${secs}s"
    }
}

