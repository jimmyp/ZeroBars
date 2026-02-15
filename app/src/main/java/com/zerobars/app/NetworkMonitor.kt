package com.zerobars.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.telephony.PhoneStateListener
import android.telephony.ServiceState
import android.telephony.TelephonyManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkMonitor(
    context: Context,
    private val logger: EventLogger
) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    private val _isConnected = MutableStateFlow(false) // Default to false, let updates set true
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val phoneStateListener = object : PhoneStateListener() {
        override fun onServiceStateChanged(serviceState: ServiceState?) {
            super.onServiceStateChanged(serviceState)
            val isServiceAvailable = serviceState?.state == ServiceState.STATE_IN_SERVICE

            if (isServiceAvailable != _isConnected.value) {
                _isConnected.value = isServiceAvailable
                if (isServiceAvailable) {
                    Log.d("NetworkMonitor", "Cellular Service Restored")
                    logger.log("Cellular Connection Restored")
                } else {
                    Log.d("NetworkMonitor", "Cellular Service Lost")
                    logger.log("Cellular Connection Lost!")
                }
            }
        }
    }

    fun startMonitoring() {
        if (_isMonitoring.value) return
        
        try {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE)

            // The listener will fire immediately with the current state, so no need for synchronous call
            // which requires Location permissions on some API levels.

            _isMonitoring.value = true
            logger.log("Monitoring Started")
        } catch (e: Exception) {
            Log.e("NetworkMonitor", "Error starting monitoring", e)
            logger.log("Error starting monitoring: ${e.message}")
        }
    }

    fun stopMonitoring() {
        if (!_isMonitoring.value) return

        try {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
            _isMonitoring.value = false
            logger.log("Monitoring Stopped")
        } catch (e: Exception) {
            // Error stopping
        }
    }
}
