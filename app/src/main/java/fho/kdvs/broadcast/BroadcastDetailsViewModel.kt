package fho.kdvs.broadcast

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.services.MediaSessionConnection
import fho.kdvs.show.ShowRepository
import fho.kdvs.track.TrackRepository
import javax.inject.Inject

class BroadcastDetailsViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    private val broadcastRepository: BroadcastRepository,
    private val trackRepository: TrackRepository,
    private val mediaSessionConnection: MediaSessionConnection,
    application: Application
) : AndroidViewModel(application) {

    lateinit var show: LiveData<ShowEntity>
    lateinit var broadcast: LiveData<BroadcastEntity>
    lateinit var tracks: LiveData<List<TrackEntity>>

    fun initialize(showId: Int, broadcastId: Int) {
        fetchTracks(broadcastId)
        show = showRepository.showById(showId)
        broadcast = broadcastRepository.broadcastById(broadcastId)
        tracks = trackRepository.tracksForBroadcast(broadcastId)
    }

    /** Callback which plays this recorded broadcast, if it is still available */
    fun onPlayBroadcast() {
        val toPlay = broadcast.value ?: return
        val showId = show.value?.id ?: return
        mediaSessionConnection.transportControls.playFromMediaId(toPlay.broadcastId.toString(),
            Bundle().apply { putInt("SHOW_ID", showId) })
    }

    private fun fetchTracks(broadcastId: Int) {
        trackRepository.fetchTracksForBroadcast(broadcastId.toString())
    }
}