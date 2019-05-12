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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestOptions
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
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.util.ImageHelper
import fho.kdvs.injection.ViewModelKey
import fho.kdvs.show.TrackDetailsViewModel
import kotlinx.android.synthetic.main.fragment_track_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.json.JSONObject
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

        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        subscribeToViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentTrackDetailsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    private fun subscribeToViewModel() {
        viewModel.liveTrack.observe(this, Observer { liveTrack ->
            Timber.d("Got updated track: $liveTrack")

            // TODO: replace some of these with binding adapters

            song.text = liveTrack.song

            var artistAlbumStr = artistAlbum.resources.getString(R.string.track_info_start, liveTrack.artist)
            artistAlbumStr += if (liveTrack.album.isNullOrBlank())
                                artistAlbum.resources.getString(R.string.track_info_middle,
                                    getRootLevelElmFromMetadataOfType<String>("title", liveTrack.metadata))
                            else artistAlbum.resources.getString(R.string.track_info_middle, liveTrack.album)
            artistAlbum.text = artistAlbumStr
            if (liveTrack.album.isNullOrBlank())
                artistAlbum.visibility = View.GONE

            val year = getRootLevelElmFromMetadataOfType<String>("date", liveTrack.metadata)
                ?.substring(0,4)
            val label = if (!liveTrack.label.isNullOrBlank()) liveTrack.label
                else getLabelFromMetadata(liveTrack.metadata)

            var albumInfoStr = year
            if (!label.isNullOrBlank())
                albumInfoStr += if (year.isNullOrBlank()) liveTrack.label
                    else albumInfo.resources.getString(R.string.album_info_middle, label)
            albumInfo.text = albumInfoStr

            if (!liveTrack.comment.isNullOrBlank()) {
                comment.text = comment.resources.getString(R.string.track_comments, liveTrack.comment)
                comment.visibility = View.VISIBLE
            }

            if ((liveTrack.imageHref ?: "").isNotEmpty()) {
                ImageHelper.loadImageWithGlide(artwork, liveTrack.imageHref)
            }
        })
    }

    private inline fun <reified T> getRootLevelElmFromMetadataOfType(key: String, metadata: JSONObject?): T?{
        var elm: T? = null

        if (metadata?.has(key) == true && metadata.get(key) is T)
            elm = metadata.get(key) as? T

        return elm
    }

    private fun getLabelFromMetadata(metadata: JSONObject?): String {
        var label = ""

        if (metadata?.has("label-info") == true){
            val labelInfo = metadata.getJSONArray("label-info").get(0) as? JSONObject
            if (labelInfo?.has("label") == true){
                val labelObj = labelInfo.get("label") as? JSONObject
                if (labelObj?.has("name") == true)
                    label = labelObj.getString("name")
            }
        }

        return label
    }
}