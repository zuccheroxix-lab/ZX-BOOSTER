package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_profiles")
data class GameProfileEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isFavorite: Boolean = false,
    val launchCount: Int = 0,
    val lastPlayedTimestamp: Long = 0L,
    val preset: String = "BALANCED",
    val isCustomGame: Boolean = false,
    val notes: String = ""
)
