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
import fho.kdvs.track.TrackRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class Spotify @Inject constructor(
    private val trackRepository: TrackRepository,
    private val sharedViewModel: SharedViewModel
): CoroutineScope {
    private lateinit var mSpotifyAppRemote: SpotifyAppRemote

    private var job: Job? = null

    private val parentJob = Job()
    override val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.IO

    fun initializeSpotifyUri(track: TrackEntity) {
        val response = searchForTrack(track)
        val newSpotifyUri = parseSpotifyTrackUri(response)

        launch { trackRepository.updateTrackSpotifyUri(track?.trackId, newSpotifyUri) }
    }

    fun openSpotify(view: View, spotifyUri: String) {
        if (isSpotifyInstalledOnDevice(view)) {
            sharedViewModel.openSpotifyApp(view, spotifyUri)
        }
        else {
            val url = makeSpotifyUrl(spotifyUri)
            if (url.isNotEmpty())
                sharedViewModel.openBrowser(view, url)
        }
    }

    private fun isSpotifyInstalledOnDevice(view: View): Boolean {
        var isSpotifyInstalled = false

        try {
            view.context.packageManager.getPackageInfo("com.spotify.music", 0)
            isSpotifyInstalled = true
        } catch (e: PackageManager.NameNotFoundException) {}

        return isSpotifyInstalled
    }

    private fun makeSpotifyUrl(spotifyUri: String): String {
        var url = ""

        val re = "spotify:(\\w+):(.+)".toRegex().find(spotifyUri)
        val type = re?.groupValues?.getOrNull(1)
        val id = re?.groupValues?.getOrNull(2)

        if (!type.isNullOrEmpty() && !id.isNullOrEmpty())
            url = "https://open.spotify.com/$type/$id"

        return url
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

    private fun parseSpotifyTrackUri(json: JSONObject): String {
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

        // TODO : parse artwork if it's available

        return uri
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