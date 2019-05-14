package fho.kdvs.global.web

import android.util.Base64
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.SpotifyAppRemote
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.extensions.urlEncoded
import fho.kdvs.global.util.HttpHelper
import org.json.JSONObject
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import timber.log.Timber

enum class SearchType {
    ALBUM, TRACK
}

object Spotify {
    private const val CLIENT_ID = "7f214830ba524ae1a8b1b8181ad4c2a4"
    private const val CLIENT_SECRET = "70f0fbddfb38481b8b604fa920a230ce"
    private const val REDIRECT_URI = "http://com.yourdomain.yourapp/callback" // TODO: set redirect

    private const val SEARCH_URL = "https://api.spotify.com/v1/search?q="
    private const val TOKEN_URL = "https://accounts.spotify.com/api/token"

    private lateinit var mSpotifyAppRemote: SpotifyAppRemote

    fun authorizeUser() {
        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()
    }

    private fun requestAuthentication(): JSONObject {
        Timber.d("Requesting Spotify authentication")

        val body = LinkedMultiValueMap<String, String>()
        body.add("grant_type", "client_credentials")

        val encoded = Base64.encodeToString(
            "$CLIENT_ID:$CLIENT_SECRET".toByteArray(),
            Base64.NO_WRAP
        )

        val headers = HttpHeaders()
        headers.set("Authorization", "Basic $encoded")
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val request = HttpEntity(body, headers)
        return HttpHelper.makePOSTRequest(TOKEN_URL, request)
    }

    // Authorization Code Flow
    fun generatePlaylist() {

    }


    fun searchForAlbum(artist: String, album: String): JSONObject {
        val query = getAlbumQuery(artist, album)
        return search(query)
    }

    fun searchForTrack(track: TrackEntity): JSONObject {
        val query = getTrackQuery(track)
        return search(query)
    }

    // Client Credentials Flow
    private fun search(query: String?): JSONObject {
        Timber.d("Spotify search $query")

        var item = JSONObject("{}")

        val authentication = requestAuthentication()
        if (authentication.has("access_token")) {
            val token = authentication.getString("access_token")

            val headers = HttpHeaders()
            headers.set("Authorization", "Bearer $token")

            val url = "$SEARCH_URL$query"
            val request = HttpEntity<String>(headers)

            item = HttpHelper.makeParameterizedGETRequest(url, request)
        }

        return item
    }

    fun parseSpotifyTrackUri(json: JSONObject): String {
        var uri = ""

        if (json.has("tracks")) {
            val tracks = json.getJSONObject("tracks")
            if (tracks.has("items")) {
                val items = tracks.getJSONArray("items")
                if (items.length() > 0) {
                    val topResult = items.getJSONObject(0)
                    if (topResult.has("id"))
                        uri = "spotify:track:" + topResult.getString("id")
                }
            }
        }

        return uri
    }

    private fun getAlbumQuery(artist: String, album: String): String {
        return "album:${album.urlEncoded}%20artist:${artist.urlEncoded}&type=album&limit=1"
    }

    private fun getTrackQuery(track: TrackEntity): String {
        return "track:${track.song.urlEncoded} artist:${track.artist.urlEncoded}&type=track&limit=1"
    }
}