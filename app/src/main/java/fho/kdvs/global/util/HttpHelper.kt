package fho.kdvs.global.util

import android.util.Base64
import org.apache.commons.text.StringEscapeUtils
import org.jetbrains.anko.doAsync
import org.json.JSONObject
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.web.client.RestTemplate
import org.w3c.dom.Document
import java.net.HttpURLConnection
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

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
        } catch (e: Exception) {}

        return JSONObject(response)
    }

    fun makePOSTRequest(url: String?, request: Any?): JSONObject {
        val restTemplate = RestTemplate(true)
        var response = "{}"

        try {
            if (!url.isNullOrEmpty())
                response = restTemplate.postForObject(url, request, String::class.java)
        } catch (e: Exception) {}

        return JSONObject(response)
    }
}
