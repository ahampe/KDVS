package fho.kdvs.show

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowEntity
import javax.inject.Inject

class ShowDetailsViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    private val broadcastRepository: BroadcastRepository,
    application: Application
) : AndroidViewModel(application) {

    lateinit var show: LiveData<ShowEntity>
    lateinit var broadcastsLiveData: LiveData<List<BroadcastEntity>>

    /** Because this ViewModel depends on the show ID, it must be provided here before using. */
    fun initialize(showId: Int) {
        fetchBroadcasts(showId)
        show = showRepository.showById(showId)
        broadcastsLiveData = broadcastRepository.broadcastsForShow(showId)
    }

    private fun fetchBroadcasts(showId: Int) {
        broadcastRepository.fetchBroadcastsForShow(showId.toString())
    }
}