package fho.kdvs.global.web

import android.util.Base64
import fho.kdvs.global.extensions.urlEncoded
import fho.kdvs.global.util.HttpHelper
import fho.kdvs.global.util.Keys.SPOTIFY_CLIENT_ID
import fho.kdvs.global.util.Keys.SPOTIFY_CLIENT_SECRET
import fho.kdvs.global.util.URLs.SPOTIFY_SEARCH_URL
import fho.kdvs.global.util.URLs.SPOTIFY_TOKEN_URL
import kotlinx.serialization.json.Json
import org.json.JSONObject
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import timber.log.Timber


@kotlinx.serialization.UnstableDefault
object Spotify {
    // Client Credentials Flow
    fun search(query: String?): SpotifyData? { // TODO: fuzzy/dynamic search?
        Timber.d("Spotify search $query")

        val authentication = requestAuthentication()
        if (authentication != null && authentication.has("access_token")) {
            val token = authentication.getString("access_token")

            val headers = HttpHeaders()
            headers.set("Authorization", "Bearer $token")

            val url = "$SPOTIFY_SEARCH_URL$query"
            val request = HttpEntity<String>(headers)

            val response = HttpHelper.makeParameterizedGETRequest(url, request)
            response?.let {
                return Json.parse(SpotifyData.serializer(), response)
            }
        }

        return null
    }

    fun getAlbumQuery(album: String?, artist: String?): String? {
        return "album:${album.encode()} artist:${artist.encode()}&type=album&limit=1"
    }

    fun getTrackQuery(song: String?, artist: String?): String? {
        return ("track:${song.encode()} "
                + "artist:${artist.encode()}&type=track&limit=1")
    }

    private fun requestAuthentication(): JSONObject? {
        Timber.d("Requesting Spotify authentication")

        val body = LinkedMultiValueMap<String, String>()
        body.add("grant_type", "client_credentials")

        val encoded = Base64.encodeToString(
            "$SPOTIFY_CLIENT_ID:$SPOTIFY_CLIENT_SECRET".toByteArray(),
            Base64.NO_WRAP
        )

        val headers = HttpHeaders()
        headers.set("Authorization", "Basic $encoded")
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val request = HttpEntity(body, headers)
        return JSONObject(HttpHelper.makePOSTRequest(SPOTIFY_TOKEN_URL, request))
    }

    private fun String?.encode(): String {
        return this.urlEncoded
            .replace("+", " ")
    }
}