package fho.kdvs.global.util

import org.jetbrains.anko.doAsync
import org.json.JSONObject
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL

object HttpHelper {
    /* Returns true if good HTTP request. */
    fun isConnectionAvailable(url: String?): Boolean {
        val con = URL(url).openConnection() as HttpURLConnection
        var response = HttpURLConnection.HTTP_BAD_REQUEST

        doAsync {
            con.connectTimeout = 5000
            con.connect()
            response = con.responseCode
        }

        return (response == HttpURLConnection.HTTP_OK)
    }

    fun makeGETRequest(url: String?): JSONObject {
        val restTemplate = RestTemplate(true)
        var response = "{}"

        try {
            if (!url.isNullOrEmpty())
                response = restTemplate.getForObject(url, String::class.java)
        } catch (e: Exception) {
            Timber.d("GET error $e")
        }

        return JSONObject(response)
    }

    fun makeParameterizedGETRequest(url: String?, request: HttpEntity<String>?): JSONObject {
        val restTemplate = RestTemplate(true)
        var response = "{}"

        try {
            if (!url.isNullOrEmpty() && request != null)
                response = restTemplate.exchange(url, HttpMethod.GET, request, String::class.java).body
        } catch (e: Exception) {
            Timber.d("GET error $e")
        }

        return JSONObject(response)
    }

    fun makePOSTRequest(url: String?, request: Any?): JSONObject {
        val restTemplate = RestTemplate(true)
        var response = "{}"

        try {
            Timber.d("Making POST $url")
            if (!url.isNullOrEmpty())
                response = restTemplate.postForObject(url, request, String::class.java)
        } catch (e: Exception) {
            Timber.d("POST error $e")
        }

        return JSONObject(response)
    }
}
