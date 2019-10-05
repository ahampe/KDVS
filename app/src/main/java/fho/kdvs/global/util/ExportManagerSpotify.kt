package fho.kdvs.global.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import fho.kdvs.api.service.SpotifyService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class ExportManagerSpotify @Inject constructor(
    val context: Context,
    val spotifyService: SpotifyService,
    val trackUris: List<String>?,
    val userToken: String,
    val playlistTitle: String,
    val storedPlaylistUri: String? = null
): ExportManager {

    var playlistUri: String? = null

    override suspend fun getExportPlaylistUri(): String? {
        if (trackUris.isNullOrEmpty()) return null

        // Check for stored URI, then check user's Spotify playlists for matching title,
        // then create playlist
        playlistUri =
            if (!storedPlaylistUri.isNullOrEmpty() &&
                spotifyService.playlistExistsAsync(storedPlaylistUri, userToken).await() == true) {
                    storedPlaylistUri
            } else getSpotifyPlaylistUriFromTitleAsync(playlistTitle, userToken)
                    .await() ?:
                spotifyService.createPlaylistAsync(playlistTitle, userToken).await()

        return if (!playlistUri.isNullOrEmpty() &&
                   exportTracksToSpotifyPlaylistAsync().await() == true) {
                        playlistUri
        } else {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    context,
                    "Error exporting music. Try again.",
                    Toast.LENGTH_LONG
                ).show()
            }

            null
        }
    }

    private suspend fun getSpotifyPlaylistUriFromTitleAsync(title: String,
                                                    token: String): Deferred<String?> = coroutineScope {
        async {
            return@async spotifyService.getSpotifyPlaylistUriFromTitleAsync(title, token).await()
        }
    }

    private suspend fun exportTracksToSpotifyPlaylistAsync(): Deferred<Boolean?> = coroutineScope {
        async {
            trackUris?.let {
                it.chunked(100).forEach { tracks ->
                    val id = getPlaylistIDFromUri(playlistUri!!)
                    return@async spotifyService.addTracksToPlaylistAsync(tracks, id, userToken).await()
                }
            }

            return@async false
        }
    }

    private fun getPlaylistIDFromUri(uri: String) =
        uri.replace("spotify:playlist:", "")
}
