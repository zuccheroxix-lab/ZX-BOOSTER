package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSettings
import com.example.model.DeviceHardwareInfo
import com.example.model.OptimizationPreset
import com.example.model.OptimizationResult
import com.example.ui.HudCard
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
fun OptimizerScreen(
    hardwareInfo: DeviceHardwareInfo,
    isOptimizing: Boolean,
    lastOptimizationResult: OptimizationResult?,
    selectedPreset: OptimizationPreset,
    onSelectPreset: (OptimizationPreset) -> Unit,
    onRunOptimize: () -> Unit,
    settings: AppSettings,
    onToggleCloseOnLaunch: (Boolean) -> Unit,
    onToggleAggressiveTrim: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "optimizing")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "DEVICE OPTIMIZER",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonCyan,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Authentic, Root-Free Android Memory & Task Optimizer",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        // 2. Big Optimize Hero HUD
        item {
            HudCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("optimizer_hero_card"),
                borderColor = NeonCyan.copy(alpha = 0.5f),
                backgroundColor = DarkSurfaceElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Big Circular Dial
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .then(if (isOptimizing) Modifier.scale(pulseScale) else Modifier),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { hardwareInfo.ramUsedPercentage / 100f },
                            modifier = Modifier.fillMaxSize(),
                            color = NeonCyan,
                            trackColor = DarkSurfaceBorder,
                            strokeWidth = 10.dp
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${hardwareInfo.ramUsedPercentage}%",
                                style = MaterialTheme.typography.headlineLarge,
                                color = TextWhite,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "RAM USED",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val ramGbUsed = hardwareInfo.ramUsedBytes / (1024.0 * 1024.0 * 1024.0)
                    val ramGbTotal = hardwareInfo.ramTotalBytes / (1024.0 * 1024.0 * 1024.0)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ACTIVE HEAP", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("${String.format("%.1f", hardwareInfo.appUsedRamBytes / (1024.0 * 1024.0))} MB", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TOTAL RAM", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("${String.format("%.1f", ramGbTotal)} GB", style = MaterialTheme.typography.titleMedium, color = TextWhite)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onRunOptimize,
                        enabled = !isOptimizing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("run_optimization_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = MatteBlack
                        )
                    ) {
                        if (isOptimizing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MatteBlack,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "PURGING INACTIVE MEMORY...",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "QUICK OPTIMIZE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // 3. Optimization Presets
        item {
            Text(
                text = "[ OPTIMIZATION PRESETS ]",
                style = MaterialTheme.typography.labelMedium,
                color = NeonCyan,
                fontWeight = FontWeight.Bold
            )
        }

        items(OptimizationPreset.entries.size) { index ->
            val preset = OptimizationPreset.entries[index]
            val isSelected = selectedPreset == preset
            val presetColor = Color(preset.colorHex)

            HudCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectPreset(preset) }
                    .testTag("preset_card_${preset.name}"),
                borderColor = if (isSelected) presetColor else DarkSurfaceBorder,
                backgroundColor = if (isSelected) DarkSurfaceElevated else DarkSurface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(presetColor.copy(alpha = 0.15f))
                            .border(1.dp, presetColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (preset) {
                                OptimizationPreset.BALANCED -> Icons.Default.Tune
                                OptimizationPreset.PERFORMANCE -> Icons.Default.Speed
                                OptimizationPreset.BATTERY_SAVER -> Icons.Default.BatterySaver
                                OptimizationPreset.CUSTOM -> Icons.Default.Settings
                            },
                            contentDescription = null,
                            tint = presetColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSelected) presetColor else TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "• ${preset.subtitle}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = preset.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }

                    if (isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = presetColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // 4. Custom Configuration Toggles
        item {
            Text(
                text = "[ RUNTIME SETTINGS ]",
                style = MaterialTheme.typography.labelMedium,
                color = NeonCyan,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            HudCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkSurface
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Toggle: Close launcher when launching game
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Close Launcher On Launch",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextWhite
                            )
                            Text(
                                text = "Finishes launcher activity to release foreground RAM when a game is launched.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = settings.isCloseLauncherOnLaunch,
                            onCheckedChange = onToggleCloseOnLaunch,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MatteBlack,
                                checkedTrackColor = NeonCyan,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = DarkSurfaceElevated
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Toggle: Aggressive memory trim
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Aggressive Cache Purging",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextWhite
                            )
                            Text(
                                text = "Purges in-memory bitmap allocations and calls ComponentCallbacks2.TRIM_MEMORY_COMPLETE.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = settings.isAggressiveTrimEnabled,
                            onCheckedChange = onToggleAggressiveTrim,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MatteBlack,
                                checkedTrackColor = NeonCyan,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = DarkSurfaceElevated
                            )
                        )
                    }
                }
            }
        }

        // 5. Last Optimization Log
        if (lastOptimizationResult != null) {
            item {
                Text(
                    text = "[ OPTIMIZATION LOGS ]",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                HudCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NeonGreen.copy(alpha = 0.4f),
                    backgroundColor = DarkSurfaceElevated
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("STATUS", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                "SUCCESS • RECLAIMED ${String.format("%.1f", lastOptimizationResult.launcherRamFreedMb)} MB",
                                style = MaterialTheme.typography.labelMedium,
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        lastOptimizationResult.actionsExecuted.forEach { action ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = action,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextWhite
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Security Notice
        item {
            HudCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = DarkSurfaceBorder,
                backgroundColor = DarkSurface
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ZX Game Launcher adheres strictly to Android security policies. It performs authentic JVM memory releases without fake root exploits or illegal system modifications.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}
