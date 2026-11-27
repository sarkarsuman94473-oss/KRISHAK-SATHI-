package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.data.model.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NetworkStatusSnapshot(
    val isOnline: Boolean = true,
    val transportType: String = "UNKNOWN",
    val isValidatedInternet: Boolean = false
) {
    fun getBadgeText(lang: AppLanguage): String = if (isOnline) {
        when (lang) {
            AppLanguage.BENGALI -> "অনলাইন"
            AppLanguage.HINDI -> "ऑनलाइन"
            AppLanguage.ENGLISH -> "Online"
        }
    } else {
        when (lang) {
            AppLanguage.BENGALI -> "অফলাইন মোড"
            AppLanguage.HINDI -> "ऑफ़लाइन मोड"
            AppLanguage.ENGLISH -> "Offline Mode"
        }
    }

    fun getTransportLabel(lang: AppLanguage): String = when (transportType) {
        "WIFI" -> when (lang) {
            AppLanguage.BENGALI -> "Wi-Fi সংযোগ সক্রিয়"
            AppLanguage.HINDI -> "Wi-Fi कनेक्शन सक्रिय"
            AppLanguage.ENGLISH -> "Wi-Fi connected"
        }
        "CELLULAR" -> when (lang) {
            AppLanguage.BENGALI -> "মোবাইল ডেটা সক্রিয়"
            AppLanguage.HINDI -> "मोबाइल डेटा सक्रिय"
            AppLanguage.ENGLISH -> "Mobile data active"
        }
        "ETHERNET" -> when (lang) {
            AppLanguage.BENGALI -> "ইথারনেট / ব্রডব্যান্ড"
            AppLanguage.HINDI -> "ईथरनेट / ब्रॉडबैंड"
            AppLanguage.ENGLISH -> "Ethernet / Broadband"
        }
        else -> if (isOnline) {
            when (lang) {
                AppLanguage.BENGALI -> "ইন্টারনেট সংযোগ সক্রিয়"
                AppLanguage.HINDI -> "इंटरनेट कनेक्शन सक्रिय"
                AppLanguage.ENGLISH -> "Network connected"
            }
        } else {
            when (lang) {
                AppLanguage.BENGALI -> "কোনো সক্রিয় ইন্টারনেট সংযোগ নেই — অফলাইন ক্যাশ ব্যবহার হচ্ছে"
                AppLanguage.HINDI -> "इंटरनेट उपलब्ध नहीं — स्थानीय ऑफ़लाइन डेटा सक्रिय"
                AppLanguage.ENGLISH -> "No internet connection — using saved offline Room cache"
            }
        }
    }
}

/**
 * Singleton live network connectivity observer using Android ConnectivityManager.
 * Safe in Robolectric & headless unit tests.
 */
object NetworkConnectivityMonitor {

    private val _status = MutableStateFlow(NetworkStatusSnapshot())
    val status: StateFlow<NetworkStatusSnapshot> = _status.asStateFlow()

    @Volatile
    private var registered = false

    fun isCurrentlyOnline(): Boolean = _status.value.isOnline

    fun refreshNow(context: Context): NetworkStatusSnapshot {
        val snapshot = readCurrentSnapshot(context)
        _status.value = snapshot
        return snapshot
    }

    fun startMonitoring(context: Context) {
        refreshNow(context)
        if (registered) return
        val cm = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _status.value = readCurrentSnapshot(context)
            }

            override fun onLost(network: Network) {
                _status.value = readCurrentSnapshot(context)
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                _status.value = evaluateCapabilities(networkCapabilities)
            }
        }
        runCatching {
            cm.registerDefaultNetworkCallback(callback)
            registered = true
        }.onFailure {
            runCatching {
                val request = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
                cm.registerNetworkCallback(request, callback)
                registered = true
            }
        }
    }

    /**
     * Allows unit tests to assert UI reactions to online/offline transitions cleanly.
     */
    internal fun setSnapshotForTesting(snapshot: NetworkStatusSnapshot) {
        _status.value = snapshot
    }

    private fun readCurrentSnapshot(context: Context): NetworkStatusSnapshot {
        return try {
            val cm = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return NetworkStatusSnapshot(isOnline = false, transportType = "NONE", isValidatedInternet = false)
            val active = cm.activeNetwork
                ?: return NetworkStatusSnapshot(isOnline = false, transportType = "NONE", isValidatedInternet = false)
            val caps = cm.getNetworkCapabilities(active)
                ?: return NetworkStatusSnapshot(isOnline = false, transportType = "NONE", isValidatedInternet = false)
            evaluateCapabilities(caps)
        } catch (_: Exception) {
            NetworkStatusSnapshot(isOnline = true, transportType = "UNKNOWN", isValidatedInternet = false)
        }
    }

    private fun evaluateCapabilities(caps: NetworkCapabilities): NetworkStatusSnapshot {
        val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val validated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val transport = when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
            else -> if (hasInternet) "OTHER" else "NONE"
        }
        return NetworkStatusSnapshot(
            isOnline = hasInternet,
            transportType = transport,
            isValidatedInternet = validated
        )
    }
}
