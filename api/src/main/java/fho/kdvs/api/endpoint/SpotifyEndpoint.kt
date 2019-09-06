package fho.kdvs.api.endpoint

import android.util.Base64
import fho.kdvs.api.raw.SpotifyAuthResponse
import fho.kdvs.api.raw.SpotifyPlaylistResponse
import fho.kdvs.api.raw.album.SpotifyAlbumsResponse
import fho.kdvs.api.raw.objects.SpotifyPrivateUserObject
import fho.kdvs.api.raw.objects.SpotifyPublicUserObject
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.*
import timber.log.Timber

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
     * Requests an access token for the Spotify Web API.
     */
    @FormUrlEncoded
    @POST
    suspend fun authorize(@Url url: String = TOKEN_URL,
                          @Header("Authorization") auth: String = authHeaderValue,
                          @Field("grant_type") grant: String = "client_credentials"
    ): Response<SpotifyAuthResponse>

    /**
     * Requests Spotify profile object for current user.
     */
    @GET("/v1/me")
    suspend fun getUserProfile(@Header("Authorization") authHeader: String
    ): Response<SpotifyPrivateUserObject>

    /**
     * Searches for the given [query] on Spotify and returns the closest matching album.
     */
    @GET("/v1/search?type=album")
    suspend fun searchAlbums(@Header("Authorization") authHeader: String,
                             @Query("q") query: String,
                             @Query("limit") limit: Int = 1
    ): Response<SpotifyAlbumsResponse>

    /**
     * Creates a playlist for logged-in user with given title.
     */
    @FormUrlEncoded
    @POST
    suspend fun createPlaylist(@Url url: String,
                          @Header("Authorization") auth: String = authHeaderValue,
                          @Field("content_type") grant: String = "application/json",
                          @Body body: JSONObject
    ): Response<SpotifyPlaylistResponse>

    companion object {

        internal const val BASE_URL = "https://api.spotify.com"
        const val TOKEN_URL = "https://accounts.spotify.com/api/token"

        internal const val SPOTIFY_CLIENT_ID = "7f214830ba524ae1a8b1b8181ad4c2a4"
        internal const val SPOTIFY_CLIENT_SECRET = "70f0fbddfb38481b8b604fa920a230ce"
        internal const val SPOTIFY_REDIRECT_URI = "http://com.yourdomain.yourapp/callback"
        internal const val SPOTIFY_SEARCH_URL = "https://api.spotify.com/v1/search?q="
        internal const val SPOTIFY_TOKEN_URL = "https://accounts.spotify.com/api/token"
        internal const val SPOTIFY_GET_USER_PROFILE_URL = "https://api.spotify.com/v1/me"

    }

}
