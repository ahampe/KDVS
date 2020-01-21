package fho.kdvs.track

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.favorite.track.FavoriteTrackRepository
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.joins.ShowBroadcastTrackFavoriteJoin
import fho.kdvs.global.database.TrackEntity
import javax.inject.Inject

class FavoriteTrackDetailsViewModel @Inject constructor(
    val trackRepository: TrackRepository,
    private val broadcastRepository: BroadcastRepository,
    private val favoriteRepository: FavoriteTrackRepository,
    application: Application
) : AndroidViewModel(application) {

    lateinit var liveJoins: LiveData<List<ShowBroadcastTrackFavoriteJoin>>

    fun initialize() {
        liveJoins = favoriteRepository.allShowBroadcastTrackFavoriteJoins()
    }

    fun getBroadcastForTrack(track: TrackEntity) =
        broadcastRepository.broadcastById(track.broadcastId)

    fun getShowForBroadcast(broadcast: BroadcastEntity) =
        broadcastRepository.showByBroadcastId(broadcast.broadcastId)
}
