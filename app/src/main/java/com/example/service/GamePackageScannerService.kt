package com.example.service

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import com.example.model.Game
import com.example.model.OptimizationPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Service that uses PackageManager to query installed applications filtered by
 * intent category [Intent.CATEGORY_LAUNCHER] and maps the results into [Game] entities
 * for display in the library.
 */
class GamePackageScannerService(
    private val context: Context,
    private val packageManager: PackageManager = context.packageManager
) {

    /**
     * Queries all installed applications that declare [Intent.CATEGORY_LAUNCHER] in their manifest
     * and maps each result into a [Game] entity.
     *
     * @param excludeSelf Whether to filter out this launcher application from the results.
     * @return List of [Game] entities ready for display in the library.
     */
    suspend fun queryInstalledLauncherApps(excludeSelf: Boolean = true): List<Game> = withContext(Dispatchers.IO) {
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                launcherIntent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(launcherIntent, 0)
        }

        val selfPackageName = context.packageName

        resolveInfos
            .filter { resolveInfo ->
                if (excludeSelf) resolveInfo.activityInfo.packageName != selfPackageName else true
            }
            .distinctBy { it.activityInfo.packageName }
            .map { resolveInfo ->
                mapResolveInfoToGame(resolveInfo)
            }
    }

    /**
     * Queries installed applications filtered by [Intent.CATEGORY_LAUNCHER] and returns only
     * applications identified as games.
     */
    suspend fun queryInstalledGamesOnly(excludeSelf: Boolean = true): List<Game> = withContext(Dispatchers.IO) {
        queryInstalledLauncherApps(excludeSelf).filter { it.isGame }
    }

    /**
     * Maps a single [ResolveInfo] resolved from the [Intent.CATEGORY_LAUNCHER] query
     * into the [Game] entity.
     */
    fun mapResolveInfoToGame(resolveInfo: ResolveInfo): Game {
        val activityInfo = resolveInfo.activityInfo
        val appInfo = activityInfo.applicationInfo
        val packageName = activityInfo.packageName
        val className = activityInfo.name
        val appName = resolveInfo.loadLabel(packageManager).toString().trim().ifEmpty { packageName }
        val icon = resolveInfo.loadIcon(packageManager)

        val isGame = isGameApplication(appInfo)

        var apkSize: Long = 0L
        try {
            val sourceDir = appInfo.sourceDir
            if (sourceDir != null) {
                val file = File(sourceDir)
                if (file.exists()) {
                    apkSize = file.length()
                }
            }
        } catch (_: Exception) {
            // Fallback to 0 if source dir is not accessible
        }

        var firstInstallTime = 0L
        var lastUpdateTime = 0L
        try {
            val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
            firstInstallTime = pkgInfo.firstInstallTime
            lastUpdateTime = pkgInfo.lastUpdateTime
        } catch (_: Exception) {
            // Ignored if package info query fails
        }

        return Game(
            packageName = packageName,
            appName = appName,
            className = className,
            isGame = isGame,
            installTime = firstInstallTime,
            updateTime = lastUpdateTime,
            apkSizeBytes = apkSize,
            apkSizeFormatted = formatBytes(apkSize),
            isFavorite = false,
            launchCount = 0,
            lastPlayedTimestamp = 0L,
            preset = OptimizationPreset.BALANCED,
            isInstalled = true,
            icon = icon
        )
    }

    /**
     * Determines whether the given [ApplicationInfo] belongs to a game.
     */
    private fun isGameApplication(appInfo: ApplicationInfo): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appInfo.category == ApplicationInfo.CATEGORY_GAME
        } else {
            @Suppress("DEPRECATION")
            (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) == ApplicationInfo.FLAG_IS_GAME
        }
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
