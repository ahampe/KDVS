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
import fho.kdvs.global.database.FavoriteEntity
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.database.getTracks
import fho.kdvs.global.ui.LoadScreen
import fho.kdvs.global.util.TimeHelper
import kotlinx.android.synthetic.main.fragment_track_details.*
import java.lang.ref.WeakReference
import javax.inject.Inject

class FavoriteTrackDetailsFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory
    private lateinit var viewModel: FavoriteTrackDetailsViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var fragmentTrackDetailsBinding: FragmentTrackDetailsBinding

    private lateinit var tracks: List<TrackEntity?>
    private lateinit var favorites: List<FavoriteEntity>

    private var tracksViewAdapter: TracksViewAdapter? = null

    private var trackLayoutManager: LinearLayoutManager? = null

    private val snapHelper = PagerSnapHelper()

    // Simple flag for scrolling to clicked-on item view. This will only be done once, after the fragment is created.
    private var scrollingToCurrentItem = true

    private val track: TrackEntity by lazy {
        arguments?.let { FavoriteTrackDetailsFragmentArgs.fromBundle(it) }?.track
            ?: throw IllegalArgumentException("Should have passed a track to FavoriteTrackDetailsFragment")
    }
    
    private val resultIds: IntArray by lazy {
        arguments?.let { FavoriteTrackDetailsFragmentArgs.fromBundle(it) }?.ids
            ?: throw IllegalArgumentException("Should have passed an Int[] to FavoriteTrackDetailsFragment")
    }
    
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    // TODO: when entering this fragment from backnav, the recycler view sometimes does not load
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(FavoriteTrackDetailsViewModel::class.java)
            .also {
                it.initialize()
            }

        sharedViewModel = ViewModelProviders.of(this, vmFactory)
            .get(SharedViewModel::class.java)
            .also {
                it.fetchThirdPartyDataForTrack(track)
            }

        subscribeToViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentTrackDetailsBinding = FragmentTrackDetailsBinding.inflate(inflater, container, false)

        fragmentTrackDetailsBinding.apply {
            sharedVm = sharedViewModel
            trackData = track
            navController = findNavController()
            type = TrackDetailsType.FAVORITE
        }

        return fragmentTrackDetailsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LoadScreen.displayLoadScreen(WeakReference(trackDetailsRoot))

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
        viewModel.liveJoins.observe(this, Observer { joins ->
            val sortedList = mutableListOf<TrackEntity>()

            val filteredTracks = joins.getTracks()
                ?.filter { t -> t != null && resultIds.contains(t.trackId) }
                ?.distinct()

            for (id in resultIds) {
                val track = filteredTracks?.firstOrNull { t -> t?.trackId == id }
                track?.let {
                    sortedList.add(track)
                }
            }

            tracks = sortedList

            processObservedTracks(tracks)
        })
    }

    private fun processObservedTracks(tracks: List<TrackEntity?>) {
        tracks.forEach {
            it?.let {
                sharedViewModel.fetchThirdPartyDataForTrack(it)
            }
        }

        tracksViewAdapter?.onTracksChanged(tracks)

        if (scrollingToCurrentItem) {
            favoriteIcon?.let {
                setFavorite()
            }

            trackRecyclerView?.scrollToPosition(getAdjustedTrackPosition(track) ?: 0)
            scrollingToCurrentItem = false
        }

        LoadScreen.hideLoadScreen(WeakReference(trackDetailsRoot))
    }

    // Correct for airbreak slots
    private fun getAdjustedTrackPosition(track: TrackEntity): Int? {
        return tracks.indexOf(track)
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
        setShowNameAndDate(track)

        song.text = track.song ?: ""
        artistAlbum.text = when {
            track.album?.isNotBlank() == true -> resources.getString(R.string.artist_album, track.artist, track.album)
            else -> track.artist
        }

        track.comment?.let {
            comment.text = it
            comment.visibility = View.VISIBLE
        }

        if (track.year != null || track.label != null) {
            when {
                track.label == null -> albumInfo.text = track.year.toString()
                track.year == null -> albumInfo.text = track.label
                else -> albumInfo.text = albumInfo.resources.getString(R.string.album_info,
                    track.year, track.label)
            }

            albumInfo.visibility = View.VISIBLE
        } else albumInfo.visibility = View.GONE

        spotifyIcon.visibility = if (track.spotifyAlbumUri?.isNotBlank() == true) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun setShowNameAndDate(track: TrackEntity) {
        viewModel.getBroadcastForTrack(track).observe(this, Observer { b ->
            viewModel.getShowForBroadcast(b).observe(this, Observer { s ->
                showName.text = s.name
                showName.tag = s.id
                broadcastDate.text = TimeHelper.dateFormatter.format(b.date)
                broadcastDate.tag = s.id
            })
        })
    }

    private fun setFavorite() {
        if (::favorites.isInitialized && favorites.count { f -> f.trackId == track.trackId } > 0) {
            sharedViewModel.onClickFavorite(favoriteIcon, track)
        } else {
            favoriteIcon.setImageResource(R.drawable.ic_favorite_border_white_24dp)
            favoriteIcon.tag = 0
        }
    }
}
