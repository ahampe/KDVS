package fho.kdvs.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.navigation.NavController
import fho.kdvs.R
import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.global.database.*
import fho.kdvs.show.ShowRepository
import fho.kdvs.subscription.SubscriptionRepository
import fho.kdvs.track.TrackRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class PlayerViewModel @Inject constructor(
    private val broadcastRepository: BroadcastRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val trackRepository: TrackRepository,
    application: Application
) : AndroidViewModel(application), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    lateinit var nowPlayingLiveData: MediatorLiveData<Pair<ShowTimeslotEntity, BroadcastEntity?>>
    lateinit var subscription: LiveData<SubscriptionEntity>
    var tracksLiveData: LiveData<List<TrackEntity>>? = null

    fun initialize() {
        nowPlayingLiveData = broadcastRepository.nowPlayingLiveData
    }

    fun setTracksLiveDataForBroadcast(broadcastId: Int) {
        tracksLiveData = trackRepository.tracksForBroadcast(broadcastId)
    }

    fun onClickPlaylist(navController: NavController, broadcast: BroadcastEntity?) {
        broadcast?.let {
            val navAction = PlayerFragmentDirections
                .actionPlayerFragmentToBroadcastDetailsFragment(
                    broadcast.showId,
                    broadcast.broadcastId
                )
            if (navController.currentDestination?.id == R.id.playerFragment)
                navController.navigate(navAction)
        }
    }

    fun onClickShowInfo(navController: NavController, show: ShowTimeslotEntity) {
        val navAction = PlayerFragmentDirections
            .actionPlayerFragmentToShowDetailsFragment(show.id)
        if (navController.currentDestination?.id == R.id.playerFragment)
            navController.navigate(navAction)
    }

    fun setSubscription(showId: Int) {
        subscription = subscriptionRepository.subscriptionByShowId(showId)
    }
}