package com.example.model

data class GameSession(
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationMillis: Long,
    val presetUsed: OptimizationPreset = OptimizationPreset.BALANCED
) {
    val durationFormatted: String
        get() {
            val seconds = (durationMillis / 1000) % 60
            val minutes = (durationMillis / (1000 * 60)) % 60
            val hours = durationMillis / (1000 * 60 * 60)
            return when {
                hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
                minutes > 0 -> "${minutes}m ${seconds}s"
                else -> "${seconds}s"
            }
        }
}
