package fho.kdvs.show

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
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

    fun onClickBroadcast(navController: NavController, broadcast: BroadcastEntity) {
        val navAction = ShowDetailsFragmentDirections
            .actionShowDetailsFragmentToBroadcastDetailsFragment(broadcast.showId, broadcast.broadcastId)
        navController.navigate(navAction)
    }

    private fun fetchBroadcasts(showId: Int) {
        broadcastRepository.scrapeShow(showId.toString())
    }
}