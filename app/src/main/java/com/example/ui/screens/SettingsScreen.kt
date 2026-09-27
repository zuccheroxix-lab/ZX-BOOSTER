package com.example.ui.screens

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AppSettings
import com.example.ui.HudCard
import com.example.ui.NavigationTab
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MatteBlack
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.utils.AppLauncher
import com.example.utils.NotificationHelper

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onToggleHaptic: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleAutoScan: (Boolean) -> Unit,
    onToggleCloseOnLaunch: (Boolean) -> Unit,
    onToggleAggressiveTrim: (Boolean) -> Unit,
    onResetAllData: () -> Unit,
    onClearSessions: () -> Unit,
    onNavigateToTab: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Application Data", color = TextWhite) },
            text = { Text("This will reset all custom game presets, favorite flags, and session records back to initial defaults. Continue?", color = TextMuted) },
            containerColor = DarkSurfaceElevated,
            confirmButton = {
                Button(
                    onClick = {
                        onResetAllData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed)
                ) {
                    Text("Reset All", color = TextWhite)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = TextWhite)
                }
            }
        )
    }

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
                    text = "SETTINGS & PREFERENCES",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonCyan,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "ZX Game Launcher Configuration • by Zucchero Xann",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        // 2. Behavior Settings
        item {
            Text(
                text = "[ LAUNCHER BEHAVIOR ]",
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
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Haptic Feedback
                    SettingsToggleRow(
                        title = "Haptic Vibration Feedback",
                        description = "Provides tactile vibration on buttons and optimization triggers.",
                        isChecked = settings.isHapticEnabled,
                        onCheckedChange = onToggleHaptic
                    )

                    // Notifications
                    SettingsToggleRow(
                        title = "System HUD Notifications",
                        description = "Post session start, completion summaries, and optimization reports in notification shade.",
                        isChecked = settings.isNotificationsEnabled,
                        onCheckedChange = onToggleNotifications
                    )

                    // Auto Scan
                    SettingsToggleRow(
                        title = "Automatic Application Rescan",
                        description = "Automatically updates the game library when returning to launcher from other apps.",
                        isChecked = settings.isAutoScanEnabled,
                        onCheckedChange = onToggleAutoScan
                    )

                    // Close Launcher
                    SettingsToggleRow(
                        title = "Close Launcher On Launch",
                        description = "Finishes launcher activity after starting a game to save memory.",
                        isChecked = settings.isCloseLauncherOnLaunch,
                        onCheckedChange = onToggleCloseOnLaunch
                    )

                    // Aggressive Trim
                    SettingsToggleRow(
                        title = "Aggressive Memory Trimming",
                        description = "Forces JVM Garbage Collection and onTrimMemory(TRIM_MEMORY_COMPLETE).",
                        isChecked = settings.isAggressiveTrimEnabled,
                        onCheckedChange = onToggleAggressiveTrim
                    )
                }
            }
        }

        // 3. Permissions & System Access
        item {
            Text(
                text = "[ ANDROID SYSTEM PERMISSIONS ]",
                style = MaterialTheme.typography.labelMedium,
                color = NeonCyan,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            HudCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkSurfaceElevated,
                borderColor = NeonCyan.copy(alpha = 0.3f)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PermissionStatusRow(
                        name = "App Inventory (QUERY_ALL_PACKAGES)",
                        status = "Active / Granted",
                        isGranted = true
                    )

                    val hasNotif = NotificationHelper.hasNotificationPermission(context)
                    PermissionStatusRow(
                        name = "Notifications (POST_NOTIFICATIONS)",
                        status = if (hasNotif) "Active / Granted" else "Not Granted",
                        isGranted = hasNotif
                    )

                    PermissionStatusRow(
                        name = "Process Management (KILL_BACKGROUND_PROCESSES)",
                        status = "Active / Safe Mode",
                        isGranted = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { AppLauncher.openApplicationSettings(context) },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                        ) {
                            Text("App Settings", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = { AppLauncher.openUsageAccessSettings(context) },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, NeonViolet.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonViolet)
                        ) {
                            Text("Usage Access", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // 4. Data Management & Danger Zone
        item {
            Text(
                text = "[ DATA MANAGEMENT ]",
                style = MaterialTheme.typography.labelMedium,
                color = NeonRed,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            HudCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkSurface,
                borderColor = NeonRed.copy(alpha = 0.3f)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onClearSessions,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurfaceElevated,
                            contentColor = TextWhite
                        ),
                        border = BorderStroke(1.dp, DarkSurfaceBorder)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = NeonRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear Session History", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.fillMaxWidth().height(44.dp).testTag("reset_app_data_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonRed.copy(alpha = 0.15f),
                            contentColor = NeonRed
                        ),
                        border = BorderStroke(1.dp, NeonRed.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, tint = NeonRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset All Application Data", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 5. About ZX Game Launcher & Credits
        item {
            HudCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkSurfaceElevated,
                borderColor = DarkSurfaceBorder
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(36.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ZX GAME LAUNCHER",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "GAME LAUNCHER & DEVICE OPTIMIZER",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonCyan
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Developed by ZUCCHERO XANN",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Version 1.0.0 • Native Android Architecture\nKotlin • Jetpack Compose • Room Database • MVVM",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = TextWhite)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MatteBlack,
                checkedTrackColor = NeonCyan,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DarkSurfaceElevated
            )
        )
    }
}

@Composable
fun PermissionStatusRow(
    name: String,
    status: String,
    isGranted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, style = MaterialTheme.typography.labelSmall, color = TextWhite, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Info,
                contentDescription = null,
                tint = if (isGranted) NeonGreen else NeonRed,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                color = if (isGranted) NeonGreen else NeonRed,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
