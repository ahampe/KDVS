package fho.kdvs.api.endpoint

import android.util.Base64
import fho.kdvs.api.raw.SpotifyAddToPlaylistResponse
import fho.kdvs.api.raw.SpotifyAuthResponse
import fho.kdvs.api.raw.SpotifyPlaylistResponse
import fho.kdvs.api.raw.album.SpotifyAlbumResponse
import fho.kdvs.api.raw.album.SpotifySimpleAlbumsResponse
import fho.kdvs.api.raw.objects.SpotifyPager
import fho.kdvs.api.raw.objects.SpotifyPrivateUserObject
import fho.kdvs.api.raw.track.SpotifyTracksResponse
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.*

interface SpotifyEndpoint {

    /**
     * Header value used to obtain Spotify access token.
     */
    private val authHeaderValue: String
        get() {
            val encoded = Base64.encodeToString(
                "$SPOTIFY_CLIENT_ID:$SPOTIFY_CLIENT_SECRET".toByteArray(),
                Base64.NO_WRAP
            )
            return "Basic $encoded"
        }

    /**
     * Requests an access token for the app to access the Spotify Web API (Client Credentials Flow).
     * https://developer.spotify.com/documentation/general/guides/authorization-guide/#client-credentials-flow
     */
    @FormUrlEncoded
    @POST
    suspend fun authorizeApp(@Url url: String = TOKEN_URL,
                             @Header("Authorization") auth: String = authHeaderValue,
                             @Field("grant_type") grant: String = "client_credentials"
    ): Response<SpotifyAuthResponse>

    /**
     * Requests an access token for user (Authorization Code Flow).
     */
    @GET
    suspend fun getUserToken(@Url url: String = TOKEN_URL,
                             @Header("Authorization") auth: String = authHeaderValue,
                             @Body body: JSONObject
    ): Response<SpotifyAuthResponse>

    /**
     * Requests Spotify profile object for current user.
     */
    @GET("/v1/me")
    suspend fun getUserProfile(@Header("Authorization") authHeader: String
    ): Response<SpotifyPrivateUserObject>


    /**
     * Requests Spotify album object for ID.
     */
    @GET("/v1/albums/{id}")
    suspend fun getAlbum(@Path(value = "id", encoded = true) id: String,
                         @Header("Authorization") authHeader: String
    ): Response<SpotifyAlbumResponse>

    /**
     * Searches for the given [query] on Spotify and returns the closest matching album.
     */
    @GET("/v1/search?type=album")
    suspend fun searchAlbums(@Header("Authorization") authHeader: String,
                             @Query("q") query: String,
                             @Query("limit") limit: Int = 1
    ): Response<SpotifySimpleAlbumsResponse>

    /**
     * Searches for the given [query] on Spotify and returns the closest matching track.
     */
    @GET("/v1/search?type=track")
    suspend fun searchTracks(@Header("Authorization") authHeader: String,
                             @Query("q") query: String,
                             @Query("limit") limit: Int = 1
    ): Response<SpotifyTracksResponse>

    /**
     * Returns list of current user's Spotify playlists.
     */
    @GET("/v1/me/playlists")
    suspend fun getPlaylists(@Header("Authorization") auth: String): Response<SpotifyPager<SpotifyPlaylistResponse>>

    /**
     * Creates a playlist for logged-in user with given title.
     */
    @POST
    suspend fun createPlaylist(@Url url: String,
                          @Header("Authorization") auth: String,
                          @Header("content_type") grant: String = "application/json",
                          @Body body: RequestBody
    ): Response<SpotifyPlaylistResponse>

    /**
     * Adds tracks to specified playlist.
     */
    @POST("/v1/playlists/{playlist_id}/tracks")
    suspend fun addTracksToPlaylist(@Path(value = "playlist_id", encoded = true) id: String,
                               @Header("Authorization") auth: String,
                               @Header("content_type") grant: String = "application/json",
                               @Body body: RequestBody
    ): Response<SpotifyAddToPlaylistResponse>

    companion object {

        internal const val BASE_URL = "https://api.spotify.com"
        const val TOKEN_URL = "https://accounts.spotify.com/api/token"

        const val SPOTIFY_CLIENT_ID = "7f214830ba524ae1a8b1b8181ad4c2a4"
        const val SPOTIFY_CLIENT_SECRET = "70f0fbddfb38481b8b604fa920a230ce"
        const val SPOTIFY_REDIRECT_URI = "fho://kdvs"
    }

}
