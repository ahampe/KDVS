package fho.kdvs.global.util

import org.json.JSONObject
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL

object HttpHelper {  // TODO: retry on connection fail?
    /** Returns true if good HTTP request. Wrap in async block. */
    fun isConnectionAvailable(url: String?): Boolean {
        val con = URL(url).openConnection() as HttpURLConnection
        con.connectTimeout = 500
        con.connect()

        return (con.responseCode == HttpURLConnection.HTTP_OK)
    }

    fun makeGETRequest(url: String?): JSONObject {
        val restTemplate = RestTemplate(true)
        var response = "{}"

        try {
            if (!url.isNullOrEmpty()) {
                Timber.d("GET request $url")
                response = restTemplate.getForObject(url, String::class.java)
            }
        } catch (e: Exception) {
            Timber.d("GET error $e")
        }

        return JSONObject(response)
    }

    fun makeParameterizedGETRequest(url: String?, request: HttpEntity<String>?): JSONObject {
        val restTemplate = RestTemplate(true)
        var response = "{}"

        try {
            if (!url.isNullOrEmpty() && request != null) {
                Timber.d("GET request $url")
                response = restTemplate.exchange(url, HttpMethod.GET, request, String::class.java).body
            }
        } catch (e: Exception) {
            Timber.d("GET error $e")
        }

        return JSONObject(response)
    }

    fun makePOSTRequest(url: String?, request: Any?): JSONObject {
        val restTemplate = RestTemplate(true)
        var response = "{}"

        try {
            if (!url.isNullOrEmpty()) {
                Timber.d("Making POST $url")
                response = restTemplate.postForObject(url, request, String::class.java)
            }
        } catch (e: Exception) {
            Timber.d("POST error $e")
        }

        return JSONObject(response)
    }
}
