package fho.kdvs.global.util

import org.apache.commons.text.StringEscapeUtils
import org.jetbrains.anko.doAsync
import org.json.JSONObject
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

    private fun getResponse(url: String?): String {
        val restTemplate = RestTemplate(true)
        var response = "{}"

        try {
            response = restTemplate.getForObject(url, String::class.java)
        } catch (e: Exception) {}

        return response
    }

    fun getDocumentResponse(url: String?): Document {
        val response = HttpHelper.getResponse(url)

        val factory = DocumentBuilderFactory.newInstance()
        factory.isValidating = true
        factory.isIgnoringElementContentWhitespace = true

        val builder = factory.newDocumentBuilder()
        return builder.parse(response)
    }

    fun getJsonResponse(url: String?): JSONObject {
        return JSONObject(HttpHelper.getResponse(url))
    }

    fun String.htmlEncode(): String {
        return StringEscapeUtils.escapeHtml4(this)
            .replace(" ", "%20")
            .replace("&quot;", "%22")
    }
}
