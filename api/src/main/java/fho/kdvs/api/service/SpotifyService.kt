package fho.kdvs.api.service

import android.accounts.NetworkErrorException
import fho.kdvs.api.endpoint.SpotifyEndpoint
import fho.kdvs.api.mapped.*
import fho.kdvs.api.mapper.SpotifyMapper
import fho.kdvs.base.urlEncoded
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.supervisorScope
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Client-facing singleton that wraps the Retrofit endpoint for Spotify API requests. Automatically
 * handles client authentication. User SSO authentication (for e.g. playlist creation) uses external
 * Spotify auth API.
 */
@Singleton
class SpotifyService @Inject constructor(
    private val endpoint: SpotifyEndpoint
) : BaseService() {

    private val mapper = SpotifyMapper()
    private var clientToken: String = ""

    /**
     * Returns first search result (if any) for an album query.
     */
    suspend fun findAlbumAsync(
        album: String?,
        artist: String?
    ): Deferred<SpotifySimpleAlbum?>? = supervisorScope {
        try {
            async {
                if (album.isNullOrBlank()) return@async null

                val query = makeSearchQuery(SearchType.ALBUM, album, artist)
                val response = endpoint
                    .searchAlbums(query = query, authHeader = makeClientAuthHeader())

                if (response.isSuccessful) {
                    mapper.album(response.body())
                } else {
                    when (response.code()) {
                        401 -> {
                            // Auth failed. Retry it
                            val auth = endpoint.authorizeApp()
                            val newResponse = auth.body()?.token?.let { token ->
                                clientToken = token
                                endpoint.searchAlbums(
                                    query = query,
                                    authHeader = makeClientAuthHeader()
                                ).body()
                            }
                            return@async newResponse?.let { mapper.album(it) }
                        }
                        429 -> {
                            // TODO untested
                            // Hit rate limit. Wait for specified span of time and try again recursively
                            return@async response.headers().get("Retry-After")?.toIntOrNull()
                                ?.let { seconds ->
                                    val millis = seconds * 1000L
                                    delay(millis)
                                    val search = findAlbumAsync(album, artist)
                                    search?.await()
                                }
                        }
                        else -> {
                            Timber.d("Error searching for album given $album and $artist")
                            Timber.d(response.message())
                            response.errorBody()?.toString()?.let { Timber.d(it) }
                            return@async null
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e("Error finding Spotify album: $e")
            null
        }
    }

    /**
     * Returns first search result (if any) for a track query.
     */
    suspend fun findTrackAsync(
        track: String?,
        artist: String?
    ): Deferred<SpotifyTrack?>? = supervisorScope {
        try {
            async {
                if (track.isNullOrBlank()) return@async null

                val query = makeSearchQuery(SearchType.TRACK, track, artist)
                val response =
                    endpoint.searchTracks(query = query, authHeader = makeClientAuthHeader())
                if (response.isSuccessful) {
                    mapper.track(response.body())
                } else {
                    when (response.code()) {
                        401 -> {
                            // Auth failed. Retry it
                            val auth = endpoint.authorizeApp()
                            val newResponse = auth.body()?.token?.let { token ->
                                clientToken = token
                                endpoint.searchTracks(
                                    query = query,
                                    authHeader = makeClientAuthHeader()
                                ).body()
                            }
                            return@async newResponse?.let { mapper.track(it) }
                        }
                        429 -> {
                            // TODO untested
                            // Hit rate limit. Wait for specified span of time and try again recursively
                            return@async response.headers().get("Retry-After")?.toIntOrNull()
                                ?.let { seconds ->
                                    val millis = seconds * 1000L
                                    delay(millis)
                                    val search = findTrackAsync(track, artist)
                                    search?.await()
                                }
                        }
                        else -> {
                            Timber.d("Error searching for track given $track and $artist")
                            Timber.d(response.message())
                            response.errorBody()?.toString()?.let { Timber.d(it) }
                            return@async null
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e("Error finding Spotify track: $e")
            null
        }
    }

    /**
     * Gets the corresponding [SpotifySimpleAlbum] from an album ID.
     */
    suspend fun getAlbumFromIDAsync(id: String?): Deferred<SpotifyAlbum?>? = supervisorScope {
        try {
            async {
                if (id.isNullOrBlank()) return@async null

                val response = endpoint.getAlbum(id = id, authHeader = makeClientAuthHeader())

                if (response.isSuccessful) {
                    mapper.album(response.body())
                } else return@async null
            }
        } catch (e: Exception) {
            Timber.e("Error getting Spotify album from ID: $e")
            null
        }

    }

    /**
     * Returns playlist, if any, in user's playlists corresponding to uri.
     */
    suspend fun getPlaylistFromUserAsync(
        uri: String,
        token: String
    ): Deferred<SpotifyPlaylist?>? = supervisorScope {
        try {
            async {
                val response = endpoint.getPlaylists(auth = makeAuthHeader(token))

                if (response.isSuccessful) {
                    return@async mapper.playlists(response.body())?.firstOrNull {
                        it?.uri == uri
                    }
                } else return@async null
            }
        } catch (e: Exception) {
            Timber.e("Error getting Spotify playlist from user: $e")
            null
        }
    }

    /**
     * Attempts to locate a playlist in user's Spotify account matching a given title, to prevent
     * the creation of duplicate playlists.
     */
    suspend fun getSpotifyPlaylistFromTitleAsync(
        title: String,
        token: String
    ): Deferred<SpotifyPlaylist?>? = supervisorScope {
        try {
            async {
                val response = endpoint.getPlaylists(auth = makeAuthHeader(token))

                if (response.isSuccessful) {
                    val playlists = mapper.playlists(response.body())

                    return@async playlists?.firstOrNull {
                        it?.name == title
                    }
                } else return@async null
            }
        } catch (e: Exception) {
            Timber.e("Error getting Spotify playlist from title: $e")
            null
        }
    }

    /**
     * Create Spotify playlist with specified title for user of corresponding token.
     */
    suspend fun createPlaylistAsync(
        title: String,
        token: String
    ): Deferred<SpotifyPlaylist?>? = supervisorScope {
        try {
            async {
                val profile = getUserProfileAsync(token)?.await()

                profile?.let {
                    val url = getUserPlaylistsUrl(profile.id)

                    val body = JSONObject()
                    body.put("name", title)
                    body.put("public", true)
                    body.put("description", "Generated by KDVS App on Android.")

                    val req =
                        RequestBody.create(MediaType.parse("application/json"), body.toString())

                    val response =
                        endpoint.createPlaylist(url = url, body = req, auth = makeAuthHeader(token))
                    if (response.isSuccessful) {
                        return@async mapper.playlist(response.body())
                    } else {
                        Timber.e("Error creating Spotify playlist")// TODO errors + retry
                    }
                }

                return@async null
            }
        } catch (e: Exception) {
            Timber.e("Error creating Spotify playlist: $e")
            null
        }
    }

    suspend fun getTracksInPlaylistAsync(
        playlistId: String,
        token: String,
        offset: Int = 0
    ): Deferred<List<SpotifyTrack?>?>? = supervisorScope {
        try {
            async {
                val response = endpoint.getTracksInPlaylist(
                    auth = makeAuthHeader(token),
                    id = playlistId,
                    offset = offset
                )

                if (response.isSuccessful) {
                    return@async mapper.tracks(response.body())
                } else {
                    when (response.code()) {
                        403 -> {
                            Timber.d("Playlist's tracks GET forbidden.")
                            Timber.d(response.message())
                            response.errorBody()?.toString()?.let { Timber.d(it) }
                        }
                        else -> {
                            Timber.d("Error getting playlist's tracks.")
                            Timber.d(response.message())
                            response.errorBody()?.toString()?.let { Timber.d(it) }
                        }
                    }
                }

                return@async null
            }
        } catch (e: Exception) {
            Timber.e("Error getting Spotify tracks from playlist: $e")
            null
        }
    }

    /**
     * Adds music from a list of Spotify track URIs to a playlist of a given ID.
     * Takes a max of 100 tracks at a time.
     */
    suspend fun addTracksToPlaylistAsync(
        uris: List<String>,
        playlistId: String,
        token: String
    ): Deferred<Boolean>? = supervisorScope {
        try {
            async {
                val body = JSONObject()
                body.put("uris", JSONArray(uris))

                val req = RequestBody.create(MediaType.parse("application/json"), body.toString())

                val response = endpoint.addTracksToPlaylist(
                    body = req,
                    auth = makeAuthHeader(token),
                    id = playlistId
                )
                if (response.isSuccessful) {
                    return@async true
                } else {
                    when (response.code()) {
                        403 -> {
                            Timber.d("Playlist not authorized by user OR playlist exceeded 10,000 tracks.")
                            Timber.d(response.message())
                            response.errorBody()?.toString()?.let { Timber.d(it) }
                        }
                        else -> {
                            Timber.d("Error adding tracks to playlist.")
                            Timber.d(response.message())
                            response.errorBody()?.toString()?.let { Timber.d(it) }
                        }
                    }
                }

                return@async false
            }
        } catch (e: Exception) {
            Timber.e("Error adding tracks to Spotify playlist: $e")
            null
        }

    }

    private suspend fun getUserProfileAsync(token: String): Deferred<SpotifyProfile?>? =
        supervisorScope {
            try {
                async {
                    val response = endpoint.getUserProfile(authHeader = "Bearer $token")

                    if (response.isSuccessful) {
                        mapper.profile(response.body())
                    } else return@async null
                }
            } catch (e: Exception) {
                Timber.e("Error getting Spotify user profile: $e")
                null
            }
        }

    /**
     * Creates an encoded query for music search for a given [SearchType] and [artist]
     */
    private fun makeSearchQuery(type: SearchType, title: String, artist: String?): String {
        val musicQuery = title.let { "${type.type}:${it.encode()}" }
        val artistQuery = artist?.let { "artist:${it.encode()}" }
        return listOfNotNull(musicQuery, artistQuery).joinToString(separator = " ")
    }

    /**
     * Creates the auth header needed for all Client Credentials API requests.
     * If we don't have a token, this will request one.
     */
    private suspend fun makeClientAuthHeader(): String {
        val token = this.clientToken
        return if (token.isNotBlank()) {
            makeAuthHeader(token)
        } else {
            val auth = endpoint.authorizeApp()
            auth.body()?.token?.let {
                "Bearer $it"
            } ?: throw NetworkErrorException("Can't auth via Spotify")
        }
    }

    private fun getUserPlaylistsUrl(id: String) = "https://api.spotify.com/v1/users/$id/playlists"

    private fun String?.encode() = this.urlEncoded.replace("+", " ")

    private fun makeAuthHeader(token: String) = "Bearer $token"

    companion object {
        enum class SearchType(val type: String) {
            ALBUM("album"), TRACK("track")
        }
    }
}
