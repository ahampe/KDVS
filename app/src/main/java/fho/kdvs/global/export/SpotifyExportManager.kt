package fho.kdvs.global.export

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationRequest
import com.spotify.sdk.android.authentication.AuthenticationResponse
import fho.kdvs.api.endpoint.SpotifyEndpoint
import fho.kdvs.api.mapped.SpotifyPlaylist
import fho.kdvs.api.service.SpotifyService
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.RequestCodes
import fho.kdvs.global.util.TimeHelper
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject


/**
 * Mediates export requests from UI to [SpotifyService] and handles Spotify login flow.
 */
class SpotifyExportManager @Inject constructor(
    val activity: FragmentActivity,
    val spotifyService: SpotifyService,
    val kdvsPreferences: KdvsPreferences,
    val sharedViewModel: SharedViewModel
) : Loginable {

    private var playlist: SpotifyPlaylist? = null
    private lateinit var userToken: String

    /**
     * Launches Spotify login activity when stored token is invalid.
     * Mutates token LiveData in [SharedViewModel]
     * */
    override fun loginIfNecessary() {
        if (isSpotifyAuthVoidOrExpired()) {
            val builder = AuthenticationRequest.Builder(
                SpotifyEndpoint.SPOTIFY_CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                SpotifyEndpoint.SPOTIFY_REDIRECT_URI
            )

            builder.setScopes(
                arrayOf(
                    "playlist-modify-public",
                    "playlist-read-private",
                    "playlist-modify-private"
                )
            )

            val request = builder.build()

            AuthenticationClient.openLoginActivity(activity, RequestCodes.SPOTIFY_LOGIN, request)

            sharedViewModel.spotifyAuthToken.observe(activity, Observer {
                userToken = it
            })
        } else {
            userToken = kdvsPreferences.spotifyAuthToken!!
            sharedViewModel.spotifyAuthToken.postValue(userToken)
        }
    }

    /**
     * Export to dynamic playlist by title, e.g. for a specific broadcast.
     *
     * @return Spotify URI of created playlist, if successful
     */
    suspend fun exportTracksToDynamicPlaylistAsync(
        trackUris: List<String>?,
        playlistTitle: String
    ): Deferred<String?> = coroutineScope {
        async {
            if (trackUris.isNullOrEmpty()) return@async null

            if (::userToken.isInitialized) {
                playlist = spotifyService.getSpotifyPlaylistFromTitleAsync(playlistTitle, userToken)
                    .await() ?: spotifyService.createPlaylistAsync(playlistTitle, userToken)
                    .await()

                if (exportTracksToPlaylistAsync(trackUris, playlist).await()) {
                    return@async playlist?.uri
                } else {
                    return@async null
                }
            } else {
                return@async null
            }
        }
    }

    /**
     * Export to a fixed, accumulative playlist stored in preferences, e.g. 'My KDVS Favorites'.
     *
     * @return Spotify URI of created playlist, if successful
     */
    suspend fun exportTracksToStoredPlaylistAsync(
        trackUris: List<String>?,
        storedPlaylistUri: String?
    ): Deferred<String?> = coroutineScope {
        async {
            if (trackUris.isNullOrEmpty()) return@async null

            if (::userToken.isInitialized) {
                storedPlaylistUri?.let {
                    playlist = spotifyService.getPlaylistFromUserAsync(storedPlaylistUri, userToken)
                        .await()
                }

                if (exportTracksToPlaylistAsync(trackUris, playlist).await()) {
                    return@async playlist?.uri
                } else {
                    return@async null
                }
            } else {
                return@async null
            }
        }
    }

    private suspend fun exportTracksToPlaylistAsync(
        trackUris: List<String>,
        playlist: SpotifyPlaylist?
    ): Deferred<Boolean> = coroutineScope {
        async {
            playlist?.let { p ->
                var uris = trackUris
                val id = getPlaylistIDFromUri(p.uri)

                // Skip duplicates in playlists with existing tracks
                (0..p.count step MAX_SPOTIFY_EXPORT_COUNT)
                    .takeWhile { uris.isNotEmpty() }
                    .forEach { offset ->
                        spotifyService.getTracksInPlaylistAsync(id, userToken, offset)
                            .await()
                            ?.mapNotNull { t -> t?.uri }
                            ?.let { existingTrackUris ->
                                uris = uris.filter { u -> !existingTrackUris.contains(u) }
                            }
                    }

                val successes = mutableListOf<Boolean>()

                uris.chunked(MAX_SPOTIFY_EXPORT_COUNT).forEach { tracks ->
                    successes.add(
                        spotifyService.addTracksToPlaylistAsync(tracks, id, userToken)
                            .await()
                    )
                }

                return@async successes.all { it }
            }

            return@async false
        }
    }

    private fun getPlaylistIDFromUri(uri: String) =
        uri.replace("spotify:playlist:", "")

    /**
     * Spotify user auth tokens are valid for one hour after time of creation.
     */
    private fun isSpotifyAuthVoidOrExpired() = kdvsPreferences.spotifyAuthToken.isNullOrEmpty() ||
            kdvsPreferences.spotifyLastLogin ?: 0 < TimeHelper.getOneHourAgoUTC().toEpochSecond()

    companion object {
        const val MAX_SPOTIFY_EXPORT_COUNT = 100
    }
}
