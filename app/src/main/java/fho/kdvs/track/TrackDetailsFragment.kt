package fho.kdvs.track

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.DaggerFragment
import fho.kdvs.R
import fho.kdvs.databinding.FragmentTrackDetailsBinding
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.FavoriteEntity
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.ui.LoadScreen
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.global.web.uri
import kotlinx.android.synthetic.main.fragment_track_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

// TODO: Refactor this and TopMusicDetailsFragment to share overlapping code?
class TrackDetailsFragment : DaggerFragment(), CoroutineScope {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory
    private lateinit var viewModel: TrackDetailsViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var fragmentTrackDetailsBinding: FragmentTrackDetailsBinding

    private lateinit var tracks: List<TrackEntity>
    private lateinit var favorites: List<FavoriteEntity>
    private lateinit var show: ShowEntity
    private lateinit var broadcast: BroadcastEntity

    private var tracksViewAdapter: TracksViewAdapter? = null

    private var trackLayoutManager: LinearLayoutManager? = null

    private val snapHelper = PagerSnapHelper()

    // Simple flag for scrolling to clicked-on item view. This will only be done once, after the fragment is created.
    private var scrollingToCurrentItem = true

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    private val track: TrackEntity by lazy {
        arguments?.let { TrackDetailsFragmentArgs.fromBundle(it) }?.track
            ?: throw IllegalArgumentException("Should have passed a track to TrackDetailsFragment")
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(TrackDetailsViewModel::class.java)
            .also {
                it.initialize(track)
            }

        sharedViewModel = ViewModelProviders.of(this, vmFactory)
            .get(SharedViewModel::class.java)
            .also {
                it.fetchThirdPartyDataForTrack(track, viewModel.trackRepository)
            }

        subscribeToViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentTrackDetailsBinding = FragmentTrackDetailsBinding.inflate(inflater, container, false)

        fragmentTrackDetailsBinding.apply {
            vm = viewModel
            sharedVm = sharedViewModel
            trackData = track
        }

        return fragmentTrackDetailsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LoadScreen.displayLoadScreen(trackDetailsRoot)

        setTrackInfo(track)

        tracksViewAdapter = TracksViewAdapter { }
        trackLayoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

        trackRecyclerView?.run {
            adapter = tracksViewAdapter
            layoutManager = trackLayoutManager
            setHasFixedSize(true)

            onFlingListener = null
            clearOnScrollListeners()
            snapHelper.attachToRecyclerView(this)
        }

        trackRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val position = getCurrentItem()
                    onItemChanged(position)
                }
            }
        })
    }

    private fun subscribeToViewModel() {
        viewModel.combinedLiveData.observe(this, Observer { combinedData ->
            Timber.d("Got updated track details livedata")

            tracks = combinedData.tracks
            favorites = combinedData.favorites
            show = combinedData.show
            broadcast = combinedData.broadcast

            showName?.let {
                it.text = show.name
                it.setOnClickListener {
                    viewModel.onClickTrackHeader(findNavController(), show.id, broadcast.broadcastId)
                }
            }

            broadcastDate?.let {
                val formatter = TimeHelper.uiDateFormatter
                it.text = formatter.format(broadcast.date)
                it.setOnClickListener {
                    viewModel.onClickTrackHeader(findNavController(), show.id, broadcast.broadcastId)
                }
            }

            favoriteIcon?.let {
                setFavorite()
            }

            tracks.forEach {
                sharedViewModel.fetchThirdPartyDataForTrack(it, viewModel.trackRepository)
            }

            tracksViewAdapter?.onTracksChanged(tracks)

            if (scrollingToCurrentItem) {
                trackRecyclerView?.scrollToPosition(track.position ?: 0)
                scrollingToCurrentItem = false
            }

            LoadScreen.hideLoadScreen(trackDetailsRoot)
        })
    }

    private fun getCurrentItem(): Int {
        return (trackRecyclerView.layoutManager as LinearLayoutManager)
            .findFirstVisibleItemPosition()
    }

    private fun onItemChanged(position: Int) {
        if (::tracks.isInitialized) {
            val item = tracks.getOrNull(position)

            item?.let {
                setTrackInfo(it)
            }
        }
    }

    private fun setTrackInfo(track: TrackEntity) {
        setFavorite()

        song.text = track.song ?: ""
        artistAlbum.text = resources.getString(R.string.artist_album,
            track.artist,
            track.album)

        if (track.year != null || track.label != null) {
            when {
                track.label == null -> albumInfo.text = track.year.toString()
                track.year == null -> albumInfo.text = track.label
                else -> albumInfo.text = albumInfo.resources.getString(R.string.album_info,
                    track.year, track.label)
            }

            albumInfo.visibility = View.VISIBLE
        } else albumInfo.visibility = View.GONE

        spotifyIcon.visibility = if (track.spotifyData != null && !track.spotifyData.uri.isNullOrBlank())
            View.VISIBLE
        else View.GONE
    }

    private fun setFavorite() {
        if (::favorites.isInitialized && favorites.count { f -> f.trackId == track.trackId } > 0) {
            favoriteIcon.setImageResource(R.drawable.ic_favorite_white_24dp)
            favoriteIcon.tag = 1
        } else {
            favoriteIcon.setImageResource(R.drawable.ic_favorite_border_white_24dp)
            favoriteIcon.tag = 0
        }
    }
}
