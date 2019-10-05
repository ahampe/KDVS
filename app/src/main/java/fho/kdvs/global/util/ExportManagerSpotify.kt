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

    override suspend fun getExportPlaylistUri(): String? {
        if (trackUris.isNullOrEmpty()) return null

        // Check for stored URI, then check user's Spotify playlists for matching title,
        // then create playlist
        val playlistUri =
            if (!storedPlaylistUri.isNullOrEmpty() &&
                spotifyService.playlistExistsAsync(storedPlaylistUri, userToken).await() == true) {
                    storedPlaylistUri
            } else getSpotifyPlaylistUriFromTitleAsync(playlistTitle, userToken)
                    .await() ?:
                exportTracksToSpotifyPlaylistAsync(trackUris, playlistTitle, userToken)
                    .await()

        return if (!playlistUri.isNullOrEmpty()) {
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

    private suspend fun exportTracksToSpotifyPlaylistAsync(trackUris: List<String>?,
                                                   title: String,
                                                   token: String): Deferred<String?> = coroutineScope {
        async {
            trackUris?.let {
                val playlist = spotifyService.createPlaylistAsync(title, token).await()

                playlist?.let { p ->
                    trackUris.chunked(100).forEach {
                        spotifyService.addTracksToPlaylistAsync(it, p.id, token).await()
                    }

                    return@async playlist.uri
                }
            }

            return@async null
        }
    }
}