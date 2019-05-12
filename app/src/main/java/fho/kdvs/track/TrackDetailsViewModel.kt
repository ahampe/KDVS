package fho.kdvs.show

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
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
        if (track.imageHref.isNullOrBlank() && track.metadata == null) {
            var trackWithMetadata: TrackEntity = track
            val job = launch { trackWithMetadata = MusicBrainz.fetchTrackInfo(track) }

            job.invokeOnCompletion {
                launch { trackRepository.updateTrackImageHref(track.trackId, trackWithMetadata.imageHref) }
                launch { trackRepository.updateTrackMetadata(track.trackId, trackWithMetadata.metadata) }
            }
        }

        liveTrack = trackRepository.trackById(track.trackId)
    }
}