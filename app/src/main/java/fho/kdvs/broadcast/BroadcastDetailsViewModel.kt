package fho.kdvs.broadcast

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.track.TrackRepository
import javax.inject.Inject

class BroadcastDetailsViewModel @Inject constructor(
    private val broadcastRepository: BroadcastRepository,
    private val trackRepository: TrackRepository,
    application: Application
) : AndroidViewModel(application) {

    lateinit var broadcast: LiveData<BroadcastEntity>
    lateinit var tracks: LiveData<List<TrackEntity>>

    fun initialize(broadcastId: Int) {
        fetchTracks(broadcastId)
        broadcast = broadcastRepository.broadcastById(broadcastId)
        tracks = trackRepository.tracksForBroadcast(broadcastId)
    }

    private fun fetchTracks(broadcastId: Int) {
        trackRepository.fetchTracksForBroadcast(broadcastId.toString())
    }
}