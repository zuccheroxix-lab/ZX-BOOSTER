package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceHardwareInfo
import com.example.model.GameItem
import com.example.model.OptimizationResult
import com.example.ui.DeviceHealthWarningCard
import com.example.ui.DrawableImage
import com.example.ui.FreeFireProfileBanner
import com.example.ui.GamingLauncherState
import com.example.ui.HardwareGaugeCard
import com.example.ui.HudCard
import com.example.ui.HudHeader
import com.example.ui.NavigationTab
import com.example.ui.PresetBadge
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MatteBlack
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.utils.AppLauncher

@Composable
fun HomeScreen(
    hardwareInfo: DeviceHardwareInfo,
    launcherState: GamingLauncherState,
    activeSessionSeconds: Long,
    selectedGame: GameItem?,
    favorites: List<GameItem>,
    recentGames: List<GameItem>,
    allGames: List<GameItem>,
    isOptimizing: Boolean,
    lastOptimizationResult: OptimizationResult?,
    onQuickOptimize: () -> Unit,
    onBoostAndPlayGame: (GameItem) -> Unit,
    onLaunchGame: (GameItem) -> Unit,
    onBoostAndPlayFreeFire: () -> Unit,
    onPlayFreeFire: () -> Unit,
    onOpenGameDetail: (GameItem) -> Unit,
    onNavigateToTab: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Header HUD with live Launcher state
        item {
            HudHeader(
                title = "ZX GAME BOOSTER",
                subtitle = "PERFORMANCE GAMING CENTER • BY ZUCCHERO XANN",
                launcherState = launcherState
            )
        }

        // 2. Active Session HUD Banner (if currently PLAYING or session active)
        if (launcherState == GamingLauncherState.PLAYING && activeSessionSeconds > 0) {
            item {
                HudCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    borderColor = NeonGreen,
                    backgroundColor = DarkSurfaceElevated
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SportsEsports, contentDescription = null, tint = NeonGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("GAMING SESSION RUNNING", style = MaterialTheme.typography.labelMedium, color = NeonGreen, fontWeight = FontWeight.Bold)
                                Text(selectedGame?.appName ?: "Active Game", style = MaterialTheme.typography.bodySmall, color = TextWhite)
                            }
                        }
                        val mins = activeSessionSeconds / 60
                        val secs = activeSessionSeconds % 60
                        Text(
                            text = String.format("%02d:%02d", mins, secs),
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonGreen,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // 3. Real-Time Hardware Status Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "[ DEVICE STATUS ]",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${hardwareInfo.deviceModel} • ${hardwareInfo.androidVersion}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // RAM & Battery
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val ramGbUsed = hardwareInfo.ramUsedBytes / (1024.0 * 1024.0 * 1024.0)
                    val ramGbTotal = hardwareInfo.ramTotalBytes / (1024.0 * 1024.0 * 1024.0)
                    HardwareGaugeCard(
                        title = "RAM",
                        valueText = "${hardwareInfo.ramUsedPercentage}%",
                        subText = "${String.format("%.1f", ramGbUsed)} / ${String.format("%.1f", ramGbTotal)} GB",
                        progress = hardwareInfo.ramUsedPercentage / 100f,
                        accentColor = if (hardwareInfo.ramUsedPercentage > 85) NeonOrange else NeonCyan,
                        icon = Icons.Default.Memory,
                        modifier = Modifier.weight(1f),
                        testTag = "gauge_ram"
                    )

                    HardwareGaugeCard(
                        title = "BATTERY",
                        valueText = "${hardwareInfo.batteryPercentage}%",
                        subText = "${hardwareInfo.batteryStatus} • ${String.format("%.1f", hardwareInfo.batteryTemperatureCelsius)}°C",
                        progress = hardwareInfo.batteryPercentage / 100f,
                        accentColor = if (hardwareInfo.isCharging) NeonGreen else NeonViolet,
                        icon = Icons.Default.BatteryChargingFull,
                        modifier = Modifier.weight(1f),
                        testTag = "gauge_battery"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Storage & Network/Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val storageGbUsed = hardwareInfo.storageUsedBytes / (1024.0 * 1024.0 * 1024.0)
                    val storageGbTotal = hardwareInfo.storageTotalBytes / (1024.0 * 1024.0 * 1024.0)
                    HardwareGaugeCard(
                        title = "STORAGE",
                        valueText = "${hardwareInfo.storageUsedPercentage}%",
                        subText = "${String.format("%.1f", storageGbUsed)} / ${String.format("%.1f", storageGbTotal)} GB",
                        progress = hardwareInfo.storageUsedPercentage / 100f,
                        accentColor = NeonElectricBlue,
                        icon = Icons.Default.SdStorage,
                        modifier = Modifier.weight(1f),
                        testTag = "gauge_storage"
                    )

                    HardwareGaugeCard(
                        title = "NETWORK & FPS",
                        valueText = "${hardwareInfo.refreshRateHz.toInt()} Hz",
                        subText = hardwareInfo.networkDetails,
                        progress = 1.0f,
                        accentColor = if (hardwareInfo.isNetworkConnected) NeonGreen else NeonOrange,
                        icon = if (hardwareInfo.isNetworkConnected) Icons.Default.Wifi else Icons.Default.Thermostat,
                        modifier = Modifier.weight(1f),
                        testTag = "gauge_network_display"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Device Health Alert Banner
                DeviceHealthWarningCard(hardwareInfo = hardwareInfo)
            }
        }

        // 4. Dedicated FREE FIRE Profile Banner
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                FreeFireProfileBanner(
                    hardwareInfo = hardwareInfo,
                    onBoostAndPlay = onBoostAndPlayFreeFire,
                    onPlay = onPlayFreeFire,
                    onOpenAppInfo = {
                        hardwareInfo.freeFirePackage?.let { AppLauncher.openAppInfo(context, it) }
                    },
                    onOpenGameSettings = {
                        hardwareInfo.freeFirePackage?.let { AppLauncher.openNotificationSettings(context, it) }
                    }
                )
            }
        }

        // 5. Main Action Cards: [ GAME BOOST ], [ PLAY GAME ], [ PERFORMANCE ], [ DEVICE STATUS ], [ GAME SESSION ]
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "[ QUICK ACTIONS ]",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(
                        title = "GAME BOOST",
                        subtitle = if (isOptimizing) "Optimizing..." else "Reclaim Memory",
                        icon = Icons.Default.RocketLaunch,
                        accentColor = NeonCyan,
                        onClick = onQuickOptimize,
                        modifier = Modifier.weight(1f),
                        testTag = "action_boost"
                    )

                    QuickActionTile(
                        title = "PLAY GAME",
                        subtitle = selectedGame?.appName ?: "Launch Library",
                        icon = Icons.Default.PlayArrow,
                        accentColor = NeonGreen,
                        onClick = {
                            if (selectedGame != null) onBoostAndPlayGame(selectedGame) else onNavigateToTab(NavigationTab.GAMES)
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "action_play"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(
                        title = "PERFORMANCE",
                        subtitle = "Presets & Tuning",
                        icon = Icons.Default.Speed,
                        accentColor = NeonViolet,
                        onClick = { onNavigateToTab(NavigationTab.OPTIMIZER) },
                        modifier = Modifier.weight(1f),
                        testTag = "action_performance"
                    )

                    QuickActionTile(
                        title = "GAME SESSIONS",
                        subtitle = "Playtime & Logs",
                        icon = Icons.Default.History,
                        accentColor = NeonElectricBlue,
                        onClick = { onNavigateToTab(NavigationTab.MONITOR) },
                        modifier = Modifier.weight(1f),
                        testTag = "action_sessions"
                    )
                }
            }
        }

        // 6. Favorites Carousel
        if (favorites.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(22.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = NeonViolet,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "[ FAVORITES ]",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonViolet,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${favorites.size} games",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favorites, key = { "fav_${it.packageName}" }) { game ->
                        FavoriteGameCard(
                            game = game,
                            onPlay = { onBoostAndPlayGame(game) },
                            onClick = { onOpenGameDetail(game) }
                        )
                    }
                }
            }
        }

        // 7. Recent Games
        item {
            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "[ RECENT GAMES ]",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "VIEW ALL",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onNavigateToTab(NavigationTab.GAMES) }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        if (recentGames.isEmpty()) {
            item {
                HudCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    backgroundColor = DarkSurface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No games played yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Launch games from the Games Library tab to track play sessions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(recentGames.take(4), key = { "recent_${it.packageName}" }) { game ->
                RecentGameRow(
                    game = game,
                    onPlay = { onBoostAndPlayGame(game) },
                    onClick = { onOpenGameDetail(game) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                )
            }
        }

        // 8. Quick Game Launch Grid (My Games)
        item {
            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "[ MY GAMES ]",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${allGames.size} INSTALLED",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        if (allGames.isEmpty()) {
            item {
                HudCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    backgroundColor = DarkSurface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Scanning device applications...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            items(allGames.take(6), key = { "all_${it.packageName}" }) { game ->
                RecentGameRow(
                    game = game,
                    onPlay = { onBoostAndPlayGame(game) },
                    onClick = { onOpenGameDetail(game) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    HudCard(
        modifier = modifier
            .clickable { onClick() }
            .testTag(testTag),
        borderColor = accentColor.copy(alpha = 0.4f),
        backgroundColor = DarkSurfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = TextWhite,
                fontWeight = FontWeight.Black
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FavoriteGameCard(
    game: GameItem,
    onPlay: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HudCard(
        modifier = modifier
            .width(140.dp)
            .clickable { onClick() }
            .testTag("favorite_card_${game.packageName}"),
        borderColor = NeonViolet.copy(alpha = 0.5f),
        backgroundColor = DarkSurfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DrawableImage(
                drawable = game.icon,
                contentDescription = game.appName,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, NeonViolet.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = game.appName,
                style = MaterialTheme.typography.labelMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(3.dp))

            PresetBadge(preset = game.preset)

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onPlay,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonViolet,
                    contentColor = TextWhite
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "PLAY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun RecentGameRow(
    game: GameItem,
    onPlay: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HudCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("game_row_${game.packageName}"),
        borderColor = DarkSurfaceBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DrawableImage(
                drawable = game.icon,
                contentDescription = game.appName,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.appName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PresetBadge(preset = game.preset)
                    if (game.launchCount > 0) {
                        Text(
                            text = "• ${game.launchCount} plays",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    if (game.totalPlayDurationMillis > 0) {
                        Text(
                            text = "• ${game.totalDurationFormatted}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonElectricBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onPlay,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = MatteBlack
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "PLAY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

