package com.example.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    // --- GAME PROFILES ---
    @Query("SELECT * FROM game_profiles")
    fun getAllProfilesFlow(): Flow<List<GameProfileEntity>>

    @Query("SELECT * FROM game_profiles WHERE packageName = :packageName LIMIT 1")
    suspend fun getProfile(packageName: String): GameProfileEntity?

    @Query("SELECT * FROM game_profiles WHERE isFavorite = 1")
    fun getFavoriteProfilesFlow(): Flow<List<GameProfileEntity>>

    @Query("SELECT * FROM game_profiles WHERE launchCount > 0 ORDER BY lastPlayedTimestamp DESC LIMIT :limit")
    fun getRecentProfilesFlow(limit: Int = 10): Flow<List<GameProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreProfile(profile: GameProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: GameProfileEntity)

    @Update
    suspend fun updateProfile(profile: GameProfileEntity)

    @Query("UPDATE game_profiles SET isFavorite = :isFavorite WHERE packageName = :packageName")
    suspend fun updateFavorite(packageName: String, isFavorite: Boolean)

    @Query("UPDATE game_profiles SET preset = :preset WHERE packageName = :packageName")
    suspend fun updatePreset(packageName: String, preset: String)

    @Query("UPDATE game_profiles SET launchCount = launchCount + 1, lastPlayedTimestamp = :timestamp WHERE packageName = :packageName")
    suspend fun recordGameLaunch(packageName: String, timestamp: Long)

    @Query("DELETE FROM game_profiles WHERE packageName NOT IN (:installedPackages)")
    suspend fun cleanupUninstalled(installedPackages: List<String>)

    @Query("DELETE FROM game_profiles")
    suspend fun clearAllProfiles()


    // --- GAME SESSIONS ---
    @Query("SELECT * FROM game_sessions ORDER BY startTimeMillis DESC")
    fun getAllSessionsFlow(): Flow<List<GameSessionEntity>>

    @Query("SELECT * FROM game_sessions WHERE packageName = :packageName ORDER BY startTimeMillis DESC")
    fun getSessionsForGameFlow(packageName: String): Flow<List<GameSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GameSessionEntity): Long

    @Query("DELETE FROM game_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    @Query("DELETE FROM game_sessions")
    suspend fun clearAllSessions()

    @Query("SELECT SUM(durationMillis) FROM game_sessions WHERE packageName = :packageName")
    suspend fun getTotalDurationForGame(packageName: String): Long?

    @Query("SELECT COUNT(*) FROM game_sessions WHERE packageName = :packageName")
    suspend fun getSessionCountForGame(packageName: String): Int

    @Query("SELECT SUM(durationMillis) FROM game_sessions")
    suspend fun getTotalPlayTimeMillis(): Long?

    @Query("SELECT COUNT(*) FROM game_sessions")
    suspend fun getTotalSessionCount(): Int
}
