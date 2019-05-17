package fho.kdvs.global.web

import android.content.pm.PackageManager
import android.util.Base64
import android.view.View
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.SpotifyAppRemote
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.extensions.urlEncoded
import fho.kdvs.global.util.HttpHelper
import fho.kdvs.global.util.Keys.SPOTIFY_CLIENT_ID
import fho.kdvs.global.util.Keys.SPOTIFY_CLIENT_SECRET
import fho.kdvs.global.util.URLs.SPOTIFY_REDIRECT_URI
import fho.kdvs.global.util.URLs.SPOTIFY_SEARCH_URL
import fho.kdvs.global.util.URLs.SPOTIFY_TOKEN_URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.json.JSONObject
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class Spotify @Inject constructor(
    private val sharedViewModel: SharedViewModel
): CoroutineScope {
    private lateinit var mSpotifyAppRemote: SpotifyAppRemote

    private val parentJob = Job()
    override val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.IO

    fun fetchSpotifyData(track: TrackEntity): TrackEntity {
        val response = searchForTrack(track)
        val topResult = parseSpotifyTrackSearchResponse(response)

        if (topResult.has("uri"))
            track.spotifyUri = topResult.getString("uri")
        else
            track.spotifyUri = ""

        if (topResult.has("album")) {
            val album = topResult.getJSONObject("album")

            if (track.imageHref.isNullOrEmpty()) {
                if (album.has("images")) {
                    val images = album.getJSONArray("images")
                    val topImage = images.getJSONObject(0)
                    if (topImage.has("url"))
                        track.imageHref = topImage.getString("url")
                }
            }

            if (track.album.isNullOrEmpty()) {
                if (album.has("name")) {
                    track.album = album.getString("name")
                }
            }

            if (track.year == null) {
                if (album.has("release_date")) {
                    val year = album.getString("release_date").toIntOrNull()
                    if (year != null)
                        track.year = year
                }
            }
        }

        return track
    }

    fun openSpotify(view: View, spotifyUri: String) {
        if (isSpotifyInstalledOnDevice(view))
            sharedViewModel.openSpotifyApp(view, spotifyUri)
        else
            sharedViewModel.onClickSpotifyNoApp(view, spotifyUri)
    }

    private fun isSpotifyInstalledOnDevice(view: View): Boolean {
        var isSpotifyInstalled = false

        try {
            view.context.packageManager.getPackageInfo("com.spotify.music", 0)
            isSpotifyInstalled = true
        } catch (e: PackageManager.NameNotFoundException) {}

        return isSpotifyInstalled
    }

    // TODO: setup playlist building
    fun authorizeUser() {
        val connectionParams = ConnectionParams.Builder(SPOTIFY_CLIENT_ID)
            .setRedirectUri(SPOTIFY_REDIRECT_URI)
            .showAuthView(true)
            .build()
    }

    private fun requestAuthentication(): JSONObject {
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
        return HttpHelper.makePOSTRequest(SPOTIFY_TOKEN_URL, request)
    }

    // Authorization Code Flow
    fun generatePlaylist() {

    }

    private fun searchForAlbum(artist: String, album: String): JSONObject {
        val query = getAlbumQuery(artist, album)
        return search(query)
    }

    private fun searchForTrack(track: TrackEntity): JSONObject {
        val query = getTrackQuery(track)
        return search(query)
    }

    // Client Credentials Flow
    private fun search(query: String?): JSONObject { // TODO: fuzzy/dynamic search
        Timber.d("Spotify search $query")

        var item = JSONObject("{}")

        val authentication = requestAuthentication()
        if (authentication.has("access_token")) {
            val token = authentication.getString("access_token")

            val headers = HttpHeaders()
            headers.set("Authorization", "Bearer $token")

            val url = "$SPOTIFY_SEARCH_URL$query"
            val request = HttpEntity<String>(headers)

            item = HttpHelper.makeParameterizedGETRequest(url, request)
        }

        return item
    }

    fun parseSpotifyAlbumUri(json: JSONObject): String {
        var uri = ""

        if (json.has("albums")) {
            val tracks = json.getJSONObject("albums")
            if (tracks.has("items")) {
                val items = tracks.getJSONArray("items")
                if (items.length() > 0) {
                    val topResult = items.getJSONObject(0)
                    if (topResult.has("id"))
                        uri = "spotify:album:" + topResult.getString("id")
                }
            }
        }

        return uri
    }

    private fun parseSpotifyTrackSearchResponse(json: JSONObject): JSONObject {
        var topResult = JSONObject("{}")

        if (json.has("tracks")) {
            val tracks = json.getJSONObject("tracks")
            if (tracks.has("items")) {
                val items = tracks.getJSONArray("items")
                if (items.length() > 0) {
                    topResult = items.getJSONObject(0)
                }
            }
        }

        return topResult
    }

    private fun getAlbumQuery(artist: String, album: String): String {
        return "album:${album.encode()} artist:${artist.encode()}&type=album&limit=1"
    }

    private fun getTrackQuery(track: TrackEntity): String {
        return ("track:${track.song.encode()} "
            + "artist:${track.artist.encode()}&type=track&limit=1")
    }

    private fun String?.encode(): String {
        return this.urlEncoded
            .replace("+", " ")
    }
}