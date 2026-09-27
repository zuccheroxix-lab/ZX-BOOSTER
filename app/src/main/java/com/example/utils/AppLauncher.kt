package com.example.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

object AppLauncher {

    fun launchApp(
        context: Context,
        packageName: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ): Boolean {
        return try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                onSuccess()
                true
            } else {
                val errorMsg = "Could not find launchable Activity for package: $packageName"
                onError(errorMsg)
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                false
            }
        } catch (e: ActivityNotFoundException) {
            val errorMsg = "App is not installed or has been removed: ${e.localizedMessage}"
            onError(errorMsg)
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            false
        } catch (e: SecurityException) {
            val errorMsg = "Security restriction prevented launching: ${e.localizedMessage}"
            onError(errorMsg)
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            false
        } catch (e: Exception) {
            val errorMsg = "Error launching app: ${e.localizedMessage}"
            onError(errorMsg)
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun openAppInfo(context: Context, packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open App Info: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestUninstall(context: Context, packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                data = Uri.parse("package:$packageName")
                putExtra(Intent.EXTRA_RETURN_RESULT, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback for older or newer Android variants
            try {
                val intent = Intent(Intent.ACTION_DELETE).apply {
                    data = Uri.parse("package:$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (err: Exception) {
                Toast.makeText(context, "Cannot open uninstall flow: ${err.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openBatterySettings(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (err: Exception) {
                Toast.makeText(context, "Cannot open Battery Settings: ${err.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openStorageSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_STORAGE_VOLUME_ACCESS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (err: Exception) {
                Toast.makeText(context, "Cannot open Storage Settings: ${err.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openApplicationSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open App Settings: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openDisplaySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open Display Settings: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openNotificationSettings(context: Context, packageName: String? = null) {
        try {
            val intent = if (packageName != null) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            openAppInfo(context, packageName ?: context.packageName)
        }
    }

    fun openNetworkSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallback = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
            } catch (err: Exception) {
                Toast.makeText(context, "Cannot open Network Settings: ${err.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openIgnoreBatteryOptimizations(context: Context, packageName: String? = null) {
        try {
            val targetPkg = packageName ?: context.packageName
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            openBatterySettings(context)
        }
    }

    fun openUsageAccessSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open Usage Access Settings: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
