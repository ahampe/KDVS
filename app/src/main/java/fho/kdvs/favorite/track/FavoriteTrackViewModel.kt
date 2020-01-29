package fho.kdvs.favorite.track

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.navigation.NavController
import fho.kdvs.R
import fho.kdvs.favorite.FavoriteFragmentDirections
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.database.joins.getTracks
import fho.kdvs.track.TrackRepository
import javax.inject.Inject

class FavoriteTrackViewModel @Inject constructor(
    val trackRepository: TrackRepository,
    favoriteRepository: FavoriteTrackRepository,
    application: Application
) : AndroidViewModel(application) {

    val showBroadcastTrackFavoriteJoinsLiveData =
        favoriteRepository.allShowBroadcastTrackFavoriteJoins()

    val favoritedTracksLiveData: LiveData<List<TrackEntity>> =
        Transformations.map(showBroadcastTrackFavoriteJoinsLiveData) { joins ->
            joins.flatMap { j ->
                j.getTracks()
                    .filterNotNull()
                    .distinct()
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
