package fho.kdvs.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
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

    lateinit var showLiveData: LiveData<ShowEntity>
    lateinit var broadcastLiveData: LiveData<BroadcastEntity>

    fun initialize() {
        showLiveData = broadcastRepository.nowPlayingShowLiveData
        broadcastLiveData = broadcastRepository.nowPlayingBroadcastLiveData
    }

    fun onClickArrow() {

    }

    fun onClickPlaylist(navController: NavController, broadcast: BroadcastEntity) {
        val navAction = PlayerFragmentDirections
            .actionPlayerFragmentToBroadcastDetailsFragment(broadcast.showId, broadcast.broadcastId)
        if (navController.currentDestination?.id == R.id.broadcastDetailsFragment)
            navController.navigate(navAction)
    }

    fun onClickStar() {

    }
}