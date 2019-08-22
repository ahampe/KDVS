package fho.kdvs.track

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import fho.kdvs.R
import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.favorite.FavoriteRepository
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowBroadcastTrackFavoriteJoin
import fho.kdvs.global.database.TrackEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@kotlinx.serialization.UnstableDefault
class FavoriteTrackDetailsViewModel @Inject constructor(
    val trackRepository: TrackRepository,
    private val broadcastRepository: BroadcastRepository,
    private val favoriteRepository: FavoriteRepository,
    application: Application
) : AndroidViewModel(application), CoroutineScope {

    lateinit var liveJoins: LiveData<List<ShowBroadcastTrackFavoriteJoin>>

    lateinit var navController: NavController

    private val parentJob = Job()
    override val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.IO

    fun initialize() {
        liveJoins = favoriteRepository.allShowBroadcastTrackFavoriteJoins()
    }

    fun getBroadcastForTrack(track: TrackEntity) = broadcastRepository.broadcastById(track.broadcastId)

    fun getShowForBroadcast(broadcast: BroadcastEntity) = broadcastRepository.showByBroadcastId(broadcast.broadcastId)

    fun onClickTrackHeader(view: View, track: TrackEntity) {
        val showId = view.tag as? Int?

        if (::navController.isInitialized && showId != null) {
            val navAction = FavoriteTrackDetailsFragmentDirections
                .actionFavoriteTrackDetailsFragmentToBroadcastDetailsFragment(showId, track.broadcastId)
            if (navController.currentDestination?.id == R.id.favoriteTrackDetailsFragment)
                navController.navigate(navAction)
        }
    }
}