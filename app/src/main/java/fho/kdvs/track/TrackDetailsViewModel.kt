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

@kotlinx.serialization.UnstableDefault
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
            var mbData: MusicBrainzReleaseData? = null
            var mbImageHref: String? = null
            var spotifyData: SpotifyData? = null

            launch {
                launch { trackRepository.onScrapeMetadata(track.trackId) }

                val musicBrainzJob = launch {
                    mbData = MusicBrainz.searchFromAlbum(track.album, track.artist)
                    mbImageHref = MusicBrainz.getCoverArtImage(mbData.id)
                }

                val spotifyJob = launch {
                    val query = Spotify.getAlbumQuery(track.album, track.artist)
                    spotifyData = Spotify.search(query)
                }

                musicBrainzJob.join()
                spotifyJob.join()

                if (!mbData.album.isNullOrBlank()) {
                    launch { trackRepository.updateTrackAlbum(track.trackId, mbData?.album)}
                } else if (!spotifyData?.album.isNullOrBlank()) {
                    launch { trackRepository.updateTrackAlbum(track.trackId, spotifyData?.album)}
                }

                if (!mbData?.label.isNullOrBlank()) {
                    launch { trackRepository.updateTrackLabel(track.trackId, mbData?.label)}
                }

                if (!mbImageHref.isNullOrBlank()) {
                    launch { trackRepository.updateTrackImageHref(track.trackId, mbImageHref)}
                } else if (!spotifyData?.imageHref.isNullOrBlank()) {
                    launch { trackRepository.updateTrackImageHref(track.trackId, spotifyData?.imageHref)}
                }

                if (mbData?.year != null && mbData?.year != -1) {
                    launch { trackRepository.updateTrackYear(track.trackId, mbData?.year)}
                } else if (spotifyData?.year != null && spotifyData?.year != -1) {
                    launch { trackRepository.updateTrackYear(track.trackId, spotifyData?.year)}
                }

                if (!spotifyData?.uri.isNullOrBlank()) {
                    launch { trackRepository.updateTrackSpotifyUri(track.trackId, spotifyData?.uri)}
                }
            }
        }

        liveTrack = trackRepository.trackById(track.trackId)
    }
}