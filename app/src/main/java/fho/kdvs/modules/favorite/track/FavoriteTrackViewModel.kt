package fho.kdvs.modules.favorite.track

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavController
import fho.kdvs.R
import fho.kdvs.favorite.FavoriteFragmentDirections
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

    val showBroadcastTrackFavoriteJoins =
        favoriteRepository.allShowBroadcastTrackFavoriteJoins()

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
