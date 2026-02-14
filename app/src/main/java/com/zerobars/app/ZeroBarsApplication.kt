package com.zerobars.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class ZeroBarsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Dependencies.init(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Network Alerts"
            val descriptionText = "Notifications when cellular network is lost"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("network_alert_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
