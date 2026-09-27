package com.example.repository

import android.content.Context
import com.example.data.launcher.GamePackageProvider
import com.example.database.GameDao
import com.example.database.GameProfileEntity
import com.example.database.GameSessionEntity
import com.example.model.Game
import com.example.model.GameSession
import com.example.model.OptimizationPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class GameRepository(
    private val context: Context,
    private val gameDao: GameDao,
    private val packageProvider: GamePackageProvider = GamePackageProvider(context)
) {

    // Combines database profiles with currently installed system applications
    fun getGamesFlow(): Flow<List<Game>> {
        return gameDao.getAllProfilesFlow().map { profiles ->
            scanAndSyncWithDatabase(profiles)
        }.flowOn(Dispatchers.IO)
    }

    fun getFavoriteGamesFlow(): Flow<List<Game>> {
        return getGamesFlow().map { games ->
            games.filter { it.isFavorite }
        }
    }

    fun getRecentGamesFlow(limit: Int = 10): Flow<List<Game>> {
        return getGamesFlow().map { games ->
            games.filter { it.launchCount > 0 }
                .sortedByDescending { it.lastPlayedTimestamp }
                .take(limit)
        }
    }

    suspend fun scanInstalledApps(autoCleanUninstalled: Boolean = true): List<Game> = withContext(Dispatchers.IO) {
        val scannedItems = packageProvider.getInstalledGames(excludeSelf = true)
        val installedPackageNames = scannedItems.map { it.packageName }

        val syncedList = scannedItems.map { scannedGame ->
            val profile = gameDao.getProfile(scannedGame.packageName)
            if (profile == null) {
                // Persist new game profile to database
                gameDao.insertOrIgnoreProfile(
                    GameProfileEntity(
                        packageName = scannedGame.packageName,
                        appName = scannedGame.appName,
                        isFavorite = false,
                        launchCount = 0,
                        lastPlayedTimestamp = 0L,
                        preset = scannedGame.preset.name,
                        isCustomGame = scannedGame.isGame
                    )
                )
                scannedGame
            } else {
                scannedGame.copy(
                    isFavorite = profile.isFavorite,
                    launchCount = profile.launchCount,
                    lastPlayedTimestamp = profile.lastPlayedTimestamp,
                    preset = OptimizationPreset.fromString(profile.preset)
                )
            }
        }

        if (autoCleanUninstalled && installedPackageNames.isNotEmpty()) {
            gameDao.cleanupUninstalled(installedPackageNames)
        }

        syncedList
    }

    private suspend fun scanAndSyncWithDatabase(profiles: List<GameProfileEntity>): List<Game> = withContext(Dispatchers.IO) {
        val scannedItems = packageProvider.getInstalledGames(excludeSelf = true)
        val profileMap = profiles.associateBy { it.packageName }

        scannedItems.map { scannedGame ->
            val profile = profileMap[scannedGame.packageName]
            if (profile != null) {
                scannedGame.copy(
                    isFavorite = profile.isFavorite,
                    launchCount = profile.launchCount,
                    lastPlayedTimestamp = profile.lastPlayedTimestamp,
                    preset = OptimizationPreset.fromString(profile.preset)
                )
            } else {
                scannedGame
            }
        }
    }


    suspend fun toggleFavorite(packageName: String, currentFavorite: Boolean) = withContext(Dispatchers.IO) {
        gameDao.updateFavorite(packageName, !currentFavorite)
    }

    suspend fun updateGamePreset(packageName: String, preset: OptimizationPreset) = withContext(Dispatchers.IO) {
        gameDao.updatePreset(packageName, preset.name)
    }

    suspend fun recordGameLaunch(packageName: String, timestamp: Long = System.currentTimeMillis()) = withContext(Dispatchers.IO) {
        gameDao.recordGameLaunch(packageName, timestamp)
    }

    // --- GAME SESSIONS ---
    fun getAllSessionsFlow(): Flow<List<GameSession>> {
        return gameDao.getAllSessionsFlow().map { entities ->
            entities.map { entity ->
                GameSession(
                    id = entity.id,
                    packageName = entity.packageName,
                    appName = entity.appName,
                    startTimeMillis = entity.startTimeMillis,
                    endTimeMillis = entity.endTimeMillis,
                    durationMillis = entity.durationMillis,
                    presetUsed = OptimizationPreset.fromString(entity.presetUsed)
                )
            }
        }
    }

    suspend fun recordSession(
        packageName: String,
        appName: String,
        startTime: Long,
        endTime: Long,
        preset: OptimizationPreset
    ) = withContext(Dispatchers.IO) {
        val duration = (endTime - startTime).coerceAtLeast(1000L)
        val entity = GameSessionEntity(
            packageName = packageName,
            appName = appName,
            startTimeMillis = startTime,
            endTimeMillis = endTime,
            durationMillis = duration,
            presetUsed = preset.name
        )
        gameDao.insertSession(entity)
    }

    suspend fun clearAllSessions() = withContext(Dispatchers.IO) {
        gameDao.clearAllSessions()
    }

    suspend fun resetAllData() = withContext(Dispatchers.IO) {
        gameDao.clearAllProfiles()
        gameDao.clearAllSessions()
        scanInstalledApps(true)
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 MB"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format("%.2f GB", gb)
            mb >= 1.0 -> String.format("%.1f MB", mb)
            else -> String.format("%.0f KB", kb)
        }
    }
}
