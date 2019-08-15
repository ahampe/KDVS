package fho.kdvs.track

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.DaggerFragment
import fho.kdvs.R
import fho.kdvs.databinding.FragmentTrackDetailsBinding
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.ui.LoadScreen
import fho.kdvs.global.util.ImageHelper
import fho.kdvs.global.util.TimeHelper
import kotlinx.android.synthetic.main.fragment_track_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class TrackDetailsFragment : DaggerFragment(), CoroutineScope {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory
    private lateinit var viewModel: TrackDetailsViewModel
    private lateinit var sharedViewModel: SharedViewModel

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    // Retrieves the timeslot from the arguments bundle. Throws an exception if it doesn't exist.
    private val track: TrackEntity by lazy {
        arguments?.let { TrackDetailsFragmentArgs.fromBundle(it) }?.track
            ?: throw IllegalArgumentException("Should have passed a track to TrackDetailsFragment")
    }

    override fun onAttach(context: Context?) {
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

        subscribeToViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentTrackDetailsBinding.inflate(inflater, container, false)
        binding.apply {
            vm = viewModel
            sharedVm = sharedViewModel
            trackData = track
        }
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LoadScreen.displayLoadScreen(trackDetailsRoot)
    }

    private fun subscribeToViewModel() {
        var musicBrainzFetchComplete = false
        var spotifyFetchComplete = false
        
        val fragment = this

        viewModel.run {
            favorite.observe(fragment, Observer { favorite ->
                if (favorite != null && favorite.trackId != -1) {
                    favoriteIcon.setImageResource(R.drawable.ic_favorite_white_24dp)
                    favoriteIcon.tag = 1
                } else {
                    favoriteIcon.setImageResource(R.drawable.ic_favorite_border_white_24dp)
                    favoriteIcon.tag = 0
                }
            })

            broadcast.observe(fragment, Observer { broadcast ->
                if (broadcast.date != null) {
                    val formatter = TimeHelper.uiDateFormatter
                    broadcastDate.text = formatter.format(broadcast.date)
                }
            })

            show.observe(fragment, Observer { show ->
                showName.text = show.name
            })

            liveTrack.observe(fragment, Observer { liveTrack ->
                Timber.d("Got updated track: $liveTrack")

                if (liveTrack.hasScrapedMetadata) {
                    musicBrainzFetchComplete = true
                    if (spotifyFetchComplete)
                        LoadScreen.hideLoadScreen(trackDetailsRoot)
                }

                val spotifyUri = liveTrack.spotifyUri
                if (spotifyUri != null) {
                    spotifyFetchComplete = true
                    if (musicBrainzFetchComplete)
                        LoadScreen.hideLoadScreen(trackDetailsRoot)

                    if (spotifyUri.isNotEmpty()) {
                        spotifyIcon.setOnClickListener {
                            Timber.d("Spotify icon clicked for ${liveTrack?.song}")
                            sharedViewModel.openSpotify(spotifyIcon, spotifyUri)
                        }
                        spotifyIcon.visibility = View.VISIBLE
                    }
                }

                // TODO: replace some of these with binding adapters

                song.text = liveTrack.song
                song.isSelected = true

                if (liveTrack.album.isNullOrBlank())
                    artistAlbum.text = liveTrack.artist
                else
                    artistAlbum.text = artistAlbum.resources.getString(R.string.artist_album,
                        liveTrack.artist, liveTrack.album)

                when {
                    liveTrack.year == null && liveTrack.label == null -> albumInfo.visibility = View.GONE
                    liveTrack.label == null -> albumInfo.text = liveTrack.year.toString()
                    liveTrack.year == null -> albumInfo.text = liveTrack.label
                    else -> {
                        albumInfo.text = albumInfo.resources.getString(
                            R.string.album_info,
                            liveTrack.year, liveTrack.label
                        )
                    }
                }

                if (!liveTrack.comment.isNullOrBlank()) {
                    comment.text = comment.resources.getString(R.string.track_comments, liveTrack.comment)
                    comment.visibility = View.VISIBLE
                }

                if ((liveTrack.imageHref ?: "").isNotEmpty()) {
                    ImageHelper.loadImageWithGlide(artwork, liveTrack.imageHref)
                }
            })
        }
    }
}
