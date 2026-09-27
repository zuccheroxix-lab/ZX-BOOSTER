package com.example.ui

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceHardwareInfo
import com.example.model.GameItem
import com.example.model.OptimizationPreset
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MatteBlack
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun HudHeader(
    title: String = "ZX GAME BOOSTER",
    subtitle: String = "PERFORMANCE GAMING CENTER • BY ZUCCHERO XANN",
    launcherState: GamingLauncherState = GamingLauncherState.READY,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Color(launcherState.colorHex))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    ),
                    color = NeonCyan
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }

        StatusPillBadge(launcherState = launcherState)
    }
}

@Composable
fun StatusPillBadge(
    launcherState: GamingLauncherState,
    modifier: Modifier = Modifier
) {
    val stateColor = Color(launcherState.colorHex)
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = DarkSurfaceElevated,
        border = BorderStroke(1.dp, stateColor.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(stateColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = launcherState.label,
                style = MaterialTheme.typography.labelSmall,
                color = stateColor,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun HudCard(
    modifier: Modifier = Modifier,
    borderColor: Color = DarkSurfaceBorder,
    backgroundColor: Color = DarkSurface,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        content()
    }
}

@Composable
fun HardwareGaugeCard(
    title: String,
    valueText: String,
    subText: String,
    progress: Float,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    HudCard(
        modifier = modifier.testTag(testTag),
        borderColor = accentColor.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted
                    )
                }
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = DarkSurfaceElevated
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = subText,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DeviceHealthWarningCard(
    hardwareInfo: DeviceHardwareInfo,
    modifier: Modifier = Modifier
) {
    val hasAlert = hardwareInfo.isStorageLow || hardwareInfo.isBatteryLow || hardwareInfo.isThermalHigh || hardwareInfo.isHighRamLoad
    val alertColor = if (hasAlert) NeonOrange else NeonGreen
    val alertTitle = if (hasAlert) "SYSTEM HEALTH NOTICE" else "DEVICE CONDITION OPTIMAL"

    HudCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = alertColor.copy(alpha = 0.4f),
        backgroundColor = if (hasAlert) DarkSurfaceElevated else DarkSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (hasAlert) Icons.Default.Warning else Icons.Default.Bolt,
                contentDescription = null,
                tint = alertColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alertTitle,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = alertColor
                )
                Text(
                    text = hardwareInfo.healthSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextWhite
                )
            }
        }
    }
}

@Composable
fun FreeFireProfileBanner(
    hardwareInfo: DeviceHardwareInfo,
    onBoostAndPlay: () -> Unit,
    onPlay: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onOpenGameSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    HudCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("free_fire_profile_card"),
        borderColor = NeonOrange.copy(alpha = 0.6f),
        backgroundColor = DarkSurfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = NeonOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FREE FIRE GAMING PROFILE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = NeonOrange
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (hardwareInfo.isFreeFireInstalled) NeonGreen.copy(alpha = 0.15f) else NeonRed.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (hardwareInfo.isFreeFireInstalled) NeonGreen.copy(alpha = 0.5f) else NeonRed.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = if (hardwareInfo.isFreeFireInstalled) "INSTALLED" else "NOT FOUND",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = if (hardwareInfo.isFreeFireInstalled) NeonGreen else NeonRed,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (hardwareInfo.isFreeFireInstalled) {
                Text(
                    text = "${hardwareInfo.freeFireAppName ?: "Free Fire"} (${hardwareInfo.freeFirePackage})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Optimized rootless profile: Launcher RAM reclaim, display sync (${hardwareInfo.refreshRateHz.toInt()}Hz), safe game launch.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onBoostAndPlay,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(40.dp)
                            .testTag("ff_boost_play_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonOrange,
                            contentColor = MatteBlack
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("BOOST & PLAY", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
                    }

                    OutlinedButton(
                        onClick = onPlay,
                        modifier = Modifier
                            .weight(0.8f)
                            .height(40.dp)
                            .testTag("ff_play_button"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, NeonCyan),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("PLAY", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = onOpenAppInfo,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "App Info", tint = TextMuted)
                    }

                    IconButton(
                        onClick = onOpenGameSettings,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextMuted)
                    }
                }
            } else {
                Text(
                    text = "Free Fire tidak ditemukan di perangkat. Pasang Garena Free Fire atau Free Fire MAX melalui store resmi untuk mengaktifkan tuning instan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun DrawableImage(
    drawable: Drawable?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    if (drawable != null) {
        if (drawable is BitmapDrawable && drawable.bitmap != null && !drawable.bitmap.isRecycled) {
            Image(
                bitmap = remember(drawable) { drawable.bitmap.asImageBitmap() },
                contentDescription = contentDescription,
                modifier = modifier
            )
        } else {
            Canvas(
                modifier = modifier
            ) {
                drawIntoCanvas { canvas ->
                    val w = size.width.toInt().coerceAtLeast(1)
                    val h = size.height.toInt().coerceAtLeast(1)
                    drawable.setBounds(0, 0, w, h)
                    drawable.draw(canvas.nativeCanvas)
                }
            }
        }
    } else {
        Box(
            modifier = modifier
                .background(DarkSurfaceElevated, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SportsEsports,
                contentDescription = contentDescription,
                tint = NeonCyan,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun GameItemCard(
    game: GameItem,
    onLaunch: () -> Unit,
    onBoostAndPlay: () -> Unit = onLaunch,
    onOpenDetail: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    HudCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenDetail() }
            .testTag("game_item_${game.packageName}"),
        borderColor = if (game.isFavorite) NeonViolet.copy(alpha = 0.5f) else DarkSurfaceBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            DrawableImage(
                drawable = game.icon,
                contentDescription = game.appName,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = game.appName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (game.isGame) {
                        Spacer(modifier = Modifier.width(6.dp))
                        PresetBadge(preset = game.preset)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = game.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (game.apkSizeFormatted.isNotBlank()) {
                        Text(
                            text = game.apkSizeFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan
                        )
                    }
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

            Spacer(modifier = Modifier.width(6.dp))

            // Favorite Button
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = if (game.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Toggle Favorite",
                    tint = if (game.isFavorite) NeonRed else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Launch Button
            Button(
                onClick = onLaunch,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = MatteBlack
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("play_button_${game.packageName}")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "PLAY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun PresetBadge(preset: OptimizationPreset) {
    val color = Color(preset.colorHex)
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = preset.title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        )
    }
}

