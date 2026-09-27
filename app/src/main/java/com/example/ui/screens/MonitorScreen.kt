package com.example.ui.screens

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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.DeviceHardwareInfo
import com.example.ui.HudCard
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
fun MonitorScreen(
    hardwareInfo: DeviceHardwareInfo,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
                    text = "DEVICE MONITOR",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonCyan,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Real-Time Android Hardware Telemetry (Official APIs)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        // 2. RAM Dashboard
        item {
            val ramGbAvail = hardwareInfo.ramAvailableBytes / (1024.0 * 1024.0 * 1024.0)
            val ramGbTotal = hardwareInfo.ramTotalBytes / (1024.0 * 1024.0 * 1024.0)
            val ramGbUsed = hardwareInfo.ramUsedBytes / (1024.0 * 1024.0 * 1024.0)

            HudCard(
                modifier = Modifier.fillMaxWidth().testTag("monitor_ram_card"),
                borderColor = NeonCyan.copy(alpha = 0.4f),
                backgroundColor = DarkSurfaceElevated
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RANDOM ACCESS MEMORY (RAM)",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${hardwareInfo.ramUsedPercentage}% USED",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (hardwareInfo.ramUsedPercentage > 85) NeonOrange else NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { hardwareInfo.ramUsedPercentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (hardwareInfo.ramUsedPercentage > 85) NeonOrange else NeonCyan,
                        trackColor = DarkSurfaceBorder
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MonitorMetricItem("TOTAL", "${String.format("%.2f", ramGbTotal)} GB")
                        MonitorMetricItem("USED", "${String.format("%.2f", ramGbUsed)} GB")
                        MonitorMetricItem("AVAILABLE", "${String.format("%.2f", ramGbAvail)} GB")
                        MonitorMetricItem("LAUNCHER HEAP", "${String.format("%.1f", hardwareInfo.appUsedRamBytes / (1024.0 * 1024.0))} MB")
                    }
                }
            }
        }

        // 3. Battery Dashboard
        item {
            HudCard(
                modifier = Modifier.fillMaxWidth().testTag("monitor_battery_card"),
                borderColor = NeonGreen.copy(alpha = 0.4f),
                backgroundColor = DarkSurfaceElevated
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BatteryChargingFull,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BATTERY TELEMETRY",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${hardwareInfo.batteryPercentage}% • ${hardwareInfo.batteryStatus.uppercase()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { hardwareInfo.batteryPercentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = NeonGreen,
                        trackColor = DarkSurfaceBorder
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MonitorMetricItem("TEMPERATURE", "${String.format("%.1f", hardwareInfo.batteryTemperatureCelsius)}°C")
                        MonitorMetricItem("VOLTAGE", "${hardwareInfo.batteryVoltageMv} mV")
                        MonitorMetricItem("HEALTH", hardwareInfo.batteryHealth)
                        MonitorMetricItem("TECH", hardwareInfo.batteryTechnology)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { AppLauncher.openBatterySettings(context) },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("OPEN BATTERY SETTINGS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Storage Dashboard
        item {
            val storageGbUsed = hardwareInfo.storageUsedBytes / (1024.0 * 1024.0 * 1024.0)
            val storageGbTotal = hardwareInfo.storageTotalBytes / (1024.0 * 1024.0 * 1024.0)
            val storageGbAvail = hardwareInfo.storageAvailableBytes / (1024.0 * 1024.0 * 1024.0)

            HudCard(
                modifier = Modifier.fillMaxWidth().testTag("monitor_storage_card"),
                borderColor = NeonElectricBlue.copy(alpha = 0.4f),
                backgroundColor = DarkSurfaceElevated
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SdStorage,
                                contentDescription = null,
                                tint = NeonElectricBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "STORAGE (INTERNAL)",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${hardwareInfo.storageUsedPercentage}% OCCUPIED",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonElectricBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { hardwareInfo.storageUsedPercentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = NeonElectricBlue,
                        trackColor = DarkSurfaceBorder
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MonitorMetricItem("TOTAL", "${String.format("%.1f", storageGbTotal)} GB")
                        MonitorMetricItem("USED", "${String.format("%.1f", storageGbUsed)} GB")
                        MonitorMetricItem("AVAILABLE", "${String.format("%.1f", storageGbAvail)} GB")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { AppLauncher.openStorageSettings(context) },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, NeonElectricBlue.copy(alpha = 0.4f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonElectricBlue)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("OPEN STORAGE SETTINGS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 5. Display & Frame Performance
        item {
            HudCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonViolet.copy(alpha = 0.4f),
                backgroundColor = DarkSurfaceElevated
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = NeonViolet,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DISPLAY & REFRESH RATE",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MonitorMetricItem("RESOLUTION", hardwareInfo.screenResolution)
                        MonitorMetricItem("REFRESH RATE", "${hardwareInfo.refreshRateHz.toInt()} Hz")
                        MonitorMetricItem("DENSITY", "${hardwareInfo.screenDensityDpi} DPI")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    HudCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = DarkSurface,
                        borderColor = DarkSurfaceBorder
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = NeonViolet,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Android Security Note: Per-app third-party FPS measurement is restricted by Android OS without Root or ADB developer debugging permissions. Display panel refresh is ${hardwareInfo.refreshRateHz.toInt()}Hz.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        // 6. System & SoC Information
        item {
            HudCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = DarkSurfaceBorder,
                backgroundColor = DarkSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SYSTEM & HARDWARE SPECS",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MonitorMetricItem("MODEL", hardwareInfo.deviceModel)
                        MonitorMetricItem("VENDOR", hardwareInfo.deviceManufacturer)
                        MonitorMetricItem("OS", "${hardwareInfo.androidVersion} (API ${hardwareInfo.apiLevel})")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MonitorMetricItem("CHIPSET / BOARD", hardwareInfo.hardwareBoard)
                        MonitorMetricItem("CPU CORES", "${hardwareInfo.availableCpuCores} Cores")
                        MonitorMetricItem("CPU ABI", hardwareInfo.cpuAbi)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    MonitorMetricItem("SYSTEM UPTIME", hardwareInfo.systemUptimeFormatted)
                }
            }
        }
    }
}

@Composable
fun MonitorMetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.titleSmall, color = TextWhite, fontWeight = FontWeight.Bold)
    }
}
