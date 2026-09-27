package com.example.model

import android.graphics.drawable.Drawable

/**
 * Game entity representing an installed launcher app/game for display in the library.
 */
data class Game(
    val packageName: String,
    val appName: String,
    val className: String,
    val isGame: Boolean,
    val installTime: Long = 0L,
    val updateTime: Long = 0L,
    val apkSizeBytes: Long = 0L,
    val apkSizeFormatted: String = "",
    val isFavorite: Boolean = false,
    val launchCount: Int = 0,
    val lastPlayedTimestamp: Long = 0L,
    val totalPlayDurationMillis: Long = 0L,
    val preset: OptimizationPreset = OptimizationPreset.BALANCED,
    val isInstalled: Boolean = true,
    val icon: Drawable? = null
) {
    val totalDurationFormatted: String
        get() {
            if (totalPlayDurationMillis <= 0) return "0m"
            val totalMinutes = totalPlayDurationMillis / (1000 * 60)
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        }
}

typealias GameItem = Game

