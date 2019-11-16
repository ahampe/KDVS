package fho.kdvs.global.web

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import timber.log.Timber
import java.net.InetAddress
import java.net.URI
import javax.inject.Inject

class ConnectionManager @Inject constructor(
    val application: Application
) {
    fun canConnectToServer(url: String) =
        isDeviceOnline(application.applicationContext) && isServerReachable(url)

    private fun isDeviceOnline(context: Context): Boolean {
        try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager
                .getNetworkCapabilities(network)

            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            Timber.e("Error checking device connectivity: $e")

            return false
        }
    }

    private fun getHostName(url: String): String? {
        val hostname = URI(url).host
        return if (hostname != null) {
            if (hostname.startsWith("www."))
                hostname.substring(4)
            else hostname
        } else hostname
    }

    private fun isServerReachable(url: String): Boolean {
        val hostname = getHostName(url) ?: return false

        return try {
            InetAddress.getByName(hostname).isReachable(1000)
        } catch (e: Exception) {
            Timber.e(e)

            false
        }
    }
}

