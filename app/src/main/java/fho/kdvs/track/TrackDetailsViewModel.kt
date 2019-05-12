package fho.kdvs.show

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.web.MusicBrainz
import fho.kdvs.track.TrackRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class TrackDetailsViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    application: Application
) : AndroidViewModel(application), CoroutineScope {

    lateinit var liveTrack: LiveData<TrackEntity>

    private val parentJob = Job()
    override val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.IO

    fun initialize(track: TrackEntity) {
        if (!track.hasScrapedMetadata) {
            val hasLabel = !track.label.isNullOrEmpty()
            var trackWithMetadata: TrackEntity = track
            val job = launch { trackWithMetadata = MusicBrainz.fetchTrackInfo(track) }

            job.invokeOnCompletion {
                if (!trackWithMetadata.imageHref.isNullOrBlank())
                    launch { trackRepository.updateTrackImageHref(track.trackId, trackWithMetadata.imageHref) }
                if (!hasLabel && !trackWithMetadata.label.isNullOrBlank())
                    launch { trackRepository.updateTrackLabel(track.trackId, trackWithMetadata.label)}
                if (trackWithMetadata.year != null && trackWithMetadata.year != -1)
                    launch { trackRepository.updateTrackYear(track.trackId, trackWithMetadata.year)}
                launch { trackRepository.onScrapeMetadata(track.trackId) }
            }
        }

        liveTrack = trackRepository.trackById(track.trackId)
    }
}