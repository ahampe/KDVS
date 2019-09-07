package fho.kdvs.api.endpoint

import android.util.Base64
import fho.kdvs.api.raw.SpotifyAuthResponse
import fho.kdvs.api.raw.album.SpotifyAlbumsResponse
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
     * Requests an access token for the Spotify Web API.
     */
    @FormUrlEncoded
    @POST
    suspend fun authorize(@Url url: String = TOKEN_URL,
                          @Header("Authorization") auth: String = authHeaderValue,
                          @Field("grant_type") grant: String = "client_credentials"
    ): Response<SpotifyAuthResponse>

    /**
     * Searches for the given [query] on Spotify and returns the closest matching album.
     */
    @GET("/v1/search?type=album")
    suspend fun searchAlbums(@Header("Authorization") authHeader: String,
                             @Query("q") query: String,
                             @Query("limit") limit: Int = 1
    ): Response<SpotifyAlbumsResponse>

    companion object {

        internal const val BASE_URL = "https://api.spotify.com"
        const val TOKEN_URL = "https://accounts.spotify.com/api/token"

        internal const val SPOTIFY_CLIENT_ID = "7f214830ba524ae1a8b1b8181ad4c2a4"
        internal const val SPOTIFY_CLIENT_SECRET = "70f0fbddfb38481b8b604fa920a230ce"

    }

}
