package fho.kdvs.broadcast

import android.app.Application
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
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
    lateinit var tracks: LiveData<List<TrackEntity>>
    lateinit var favorites: List<LiveData<FavoriteEntity>>

    val favoritedTracks = mutableListOf<Int>()

    fun initialize(showId: Int, broadcastId: Int) {
        fetchTracks(broadcastId)
        show = showRepository.showById(showId)
        broadcast = broadcastRepository.broadcastById(broadcastId)
        tracks = trackRepository.tracksForBroadcast(broadcastId)
    }

    /** Callback which plays this recorded broadcast, if it is still available */
    fun onPlayBroadcast() {
        val toPlay = broadcast.value ?: return
        val show = show.value ?: return

        broadcastRepository.playPastBroadcast(toPlay, show)
    }

    private fun fetchTracks(broadcastId: Int) {
        trackRepository.scrapePlaylist(broadcastId.toString())
    }

    fun getFavoritesForTracks(tracks: List<TrackEntity>) {
        favorites = favoriteRepository.favoritesForTracks(tracks)
    }

    fun onClickFavorite(view: View, trackId: Int) {
        val imageView = view as? ImageView

        if (imageView?.tag == 0) {
            imageView.setImageResource(R.drawable.ic_favorite_white_24dp)
            imageView.tag = 1
            launch { favoriteDao.insert(FavoriteEntity(0, trackId)) }
        } else if (imageView?.tag == 1) {
            imageView.setImageResource(R.drawable.ic_favorite_border_white_24dp)
            imageView.tag = 0
            launch { favoriteDao.deleteByTrackId(trackId) }
        }
    }

    fun onClickTrack(navController: NavController, track: TrackEntity) {
        val navAction = BroadcastDetailsFragmentDirections
            .actionBroadcastDetailsFragmentToTrackDetailsFragment(track)
        if (navController.currentDestination?.id == R.id.broadcastDetailsFragment)
            navController.navigate(navAction)
    }
}