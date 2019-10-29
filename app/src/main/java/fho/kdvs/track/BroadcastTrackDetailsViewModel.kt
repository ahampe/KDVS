package fho.kdvs.track

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.navigation.NavController
import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.favorite.FavoriteRepository
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.FavoriteTrackEntity
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.database.TrackEntity
import javax.inject.Inject

class BroadcastTrackDetailsViewModel @Inject constructor(
    val trackRepository: TrackRepository,
    private val broadcastRepository: BroadcastRepository,
    private val favoriteRepository: FavoriteRepository,
    application: Application
) : AndroidViewModel(application) {

    private lateinit var liveTracks: LiveData<List<TrackEntity>>
    private lateinit var liveFavorites: LiveData<List<FavoriteTrackEntity>>
    private lateinit var liveBroadcast: LiveData<BroadcastEntity>
    private lateinit var liveShow: LiveData<ShowEntity>

    data class CombinedTrackData (
        val tracks: List<TrackEntity>,
        val favorites: List<FavoriteTrackEntity>,
        val broadcast: BroadcastEntity,
        val show: ShowEntity
    )

    lateinit var combinedLiveData: MediatorLiveData<CombinedTrackData>

    lateinit var navController: NavController

    fun initialize(track: TrackEntity) {
        liveTracks = trackRepository.songsForBroadcast(track.broadcastId)
        liveFavorites = favoriteRepository.allFavoritesByBroadcast(track.broadcastId)
        liveBroadcast = broadcastRepository.broadcastById(track.broadcastId)
        liveShow = broadcastRepository.showByBroadcastId(track.broadcastId)

        combinedLiveData = MediatorLiveData<CombinedTrackData>()
            .apply {
                var tracks: List<TrackEntity>? = null
                var favorites: List<FavoriteTrackEntity>? = null
                var broadcast: BroadcastEntity? = null
                var show: ShowEntity? = null

                addSource(liveTracks) { trackEntities ->
                    tracks = trackEntities

                    val favoriteEntities = favorites ?: return@addSource
                    val broadcastEntity = broadcast ?: return@addSource
                    val showEntity = show ?: return@addSource

                    postValue(CombinedTrackData(
                        trackEntities,
                        favoriteEntities,
                        broadcastEntity,
                        showEntity
                    ))
                }

                addSource(liveFavorites) { favoriteEntities ->
                    favorites = favoriteEntities

                    val trackEntities = tracks ?: return@addSource
                    val broadcastEntity = broadcast ?: return@addSource
                    val showEntity = show ?: return@addSource

                    postValue(CombinedTrackData(
                        trackEntities,
                        favoriteEntities,
                        broadcastEntity,
                        showEntity
                    ))
                }

                addSource(liveBroadcast) { broadcastEntity ->
                    broadcast = broadcastEntity

                    val trackEntities = tracks ?: return@addSource
                    val favoriteEntities = favorites ?: return@addSource
                    val showEntity = show ?: return@addSource

                    postValue(CombinedTrackData(
                        trackEntities,
                        favoriteEntities,
                        broadcastEntity,
                        showEntity
                    ))
                }

                addSource(liveShow) { showEntity ->
                    show = showEntity

                    val trackEntities = tracks ?: return@addSource
                    val favoriteEntities = favorites ?: return@addSource
                    val broadcastEntity = broadcast ?: return@addSource

                    postValue(CombinedTrackData(
                        trackEntities,
                        favoriteEntities,
                        broadcastEntity,
                        showEntity
                    ))
                }
            }
    }


}
