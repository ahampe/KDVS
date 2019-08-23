package fho.kdvs.favorite

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavController
import fho.kdvs.R
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.track.TrackRepository
import javax.inject.Inject

class FavoriteViewModel @Inject constructor(
    val trackRepository: TrackRepository,
    private val favoriteRepository: FavoriteRepository,
    application: Application
) : AndroidViewModel(application) {

    fun getShowBroadcastTrackFavoriteJoins() = favoriteRepository.allShowBroadcastTrackFavoriteJoins()

    fun onClickTrack(navController: NavController, track: TrackEntity?, resultIds: IntArray) {
        track?.let {
            val navAction = FavoriteFragmentDirections
                .actionFavoriteFragmentToFavoriteTrackDetailsFragment(track, resultIds)
            if (navController.currentDestination?.id == R.id.favoriteFragment)
                navController.navigate(navAction)
        }
    }
}
