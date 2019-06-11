package fho.kdvs.track

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.favorite.FavoriteRepository
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.FavoriteEntity
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.web.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class TrackDetailsViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val broadcastRepository: BroadcastRepository,
    private val favoriteRepository: FavoriteRepository,
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
            var mbData: MusicBrainzData? = null
            var spotifyData: SpotifyData? = null

            launch {
                launch { trackRepository.onScrapeMetadata(track.trackId)}

                val musicBrainzJob = launch {
                    if (!track.album.isNullOrBlank()) {
                        val mb = MusicBrainzAlbum()
                        mbData = mb.getMusicData(track.album, track.artist)
                    }

                    // if album query returns no results, attempt with song
                    if (mbData?.albumTitle.isNullOrBlank()) {
                        val mb = MusicBrainzSong()
                        mbData = mb.getMusicData(track.song, track.artist)
                    }
                }

                val spotifyJob = launch {
                    val spotify = SpotifyAlbum()
                    spotifyData = spotify.getMusicData(track.album, track.artist)
                }

                musicBrainzJob.join()
                spotifyJob.join()

                if (!mbData?.albumTitle.isNullOrBlank()) {
                    launch { trackRepository.updateTrackAlbum(track.trackId, mbData?.albumTitle)}
                } else if (!spotifyData?.albumTitle.isNullOrBlank()) {
                    launch { trackRepository.updateTrackAlbum(track.trackId, spotifyData?.albumTitle)}
                }

                if (!mbData?.label.isNullOrBlank()) {
                    launch { trackRepository.updateTrackLabel(track.trackId, mbData?.label)}
                }

                if (!mbData?.imageHref.isNullOrBlank()) {
                    launch { trackRepository.updateTrackImageHref(track.trackId, mbData?.imageHref)}
                } else if (!spotifyData?.imageHref.isNullOrBlank()) {
                    launch { trackRepository.updateTrackImageHref(track.trackId, spotifyData?.imageHref)}
                }

                if (mbData?.year != null && mbData?.year != -1) {
                    launch { trackRepository.updateTrackYear(track.trackId, mbData?.year)}
                } else if (spotifyData?.year != null && spotifyData?.year != -1) {
                    launch { trackRepository.updateTrackYear(track.trackId, spotifyData?.year)}
                }

                if (!spotifyData?.spotifyUri.isNullOrBlank()) {
                    launch { trackRepository.updateTrackSpotifyUri(track.trackId, spotifyData?.spotifyUri)}
                }
            }
        }

        liveTrack = trackRepository.trackById(track.trackId)
    }
}