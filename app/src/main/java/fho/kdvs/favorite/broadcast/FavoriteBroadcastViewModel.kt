package fho.kdvs.favorite.broadcast

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavController
import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.global.database.BroadcastEntity
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
            // TODO
        }
    }
}
