package fho.kdvs.global.export

import android.content.Context
import android.widget.Toast
import fho.kdvs.api.mapped.SpotifyPlaylist
import fho.kdvs.api.service.SpotifyService
import kotlinx.coroutines.*
import javax.inject.Inject

const val MAX_SPOTIFY_EXPORT_COUNT = 100

class ExportManagerSpotify @Inject constructor(
    val context: Context,
    val spotifyService: SpotifyService,
    val trackUris: List<String>?,
    val userToken: String,
    val playlistTitle: String,
    val storedPlaylistUri: String? = null
) : ExportManager {

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

        return if (exportTracksToSpotifyPlaylistAsync().await()) {
            playlist?.uri
        } else {
            null
        }
    }

    private suspend fun exportTracksToSpotifyPlaylistAsync(): Deferred<Boolean> = coroutineScope {
        async {
            playlist?.let { p ->
                trackUris?.let {
                    var uris = it
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

                    val success = mutableListOf<Boolean>()

                    uris.chunked(MAX_SPOTIFY_EXPORT_COUNT).forEach { tracks ->
                        success.add(
                            spotifyService.addTracksToPlaylistAsync(tracks, id, userToken)
                                .await()
                        )
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
