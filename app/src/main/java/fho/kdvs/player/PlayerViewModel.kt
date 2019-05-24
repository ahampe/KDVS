package fho.kdvs.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.navigation.NavController
import fho.kdvs.R
import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowEntity
import javax.inject.Inject

class PlayerViewModel @Inject constructor(
    private val broadcastRepository: BroadcastRepository,
    application: Application
) : AndroidViewModel(application) {

    lateinit var nowPlayingLiveData: MediatorLiveData<Pair<ShowEntity, BroadcastEntity?>>

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

    fun onClickStar() {

    }
}