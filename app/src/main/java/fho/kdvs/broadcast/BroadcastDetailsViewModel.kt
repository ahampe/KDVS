package fho.kdvs.broadcast

import android.app.Application
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.navigation.NavController
import fho.kdvs.R
import fho.kdvs.favorite.FavoriteRepository
import fho.kdvs.global.database.*
import fho.kdvs.show.ShowRepository
import fho.kdvs.track.TrackRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class BroadcastDetailsViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    private val broadcastRepository: BroadcastRepository,
    private val favoriteRepository: FavoriteRepository,
    private val trackRepository: TrackRepository,
    private val favoriteDao: FavoriteDao,
    application: Application
) : AndroidViewModel(application), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    lateinit var show: LiveData<ShowEntity>
    lateinit var broadcast: LiveData<BroadcastEntity>
    private lateinit var tracksLiveData: LiveData<List<TrackEntity>>
    private lateinit var favoritesLiveData: LiveData<List<FavoriteEntity>>

    lateinit var tracksWithFavorites: MediatorLiveData<Pair<List<TrackEntity>,List<FavoriteEntity>?>>
    var favorites: List<FavoriteEntity>?= null

    fun initialize(showId: Int, broadcastId: Int) {
        fetchTracks(broadcastId)
        show = showRepository.showById(showId)
        broadcast = broadcastRepository.broadcastById(broadcastId)
        tracksLiveData = trackRepository.tracksForBroadcast(broadcastId)
        favoritesLiveData = favoriteRepository.allFavoritesByBroadcast(broadcastId)

        // Ensure that we have all tracks and favorites at same time
        tracksWithFavorites = MediatorLiveData<Pair<List<TrackEntity>,List<FavoriteEntity>?>>()
            .apply {
                var tracks: List<TrackEntity>? = null

                addSource(tracksLiveData) { trackEntities ->
                    tracks = trackEntities
                    val favoriteEntities = favorites ?: return@addSource
                    postValue(Pair(trackEntities, favoriteEntities))
                }

                addSource(favoritesLiveData) { favoriteEntities ->
                    favorites = favoriteEntities

                    val trackEntities = tracks ?: return@addSource
                    postValue(Pair(trackEntities, favoriteEntities))
                }
            }
    }

    /** Callback which plays this recorded broadcast, if it is still available */
    fun onPlayBroadcast() {
        val toPlay = broadcast.value ?: return
        val show = show.value ?: return

        broadcastRepository.playingLiveBroadcast = false
        broadcastRepository.playPastBroadcast(toPlay, show)
    }

    private fun fetchTracks(broadcastId: Int) {
        trackRepository.scrapePlaylist(broadcastId.toString())
    }


    fun onClickTrack(navController: NavController, track: TrackEntity) {
        val navAction = BroadcastDetailsFragmentDirections
            .actionBroadcastDetailsFragmentToTrackDetailsFragment(track)
        if (navController.currentDestination?.id == R.id.broadcastDetailsFragment)
            navController.navigate(navAction)
    }
}