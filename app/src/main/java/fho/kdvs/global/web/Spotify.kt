package fho.kdvs.global.web

import android.util.Base64
import fho.kdvs.global.extensions.urlEncoded
import fho.kdvs.global.util.HttpHelper
import fho.kdvs.global.util.HttpHelper.EMPTY_RESPONSE
import fho.kdvs.global.util.Keys.SPOTIFY_CLIENT_ID
import fho.kdvs.global.util.Keys.SPOTIFY_CLIENT_SECRET
import fho.kdvs.global.util.URLs.SPOTIFY_SEARCH_URL
import fho.kdvs.global.util.URLs.SPOTIFY_TOKEN_URL
import org.json.JSONObject
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import timber.log.Timber

class SpotifyData(
    val spotifyUri: String?,
    override val albumTitle: String?,
    override val imageHref: String?,
    override val year: Int?
): ThirdPartyData()

abstract class Spotify: IThirdPartyMusicAPI {

    abstract override fun getMusicData(title: String?, artist: String?): SpotifyData?

    // Client Credentials Flow
    protected fun search(query: String?): JSONObject { // TODO: fuzzy/dynamic search?
        Timber.d("Spotify search $query")

        var item = JSONObject(EMPTY_RESPONSE)

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

    protected fun parseTopResultFromAlbumResponse(json: JSONObject?): JSONObject? {
        json?.let {
            if (json.has("albums")) {
                val tracks = json.getJSONObject("albums")
                if (tracks.has("items")) {
                    val items = tracks.getJSONArray("items")
                    if (items.length() > 0) {
                        return items.getJSONObject(0)
                    }
                }
            }
        }

        return null
    }

    protected fun parseTopResultFromTrackResponse(json: JSONObject?): JSONObject? {
        json?.let {
            if (json.has("tracks")) {
                val tracks = json.getJSONObject("tracks")
                if (tracks.has("items")) {
                    val items = tracks.getJSONArray("items")
                    if (items.length() > 0) {
                        return items.getJSONObject(0)
                    }
                }
            }
        }

        return null
    }

    protected fun getDataFromTopResult(topResult: JSONObject?): SpotifyData? {
        val uri = JsonHelper.getRootLevelElmOfType<String>("uri", topResult)
        val albumObj = JsonHelper.getRootLevelElmOfType<JSONObject>("album", topResult)
        val albumTitle = JsonHelper.getRootLevelElmOfType<String>("name", albumObj)
        val year = JsonHelper.getRootLevelElmOfType<String>("release_date", topResult)
            ?.toIntOrNull()
        val imageHref = getImageHrefFromAlbumObj(albumObj)


        return SpotifyData(uri, albumTitle, imageHref, year)
    }

    protected fun getAlbumQuery(album: String, artist: String): String {
        return "album:${album.encode()} artist:${artist.encode()}&type=album&limit=1"
    }

    protected fun getTrackQuery(song: String, artist: String): String {
        return ("track:${song.encode()} "
                + "artist:${artist.encode()}&type=track&limit=1")
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

    private fun getImageHrefFromAlbumObj(json: JSONObject?): String? {
        json?.let {
            if (json.has("images")) {
                val images = json.getJSONArray("images")
                val topImage = images.getJSONObject(0)
                if (topImage.has("url"))
                    return topImage.getString("url")
            }
        }

        return null
    }

    private fun String?.encode(): String {
        return this.urlEncoded
            .replace("+", " ")
    }
}

class SpotifyAlbum: Spotify() {
    override fun getMusicData(title: String?, artist: String?): SpotifyData? {
        if (title.isNullOrEmpty() || artist.isNullOrEmpty())
            return null

        val response = search(getAlbumQuery(title, artist))

        val topResult = parseTopResultFromAlbumResponse(response)

        return getDataFromTopResult(topResult)
    }
}

class SpotifySong: Spotify() {
    override fun getMusicData(title: String?, artist: String?): SpotifyData? {
        if (title.isNullOrEmpty() || artist.isNullOrEmpty())
            return null

        val response = search(getTrackQuery(title, artist))

        val topResult = parseTopResultFromTrackResponse(response)

        return getDataFromTopResult(topResult)
    }
}