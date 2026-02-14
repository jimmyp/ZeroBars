package com.zerobars.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
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

    private val _isConnected = MutableStateFlow(true) // Optimistic default
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val availableNetworks = mutableSetOf<Network>()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            synchronized(availableNetworks) {
                val wasEmpty = availableNetworks.isEmpty()
                availableNetworks.add(network)
                if (wasEmpty) {
                    _isConnected.value = true
                    Log.d("NetworkMonitor", "Cellular Connected")
                    logger.log("Cellular Connection Restored")
                }
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            synchronized(availableNetworks) {
                availableNetworks.remove(network)
                if (availableNetworks.isEmpty()) {
                    _isConnected.value = false
                    Log.d("NetworkMonitor", "Cellular Lost")
                    logger.log("Cellular Connection Lost!")
                }
            }
        }
    }

    fun startMonitoring() {
        if (_isMonitoring.value) return
        
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        try {
            // Registering callback. Note: This does not immediately return current state
            // unless we also check active networks, but callback usually fires onAvailable immediately for existing networks.
            connectivityManager.registerNetworkCallback(request, networkCallback)
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
            connectivityManager.unregisterNetworkCallback(networkCallback)
            _isMonitoring.value = false
            logger.log("Monitoring Stopped")
        } catch (e: Exception) {
            // Already unregistered or not registered
        }
    }
}
