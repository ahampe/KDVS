package fho.kdvs.favorite.track

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.navigation.NavController
import fho.kdvs.R
import fho.kdvs.favorite.FavoriteFragmentDirections
import fho.kdvs.global.database.ShowBroadcastFavoriteJoin
import fho.kdvs.global.database.ShowBroadcastTrackFavoriteJoin
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.track.TrackRepository
import javax.inject.Inject

class FavoriteTrackViewModel @Inject constructor(
    val trackRepository: TrackRepository,
    favoriteRepository: FavoriteTrackRepository,
    application: Application
) : AndroidViewModel(application) {

    private val showBroadcastFavoriteJoins =
        favoriteRepository.allShowBroadcastFavoriteJoins()

    private val showBroadcastTrackFavoriteJoins =
        favoriteRepository.allShowBroadcastTrackFavoriteJoins()

    val allJoins =
        MediatorLiveData<Pair<List<ShowBroadcastFavoriteJoin>?, List<ShowBroadcastTrackFavoriteJoin>?>>().apply {
            var broadcastFavoriteEnt: List<ShowBroadcastFavoriteJoin>? = null
            var trackFavoriteEnt: List<ShowBroadcastTrackFavoriteJoin>? = null

            addSource(showBroadcastFavoriteJoins) { join ->
                broadcastFavoriteEnt = join
                val trackFavorite = trackFavoriteEnt ?: return@addSource
                postValue(Pair(broadcastFavoriteEnt, trackFavorite))
            }

            addSource(showBroadcastTrackFavoriteJoins) { join ->
                trackFavoriteEnt = join
                val broadcastFavorite = broadcastFavoriteEnt ?: return@addSource
                postValue(Pair(broadcastFavorite, trackFavoriteEnt))
            }
        }


    fun onClickTrack(navController: NavController, track: TrackEntity?, resultIds: IntArray) {
        track?.let {
            val navAction =
                FavoriteFragmentDirections.actionFavoriteFragmentToFavoriteTrackDetailsFragment(
                    track,
                    resultIds
                )
            if (navController.currentDestination?.id == R.id.favoriteFragment)
                navController.navigate(navAction)
        }
    }
}
