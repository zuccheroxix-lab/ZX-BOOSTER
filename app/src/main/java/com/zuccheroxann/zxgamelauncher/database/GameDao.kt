package com.zuccheroxann.zxgamelauncher.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Query("SELECT * FROM game_profiles ORDER BY appName ASC")
    fun getAllProfilesFlow(): Flow<List<GameProfile>>

    @Query("SELECT * FROM game_profiles WHERE packageName = :packageName LIMIT 1")
    suspend fun getProfile(packageName: String): GameProfile?

    @Query("SELECT * FROM game_profiles WHERE isFavorite = 1 ORDER BY appName ASC")
    fun getFavoriteProfilesFlow(): Flow<List<GameProfile>>

    @Query("SELECT * FROM game_profiles WHERE launchCount > 0 ORDER BY lastPlayedTimestamp DESC LIMIT :limit")
    fun getRecentProfilesFlow(limit: Int = 10): Flow<List<GameProfile>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreProfile(profile: GameProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: GameProfile)

    @Update
    suspend fun updateProfile(profile: GameProfile)

    @Query("UPDATE game_profiles SET isFavorite = :isFavorite WHERE packageName = :packageName")
    suspend fun updateFavorite(packageName: String, isFavorite: Boolean)

    @Query("UPDATE game_profiles SET preset = :preset WHERE packageName = :packageName")
    suspend fun updatePreset(packageName: String, preset: String)

    @Query("UPDATE game_profiles SET launchCount = launchCount + 1, lastPlayedTimestamp = :timestamp WHERE packageName = :packageName")
    suspend fun recordGameLaunch(packageName: String, timestamp: Long)

    @Query("DELETE FROM game_profiles WHERE packageName = :packageName")
    suspend fun deleteProfile(packageName: String)

    @Delete
    suspend fun deleteProfile(profile: GameProfile)

    @Query("DELETE FROM game_profiles WHERE packageName NOT IN (:installedPackages)")
    suspend fun cleanupUninstalled(installedPackages: List<String>)

    @Query("DELETE FROM game_profiles")
    suspend fun clearAllProfiles()
}
