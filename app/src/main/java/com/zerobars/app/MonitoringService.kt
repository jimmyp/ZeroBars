package com.zerobars.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MonitoringService : Service() {

    private var serviceJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    
    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "network_alert_channel"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoringService()
            ACTION_STOP -> stopMonitoringService()
        }
        return START_STICKY
    }

    private fun startMonitoringService() {
        if (serviceJob?.isActive == true) return // Already running

        // Start Foreground immediately
        val notification = createNotification(isConnected = true)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        Dependencies.networkMonitor.startMonitoring()

        serviceJob = serviceScope.launch {
            Dependencies.networkMonitor.isConnected.collectLatest { isConnected ->
                updateNotification(isConnected)
            }
        }
    }

    private fun stopMonitoringService() {
        Dependencies.networkMonitor.stopMonitoring()
        serviceJob?.cancel()
        serviceJob = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateNotification(isConnected: Boolean) {
        val notification = createNotification(isConnected)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotification(isConnected: Boolean): Notification {
        val title = if (isConnected) "ZeroBars is Watching" else "⚠️ NO SIGNAL!"
        val text = if (isConnected) "Monitoring cellular connection..." else "You have lost cellular connection!"
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setPriority(if (isConnected) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            
        // If lost, ensure it's noisy
        if (!isConnected) {
            builder.setVibrate(longArrayOf(0, 500, 200, 500))
        }

        return builder.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob?.cancel()
    }
}
