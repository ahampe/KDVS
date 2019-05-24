package fho.kdvs.track

import android.app.Application
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fho.kdvs.R
import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.favorite.FavoriteRepository
import fho.kdvs.global.database.*
import fho.kdvs.global.web.MusicBrainz
import fho.kdvs.global.web.Spotify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class TrackDetailsViewModel @Inject constructor(
    val trackRepository: TrackRepository,
    private val broadcastRepository: BroadcastRepository,
    private val favoriteRepository: FavoriteRepository,
    private val favoriteDao: FavoriteDao,
    private val spotify: Spotify,
    application: Application
) : AndroidViewModel(application), CoroutineScope {

    lateinit var liveTrack: LiveData<TrackEntity>
    lateinit var favorite: LiveData<FavoriteEntity>
    lateinit var broadcast: LiveData<BroadcastEntity>
    lateinit var show: LiveData<ShowEntity>

    private val parentJob = Job()
    override val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.IO

    fun initialize(track: TrackEntity) {
        favorite = favoriteRepository.favoriteByTrackId(track.trackId)
        broadcast = broadcastRepository.broadcastById(track.broadcastId)
        show = broadcastRepository.showByBroadcastId(track.broadcastId)
        
        if (!track.hasScrapedMetadata) {
            val hasAlbum = !track.album.isNullOrEmpty()
            val hasLabel = !track.label.isNullOrEmpty()
            
            var trackWithMBData: TrackEntity = track
            var trackWithSpotifyData: TrackEntity = track

            val musicBrainzJob = launch { trackWithMBData = MusicBrainz.fetchMusicBrainzData(track) }

            val spotifyJob = launch {
                if (track.spotifyUri.isNullOrEmpty()) {
                    trackWithSpotifyData = spotify.fetchSpotifyData(track)
                }
            }

            launch {
                musicBrainzJob.join()
                spotifyJob.join()

                if (!hasAlbum) {
                    if (!trackWithMBData.album.isNullOrBlank())
                        launch { trackRepository.updateTrackAlbum(track.trackId, trackWithMBData.album)}
                    else if (!trackWithSpotifyData.album.isNullOrBlank())
                        launch { trackRepository.updateTrackAlbum(track.trackId, trackWithSpotifyData.album)}
                }

                if (!hasLabel && !trackWithMBData.label.isNullOrBlank())
                    launch { trackRepository.updateTrackLabel(track.trackId, trackWithMBData.label)}

                if (!trackWithMBData.imageHref.isNullOrBlank())
                    launch { trackRepository.updateTrackImageHref(track.trackId, trackWithMBData.imageHref)}
                else if (!trackWithSpotifyData.imageHref.isNullOrBlank())
                    launch { trackRepository.updateTrackImageHref(track.trackId, trackWithSpotifyData.imageHref)}

                if (trackWithMBData.year != null && trackWithMBData.year != -1)
                    launch { trackRepository.updateTrackYear(track.trackId, trackWithMBData.year)}
                else if (trackWithSpotifyData.year != null && trackWithSpotifyData.year != -1)
                    launch { trackRepository.updateTrackYear(track.trackId, trackWithSpotifyData.year)}

                if (trackWithSpotifyData.spotifyUri != null)
                    launch { trackRepository.updateTrackSpotifyUri(track.trackId, trackWithSpotifyData.spotifyUri) }

                launch { trackRepository.onScrapeMetadata(track.trackId) }
            }
        }

        liveTrack = trackRepository.trackById(track.trackId)
    }
}