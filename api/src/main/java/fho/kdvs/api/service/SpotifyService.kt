package fho.kdvs.api.service

import android.accounts.NetworkErrorException
import fho.kdvs.api.endpoint.SpotifyEndpoint
import fho.kdvs.api.mapped.SpotifyAlbum
import fho.kdvs.api.mapper.SpotifyMapper
import fho.kdvs.base.urlEncoded
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

/**
 * Client-facing singleton that wraps the Retrofit endpoint for Spotify API requests. Automatically
 * handles authentication.
 */
@Singleton
class SpotifyService @Inject constructor(
    private val endpoint: SpotifyEndpoint
) : BaseService() {

    private val mapper = SpotifyMapper()
    private var accessToken: String = ""

    suspend fun findAlbumAsync(album: String?,
                               artist: String?): Deferred<SpotifyAlbum?> = coroutineScope {
        async {
            if (album.isNullOrBlank()) return@async null

            val query = makeAlbumQuery(album, artist)
            val response = endpoint.searchAlbums(query = query, authHeader = makeAuthHeader())
            if (response.isSuccessful) {
                mapper.album(response.body())
            } else {
                when (response.code()) {
                    401 -> {
                        // Auth failed. Retry it
                        val auth = endpoint.authorize()
                        val newResponse = auth.body()?.token?.let { token ->
                            accessToken = token
                            endpoint.searchAlbums(query = query, authHeader = makeAuthHeader()).body()
                        }
                        return@async newResponse?.let { mapper.album(it) }
                    }
                    429 -> {
                        // TODO untested
                        // Hit rate limit. Wait for specified span of time and try again recursively
                        return@async response.headers().get("Retry-After")?.toIntOrNull()?.let { seconds ->
                            val millis = seconds * 1000L
                            delay(millis)
                            val search = findAlbumAsync(album, artist)
                            search.await()
                        }
                    }
                    else -> {
                        Timber.d("Error searching for album given $album and $artist")
                        Timber.d(response.message())
                        response.errorBody()?.string()?.let { Timber.d(it) }
                        return@async null
                    }
                }
            }
        }
    }

    /**
     * Creates an encoded query for album search for a given [album] and [artist]
     */
    private fun makeAlbumQuery(album: String, artist: String?): String {
        val albumQuery = album.let { "album:${encode(it)}" }
        val artistQuery = artist?.let { "artist:${encode(it)}" }
        return listOfNotNull(albumQuery, artistQuery).joinToString(separator = " ")
    }

    /**
     * Creates the auth header needed for all API requests.
     * If we don't have a token, this will request one.
     */
    private suspend fun makeAuthHeader(): String {
        val token = this.accessToken
        return if (token.isNotBlank()) {
            "Bearer $token"
        } else {
            val auth = endpoint.authorize()
            auth.body()?.token?.let {
                "Bearer $it"
            } ?: throw NetworkErrorException("Can't auth via Spotify")
        }
    }

    private fun encode(s: String?) = s.urlEncoded.replace("+", " ")

}
