package fho.kdvs.track

import android.app.Application
import android.view.View
import androidx.lifecycle.*
import androidx.navigation.NavController
import com.bumptech.glide.manager.Lifecycle
import fho.kdvs.R
import fho.kdvs.broadcast.BroadcastRepository
import fho.kdvs.favorite.FavoriteRepository
import fho.kdvs.global.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@kotlinx.serialization.UnstableDefault
class TrackDetailsViewModel @Inject constructor(
    val trackRepository: TrackRepository,
    private val broadcastRepository: BroadcastRepository,
    private val favoriteRepository: FavoriteRepository,
    application: Application
) : AndroidViewModel(application), CoroutineScope {

    private lateinit var liveTracks: LiveData<List<TrackEntity>>
    private lateinit var liveFavorites: LiveData<List<FavoriteEntity>>
    private lateinit var liveBroadcast: LiveData<BroadcastEntity>
    private lateinit var liveShow: LiveData<ShowEntity>

    data class CombinedTrackData (
        val tracks: List<TrackEntity>,
        val favorites: List<FavoriteEntity>,
        val broadcast: BroadcastEntity,
        val show: ShowEntity
    )

    lateinit var combinedLiveData: MediatorLiveData<CombinedTrackData>

    lateinit var liveJoins: LiveData<List<ShowBroadcastTrackFavoriteJoin>>

    lateinit var navController: NavController

    private val parentJob = Job()
    override val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.IO

    fun initialize(track: TrackEntity) {
        liveTracks = trackRepository.songsForBroadcast(track.broadcastId)
        liveFavorites = favoriteRepository.allFavoritesByBroadcast(track.broadcastId)
        liveBroadcast = broadcastRepository.broadcastById(track.broadcastId)
        liveShow = broadcastRepository.showByBroadcastId(track.broadcastId)

        combinedLiveData = MediatorLiveData<CombinedTrackData>()
            .apply {
                var tracks: List<TrackEntity>? = null
                var favorites: List<FavoriteEntity>? = null
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

    fun initializeForFavorites() {
        liveJoins = favoriteRepository.allShowBroadcastTrackFavoriteJoins()
    }

    fun getBroadcastForTrack(track: TrackEntity) = broadcastRepository.broadcastById(track.broadcastId)

    fun getShowForBroadcast(broadcast: BroadcastEntity) = broadcastRepository.showByBroadcastId(broadcast.broadcastId)

    fun onClickTrackHeader(view: View, track: TrackEntity) {
        val showId = view.tag as? Int?

        if (::navController.isInitialized && showId != null) {
            val navAction = TrackDetailsFragmentDirections
                .actionTrackDetailsFragmentToBroadcastDetailsFragment(showId, track.broadcastId)
            if (navController.currentDestination?.id == R.id.trackDetailsFragment)
                navController.navigate(navAction)
        }
    }
}