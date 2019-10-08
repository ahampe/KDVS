package fho.kdvs.global.util

import android.content.Context
import android.widget.Toast
import fho.kdvs.api.mapped.SpotifyPlaylist
import fho.kdvs.api.service.SpotifyService
import kotlinx.coroutines.*
import javax.inject.Inject

class ExportManagerSpotify @Inject constructor(
    val context: Context,
    val spotifyService: SpotifyService,
    val trackUris: List<String>?,
    val userToken: String,
    val playlistTitle: String,
    val storedPlaylistUri: String? = null
): ExportManager {

    private var playlist: SpotifyPlaylist? = null

    override suspend fun getExportPlaylistUri(): String? {
        if (trackUris.isNullOrEmpty()) return null

        playlist = if (!storedPlaylistUri.isNullOrEmpty()) {
            spotifyService.getPlaylistFromUserAsync(storedPlaylistUri, userToken).await()
        } else {
            spotifyService.getSpotifyPlaylistFromTitleAsync(playlistTitle, userToken)
                .await() ?: spotifyService.createPlaylistAsync(playlistTitle, userToken)
                    .await()
        }

        return if (exportTracksToSpotifyPlaylistAsync().await() == true) {
            playlist?.uri
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Error exporting music. Try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            null
        }
    }

    private suspend fun exportTracksToSpotifyPlaylistAsync(): Deferred<Boolean?> = coroutineScope {
        async {
            playlist?.let { p ->
                trackUris?.let {
                    var uris = it
                    val id = getPlaylistIDFromUri(p.uri)

                    // Skip duplicates in playlist with existing tracks
                    if (p.count > 0) {
                        (0..p.count step 100).takeWhile { uris.isNotEmpty() }.forEach { offset ->
                            spotifyService.getTracksInPlaylistAsync(id, userToken, offset)
                                .await()
                                ?.mapNotNull { t -> t?.uri }
                                ?.let { existingTrackUris ->
                                    uris = uris.filter { u -> !existingTrackUris.contains(u) }
                                }
                        }
                    }

                    val success = mutableListOf<Boolean>()

                    uris.chunked(100).forEach { tracks ->
                        success.add(spotifyService.addTracksToPlaylistAsync(tracks, id, userToken)
                            .await())
                    }

                    return@async success.all { s -> s }
                }
            }

            return@async false
        }
    }

    private fun getPlaylistIDFromUri(uri: String) =
        uri.replace("spotify:playlist:", "")
}
