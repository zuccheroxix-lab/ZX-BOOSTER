package com.example.model

enum class OptimizationPreset(
    val title: String,
    val subtitle: String,
    val description: String,
    val colorHex: Long
) {
    BALANCED(
        title = "BALANCED",
        subtitle = "Stable & Cool",
        description = "Balanced optimization with standard memory trimming and background task release for maximum device stability.",
        colorHex = 0xFF00F0FF // Cyan
    ),
    PERFORMANCE(
        title = "PERFORMANCE",
        subtitle = "Peak Efficiency",
        description = "Aggressive memory reclaim, close launcher upon game start, and suppress background workers for maximum gaming throughput.",
        colorHex = 0xFFA855F7 // Violet
    ),
    BATTERY_SAVER(
        title = "BATTERY SAVER",
        subtitle = "Extended Play",
        description = "Minimize launcher wake-locks, reduce polling rates, and conserve battery drain during gaming sessions.",
        colorHex = 0xFF00FF88 // Neon Green
    ),
    CUSTOM(
        title = "CUSTOM",
        subtitle = "User Configured",
        description = "Fine-tune individual optimization toggles according to your exact device preferences.",
        colorHex = 0xFFFF7700 // Neon Orange
    );

    companion object {
        fun fromString(value: String?): OptimizationPreset {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: BALANCED
        }
    }
}
