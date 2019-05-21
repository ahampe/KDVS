package fho.kdvs.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerControlView
import dagger.android.support.DaggerFragment
import fho.kdvs.R
import fho.kdvs.databinding.FragmentPlayerBinding
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.MainActivity
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.ui.PlayerPaletteRequestListener
import fho.kdvs.global.util.TimeHelper
import kotlinx.android.synthetic.main.exo_playback_control_view.view.*
import kotlinx.android.synthetic.main.fragment_player.*
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

class PlayerFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory
    private lateinit var viewModel: PlayerViewModel
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(PlayerViewModel::class.java)
            .also { it.initialize() }

        subscribeToViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sharedViewModel = ViewModelProviders.of(requireActivity(), vmFactory)
            .get(SharedViewModel::class.java)

        val binding = FragmentPlayerBinding.inflate(inflater, container, false)
            .apply { vm = viewModel }
        binding.lifecycleOwner = this

        val activity = activity as? MainActivity
        activity?.toggleBottomNavAndPlayerBar(false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val activity = this.activity as? MainActivity
        activity?.toggleBottomNavAndPlayerBar(true)
    }

    private fun subscribeToViewModel() {
        viewModel.broadcastLiveData.observe(this, Observer { broadcast ->
            Timber.d("got currently playing broadcast: $broadcast")
            viewModel.showLiveData.observe(this, Observer { show ->
                Timber.d("got currently playing show: $show")
                playerShowName.text = show.name
                showHost.text = show.host

                if ((broadcast.imageHref ?: "").isNotEmpty()) {
                    val parent = playing_image.parent as ConstraintLayout
                    Glide.with(playing_image)
                        .asBitmap()
                        .load(broadcast.imageHref)
                        .apply(
                            RequestOptions()
                                .error(R.drawable.show_placeholder)
                                .apply(RequestOptions.centerCropTransform())
                        )
                        .transition(BitmapTransitionOptions.withCrossFade())
                        .listener(
                            PlayerPaletteRequestListener(playing_image, parent)
                        )
                        .into(playing_image)
                }

                if (TimeHelper.isShowBroadcastLive(show, broadcast) || sharedViewModel.isLiveNow.value == null)
                    configureLiveExoPlayer(show)
                else
                    configureArchiveExoPlayer(broadcast)
            })
        })
    }

    private fun configureArchiveExoPlayer(broadcast: BroadcastEntity) {
        val formatter = TimeHelper.uiDateFormatter
        liveOrBroadcastDate.text = formatter.format(broadcast.date)

        // hide progress bar, show time bar
        customExoPlayer.progress.progressBar.visibility = View.GONE
        customExoPlayer.progress.exo_progress.visibility = View.VISIBLE

        val player = getPlayer()
        sharedViewModel.prepareExoPlayerForBroadcast(player, broadcast)
    }

    private fun configureLiveExoPlayer(show: ShowEntity) {
        val timeStart = show.timeStart
        val timeEnd = show.timeEnd

        if (timeStart == null || timeEnd == null) return

        liveOrBroadcastDate.text = resources.getString(R.string.live)

        // bind custom progress logic
        val weakPB = WeakReference<ProgressBar>(customExoPlayer.progress.progressBar)
        val progressAsyncTask = TimeProgressAsyncTask(weakPB, timeStart, timeEnd)
        progressAsyncTask.execute()

        // hide time bar, show progress bar
        customExoPlayer.progress.progressBar.visibility = View.VISIBLE
        customExoPlayer.progress.exo_progress.visibility = View.GONE

        // set time labels
        val formatter = TimeHelper.showTimeFormatter
        customExoPlayer.exo_position.text = formatter.format(show.timeStart)
        customExoPlayer.exo_duration.text = formatter.format(show.timeEnd)

        val player = getPlayer()
        sharedViewModel.prepareExoPlayerForLiveStream(player)
    }

    private fun getPlayer(): ExoPlayer {
        return ExoPlayerFactory.newSimpleInstance(
            context
        )
    }
}