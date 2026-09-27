package com.example.data.launcher

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
 * Provider that uses [PackageManager] to query launchable applications via
 * [Intent.ACTION_MAIN] and [Intent.CATEGORY_LAUNCHER], and maps them into the [Game] data model.
 */
class GamePackageProvider(
    private val context: Context,
    private val packageManager: PackageManager = context.packageManager
) {

    /**
     * Queries all launchable applications using Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
     * and maps the resolved activities into [Game] data models.
     *
     * @param excludeSelf If true, filters out this launcher application from the results.
     * @return List of mapped [Game] instances.
     */
    suspend fun getInstalledGames(excludeSelf: Boolean = true): List<Game> = withContext(Dispatchers.IO) {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

        val resolveInfoList: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                launcherIntent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(launcherIntent, 0)
        }

        val selfPackageName = context.packageName

        resolveInfoList
            .filter { resolveInfo ->
                if (excludeSelf) resolveInfo.activityInfo.packageName != selfPackageName else true
            }
            .distinctBy { it.activityInfo.packageName }
            .map { resolveInfo ->
                mapResolveInfoToGame(resolveInfo)
            }
    }

    /**
     * Queries only applications that are categorized as games.
     */
    suspend fun getGamesOnly(excludeSelf: Boolean = true): List<Game> = withContext(Dispatchers.IO) {
        getInstalledGames(excludeSelf).filter { it.isGame }
    }

    /**
     * Maps a [ResolveInfo] instance into a [Game] data model.
     */
    fun mapResolveInfoToGame(resolveInfo: ResolveInfo): Game {
        val activityInfo = resolveInfo.activityInfo
        val applicationInfo = activityInfo.applicationInfo
        val packageName = activityInfo.packageName
        val className = activityInfo.name
        val appLabel = resolveInfo.loadLabel(packageManager).toString().trim().ifEmpty { packageName }
        val iconDrawable = resolveInfo.loadIcon(packageManager)

        val isGameApp = checkIsGame(applicationInfo)

        var apkSize: Long = 0L
        try {
            applicationInfo.sourceDir?.let { path ->
                val apkFile = File(path)
                if (apkFile.exists()) {
                    apkSize = apkFile.length()
                }
            }
        } catch (_: Exception) {
            // Ignored
        }

        var firstInstallTime = 0L
        var lastUpdateTime = 0L
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
            firstInstallTime = packageInfo.firstInstallTime
            lastUpdateTime = packageInfo.lastUpdateTime
        } catch (_: Exception) {
            // Ignored
        }

        return Game(
            packageName = packageName,
            appName = appLabel,
            className = className,
            isGame = isGameApp,
            installTime = firstInstallTime,
            updateTime = lastUpdateTime,
            apkSizeBytes = apkSize,
            apkSizeFormatted = formatApkSize(apkSize),
            isFavorite = false,
            launchCount = 0,
            lastPlayedTimestamp = 0L,
            preset = OptimizationPreset.BALANCED,
            isInstalled = true,
            icon = iconDrawable
        )
    }

    /**
     * Checks if the given application is classified as a game.
     */
    private fun checkIsGame(appInfo: ApplicationInfo): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appInfo.category == ApplicationInfo.CATEGORY_GAME
        } else {
            @Suppress("DEPRECATION")
            (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) == ApplicationInfo.FLAG_IS_GAME
        }
    }

    private fun formatApkSize(bytes: Long): String {
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
