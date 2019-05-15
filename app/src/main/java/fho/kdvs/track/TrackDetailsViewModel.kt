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
            var trackWithMetadata: TrackEntity = track
            val job = launch { trackWithMetadata = MusicBrainz.fetchTrackInfo(track) }

            job.invokeOnCompletion {
                if (!hasAlbum && !trackWithMetadata.album.isNullOrBlank())
                    launch { trackRepository.updateTrackAlbum(track.trackId, trackWithMetadata.album)}
                if (!hasLabel && !trackWithMetadata.label.isNullOrBlank())
                    launch { trackRepository.updateTrackLabel(track.trackId, trackWithMetadata.label)}
                if (!trackWithMetadata.imageHref.isNullOrBlank())
                    launch { trackRepository.updateTrackImageHref(track.trackId, trackWithMetadata.imageHref) }
                if (trackWithMetadata.year != null && trackWithMetadata.year != -1)
                    launch { trackRepository.updateTrackYear(track.trackId, trackWithMetadata.year)}
                launch { trackRepository.onScrapeMetadata(track.trackId) }
            }
        }

        liveTrack = trackRepository.trackById(track.trackId)
    }

    // TODO: refactor this block (shared with BroadcastDetailsViewModel)
    fun onClickFavorite(view: View, trackId: Int) {
        val imageView = view as? ImageView

        if (imageView?.tag == 0) {
            imageView.setImageResource(R.drawable.ic_favorite_white_24dp)
            imageView.tag = 1
            launch { favoriteDao.insert(FavoriteEntity(0, trackId)) }
        } else if (imageView?.tag == 1) {
            imageView.setImageResource(R.drawable.ic_favorite_border_white_24dp)
            imageView.tag = 0
            launch { favoriteDao.deleteByTrackId(trackId) }
        }
    }
}