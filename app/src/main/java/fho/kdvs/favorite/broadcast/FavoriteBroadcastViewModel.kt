package fho.kdvs.favorite.broadcast

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavController
import fho.kdvs.R
import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.favorite.FavoriteFragmentDirections
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowEntity
import javax.inject.Inject

class FavoriteBroadcastViewModel @Inject constructor(
    val broadcastRepository: BroadcastRepository,
    favoriteBroadcastRepository: FavoriteBroadcastRepository,
    application: Application
) : AndroidViewModel(application) {

    val showBroadcastFavoriteJoins =
        favoriteBroadcastRepository.allShowBroadcastFavoriteJoins()

    fun onClickBroadcast(navController: NavController, broadcast: BroadcastEntity?) {
        broadcast?.let {
            val navAction =
                FavoriteFragmentDirections.actionFavoriteFragmentToBroadcastDetailsFragment(
                    broadcast.showId,
                    broadcast.broadcastId
                )
            if (navController.currentDestination?.id == R.id.favoriteFragment)
                navController.navigate(navAction)
        }
    }
}
