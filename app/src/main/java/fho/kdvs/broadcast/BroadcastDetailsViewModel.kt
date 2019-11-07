package fho.kdvs.broadcast

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.navigation.NavController
import fho.kdvs.R
import fho.kdvs.favorite.broadcast.FavoriteBroadcastRepository
import fho.kdvs.favorite.track.FavoriteTrackRepository
import fho.kdvs.global.database.*
import fho.kdvs.show.ShowRepository
import fho.kdvs.track.TrackRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class BroadcastDetailsViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    private val broadcastRepository: BroadcastRepository,
    private val favoriteTrackRepository: FavoriteTrackRepository,
    private val favoriteBroadcastRepository: FavoriteBroadcastRepository,
    private val trackRepository: TrackRepository,
    application: Application
) : AndroidViewModel(application), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    lateinit var showLiveData: LiveData<ShowEntity>
    lateinit var broadcastLiveData: LiveData<BroadcastEntity>
    lateinit var broadcastFavoriteLiveData: LiveData<FavoriteBroadcastEntity>
    lateinit var showWithBroadcast: MediatorLiveData<Pair<ShowEntity, BroadcastEntity>>

    lateinit var tracksLiveData: LiveData<List<TrackEntity>>
    private lateinit var trackFavoritesLiveData: LiveData<List<FavoriteTrackEntity>>
    lateinit var tracksWithFavorites: MediatorLiveData<Pair<List<TrackEntity>, List<FavoriteTrackEntity>?>>

    var trackFavorites: List<FavoriteTrackEntity>? = null

    fun initialize(showId: Int, broadcastId: Int) {
        fetchTracks(broadcastId)

        showLiveData = showRepository.showById(showId)
        broadcastLiveData = broadcastRepository.broadcastById(broadcastId)
        tracksLiveData = trackRepository.tracksForBroadcast(broadcastId)
        trackFavoritesLiveData = favoriteTrackRepository.allFavoritesByBroadcast(broadcastId)
        broadcastFavoriteLiveData = favoriteBroadcastRepository.favoriteByBroadcastId(broadcastId)

        showWithBroadcast = MediatorLiveData<Pair<ShowEntity, BroadcastEntity>>()
            .apply {
                var showEnt: ShowEntity? = null
                var broadcastEnt: BroadcastEntity? = null

                addSource(showLiveData) { show ->
                    showEnt = show
                    val broadcast = broadcastEnt ?: return@addSource
                    postValue(Pair(show, broadcast))
                }

                addSource(broadcastLiveData) { broadcast ->
                    broadcastEnt = broadcast

                    val show = showEnt ?: return@addSource
                    postValue(Pair(show, broadcast))
                }
            }

        tracksWithFavorites =
            MediatorLiveData<Pair<List<TrackEntity>, List<FavoriteTrackEntity>?>>()
                .apply {
                    var tracks: List<TrackEntity>? = null

                    addSource(tracksLiveData) { trackEntities ->
                        tracks = trackEntities

                        val favoriteEntities = trackFavorites ?: return@addSource
                        postValue(Pair(trackEntities, favoriteEntities))
                    }

                    addSource(trackFavoritesLiveData) { favoriteEntities ->
                        trackFavorites = favoriteEntities

                        val trackEntities = tracks ?: return@addSource
                        postValue(Pair(trackEntities, favoriteEntities))
                    }
                }
    }

    private fun fetchTracks(broadcastId: Int) {
        trackRepository.scrapePlaylist(broadcastId.toString())
    }

    fun onClickTrack(navController: NavController, track: TrackEntity) {
        val navAction = BroadcastDetailsFragmentDirections
            .actionBroadcastDetailsFragmentToBroadcastTrackDetailsFragment(track)
        if (navController.currentDestination?.id == R.id.broadcastDetailsFragment)
            navController.navigate(navAction)
    }
}
