package com.example.model

data class DeviceHardwareInfo(
    // RAM
    val ramAvailableBytes: Long = 0L,
    val ramTotalBytes: Long = 0L,
    val ramUsedBytes: Long = 0L,
    val ramUsedPercentage: Int = 0,
    val isLowMemory: Boolean = false,
    val lowMemoryThresholdBytes: Long = 0L,

    // Launcher Internal Memory
    val appUsedRamBytes: Long = 0L,
    val appMaxRamBytes: Long = 0L,

    // Battery
    val batteryPercentage: Int = 0,
    val isCharging: Boolean = false,
    val batteryStatus: String = "Unknown",
    val batteryHealth: String = "Good",
    val batteryTemperatureCelsius: Float = 0f,
    val batteryVoltageMv: Int = 0,
    val batteryTechnology: String = "Li-ion",

    // Storage
    val storageTotalBytes: Long = 0L,
    val storageAvailableBytes: Long = 0L,
    val storageUsedBytes: Long = 0L,
    val storageUsedPercentage: Int = 0,

    // Display
    val screenResolution: String = "",
    val screenWidthPx: Int = 0,
    val screenHeightPx: Int = 0,
    val refreshRateHz: Float = 60f,
    val screenDensityDpi: Int = 0,

    // Network Status (Real Android ConnectivityManager)
    val isNetworkConnected: Boolean = true,
    val networkType: String = "Wi-Fi",
    val downlinkBandwidthKbps: Int = 0,
    val uplinkBandwidthKbps: Int = 0,
    val networkDetails: String = "Active Connection",

    // Device Health & Alerts (Strictly Real Sensor/API Values)
    val isStorageLow: Boolean = false,
    val isBatteryLow: Boolean = false,
    val isThermalHigh: Boolean = false,
    val isHighRamLoad: Boolean = false,
    val healthSummary: String = "Optimal Condition",

    // SoC & OS Specs
    val androidVersion: String = "",
    val apiLevel: Int = 0,
    val deviceModel: String = "",
    val deviceManufacturer: String = "",
    val hardwareBoard: String = "",
    val cpuAbi: String = "",
    val availableCpuCores: Int = 1,
    val systemUptimeFormatted: String = "",

    // Frame/FPS API Status
    val fpsSupportedByApi: Boolean = false,
    val currentFpsEstimate: Float = 0f,
    val frameStatusMessage: String = "Display Refresh: 60Hz",

    // Free Fire Specific Detection Status
    val isFreeFireInstalled: Boolean = false,
    val freeFirePackage: String? = null,
    val freeFireAppName: String? = null,
    val freeFireVersion: String? = null
)

