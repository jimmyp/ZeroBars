package com.zerobars.app

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogEvent(
    val timestamp: Long,
    val message: String
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}

class EventLogger {
    private val _logs = MutableStateFlow<List<LogEvent>>(emptyList())
    val logs: StateFlow<List<LogEvent>> = _logs.asStateFlow()

    fun log(message: String) {
        val newEvent = LogEvent(System.currentTimeMillis(), message)
        _logs.value = listOf(newEvent) + _logs.value // Prepend to list
    }
    
    fun clearLogs() {
        _logs.value = emptyList()
    }
}
