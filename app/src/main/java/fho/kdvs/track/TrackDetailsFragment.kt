package fho.kdvs.track

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.multibindings.IntoMap
import fho.kdvs.R
import fho.kdvs.databinding.FragmentTrackDetailsBinding
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.PerFragment
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.util.ImageHelper
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.global.web.Spotify
import fho.kdvs.injection.ViewModelKey
import kotlinx.android.synthetic.main.fragment_track_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@Module
abstract class TrackDetailsUiModule {

    @Binds
    abstract fun bindViewModelFactory(vmFactory: KdvsViewModelFactory): ViewModelProvider.Factory

    @PerFragment
    @ContributesAndroidInjector(modules = [(TrackDetailsModule::class)])
    abstract fun contributeTrackDetailsFragment(): TrackDetailsFragment
}

@Module
abstract class TrackDetailsModule: ViewModel() {

    @Binds
    @IntoMap
    @PerFragment
    @ViewModelKey(TrackDetailsViewModel::class)
    abstract fun bindViewModel(viewModel: TrackDetailsModule): ViewModel
}

class TrackDetailsFragment : BottomSheetDialogFragment(), CoroutineScope {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory
    private lateinit var viewModel: TrackDetailsViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var spotify: Spotify

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val root = LinearLayout(activity)
        root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        root.setPadding(0,0,0,0)

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        return dialog
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

        spotify = Spotify(viewModel.trackRepository, sharedViewModel)

        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        subscribeToViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentTrackDetailsBinding.inflate(inflater, container, false)
        binding.apply {
            vm = viewModel
            sharedVm = sharedViewModel
            spotifyObj = spotify
            trackData = track
        }
        binding.lifecycleOwner = this
        return binding.root
    }

    private fun subscribeToViewModel() {
        viewModel.favorite.observe(this, Observer { favorite ->
            if (favorite != null && favorite.trackId != -1) {
                favoriteIcon.setImageResource(R.drawable.ic_favorite_white_24dp)
                favoriteIcon.tag = 1
            } else {
                favoriteIcon.setImageResource(R.drawable.ic_favorite_border_white_24dp)
                favoriteIcon.tag = 0
            }
        })

        viewModel.broadcast.observe(this, Observer { broadcast ->
            if (broadcast.date != null) {
                val formatter = TimeHelper.uiDateFormatter
                broadcastDate.text = formatter.format(broadcast.date)
            }
        })

        viewModel.show.observe(this, Observer { show ->
            showName.text = show.name
        })

        viewModel.liveTrack.observe(this, Observer { liveTrack ->
            Timber.d("Got updated track: $liveTrack")

            if (liveTrack.spotifyUri?.isNotEmpty() == true) {
                spotifyIcon.setOnClickListener {
                    val spotifyUri = liveTrack.spotifyUri
                    Timber.d("Spotify icon clicked for ${liveTrack?.song}")
                    spotify.openSpotify(spotifyIcon, spotifyUri!!)
                }
                spotifyIcon.visibility = View.VISIBLE
            }


            // TODO: replace some of these with binding adapters

            song.text = liveTrack.song
            song.isSelected = true

            if (liveTrack.album.isNullOrBlank())
                artistAlbum.text = liveTrack.artist
            else
                artistAlbum.text = artistAlbum.resources.getString(R.string.artist_album,
                    liveTrack.artist, liveTrack.album)

            if (liveTrack.year != null || liveTrack.label != null) {
                if (liveTrack.label == null) {
                    albumInfo.text = liveTrack.year.toString()
                } else if (liveTrack.year == null) {
                    albumInfo.text = liveTrack.label
                } else {
                    albumInfo.text = albumInfo.resources.getString(R.string.album_info,
                        liveTrack.year, liveTrack.label)
                }
                albumInfo.visibility = View.VISIBLE
            } else albumInfo.visibility = View.GONE

            if (!liveTrack.comment.isNullOrBlank()) {
                comment.text = comment.resources.getString(R.string.track_comments, liveTrack.comment)
                comment.visibility = View.VISIBLE
            }

            if ((liveTrack.imageHref ?: "").isNotEmpty()) {
                ImageHelper.loadImageWithGlide(artwork, liveTrack.imageHref)
                //ImageHelper.loadImageAndReflectionWithGlide(artwork, liveTrack.imageHref) // TODO: finish setting this up
            }

        })
    }
}