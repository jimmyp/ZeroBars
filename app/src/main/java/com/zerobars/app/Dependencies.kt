package com.zerobars.app

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object Dependencies {
    lateinit var eventLogger: EventLogger
    lateinit var networkMonitor: NetworkMonitor

    fun init(context: Context) {
        eventLogger = EventLogger()
        networkMonitor = NetworkMonitor(context, eventLogger)
    }
}
