package fho.kdvs.global.util

import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL

object HttpHelper {  // TODO: retry on connection fail?
    /** Returns true if good HTTP request. Wrap in async block. */
    fun isConnectionAvailable(url: String?): Boolean {
        val con = URL(url).openConnection() as HttpURLConnection
        con.connectTimeout = 500

        try {
            con.connect()
        } catch (e: Exception) {
            Timber.d("Connection failed $e")
            return false
        }

        return (con.responseCode == HttpURLConnection.HTTP_OK)
    }
}
