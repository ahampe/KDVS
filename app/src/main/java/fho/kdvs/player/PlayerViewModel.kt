package fho.kdvs.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.navigation.NavController
import fho.kdvs.R
import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.database.SubscriptionEntity
import fho.kdvs.subscription.SubscriptionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class PlayerViewModel @Inject constructor(
    private val broadcastRepository: BroadcastRepository,
    private val subscriptionRepository: SubscriptionRepository,
    application: Application
) : AndroidViewModel(application), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    lateinit var nowPlayingLiveData: MediatorLiveData<Pair<ShowEntity, BroadcastEntity?>>
    lateinit var subscription: LiveData<SubscriptionEntity>

    fun initialize() {
        nowPlayingLiveData = broadcastRepository.nowPlayingLiveData
    }

    fun onClickPlaylist(navController: NavController, broadcast: BroadcastEntity?) {
        broadcast?.let {
            val navAction = PlayerFragmentDirections
                .actionPlayerFragmentToBroadcastDetailsFragment(broadcast.showId, broadcast.broadcastId)
            if (navController.currentDestination?.id == R.id.playerFragment)
                navController.navigate(navAction)
        }
    }

    fun onClickShowInfo(navController: NavController, show: ShowEntity) {
        val navAction = PlayerFragmentDirections
            .actionPlayerFragmentToShowDetailsFragment(show.id)
        if (navController.currentDestination?.id == R.id.playerFragment)
            navController.navigate(navAction)
    }



    fun setSubscription(showId: Int) {
        subscription = subscriptionRepository.subscriptionByShowId(showId)
    }
}