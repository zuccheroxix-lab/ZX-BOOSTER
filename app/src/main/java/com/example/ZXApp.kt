package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.database.ZXDatabase

class ZXApp : Application() {

    lateinit var database: ZXDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = ZXDatabase.getDatabase(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = NOTIFICATION_CHANNEL_ID
            val name = "ZX Game Launcher Alerts"
            val descriptionText = "Notifications for game sessions and device optimization summaries"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                enableVibration(false)
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "zx_game_launcher_channel"
        lateinit var instance: ZXApp
            private set
    }
}
