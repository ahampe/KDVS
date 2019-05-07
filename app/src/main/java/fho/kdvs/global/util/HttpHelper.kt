package fho.kdvs.global.util

import org.jetbrains.anko.doAsync
import java.net.HttpURLConnection
import java.net.URL

object HttpHelper {
    /* Returns true if good HTTP request. */
    fun isConnectionAvailable(url: String?): Boolean{
        val con = URL(url).openConnection() as HttpURLConnection
        var response = HttpURLConnection.HTTP_BAD_REQUEST

        doAsync {
            con.connectTimeout = 5000
            con.connect()
            response = con.responseCode
        }

        return (response == HttpURLConnection.HTTP_OK)
    }


}
