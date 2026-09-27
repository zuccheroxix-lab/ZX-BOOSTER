package com.example.model

enum class ThemeMode {
    DARK,
    LIGHT,
    AUTO
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val isHapticEnabled: Boolean = true,
    val isNotificationsEnabled: Boolean = true,
    val isAutoScanEnabled: Boolean = true,
    val isCloseLauncherOnLaunch: Boolean = false,
    val defaultPreset: OptimizationPreset = OptimizationPreset.BALANCED,
    val isAggressiveTrimEnabled: Boolean = true,
    val isAnimationEnabled: Boolean = true
)
