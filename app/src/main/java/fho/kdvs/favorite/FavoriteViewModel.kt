package fho.kdvs.favorite

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavController
import fho.kdvs.R
import fho.kdvs.global.database.TrackEntity
import javax.inject.Inject

class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    application: Application
) : AndroidViewModel(application) {

    fun getFavoritedTracks() = favoriteRepository.allFavoritedTracks()

    fun onClickTrack(navController: NavController, track: TrackEntity?) {
        track?.let {
            val navAction = FavoriteFragmentDirections
                .actionFavoriteFragmentToTrackDetailsFragment(track)
            if (navController.currentDestination?.id == R.id.favoriteFragment)
                navController.navigate(navAction)
        }
    }
}
