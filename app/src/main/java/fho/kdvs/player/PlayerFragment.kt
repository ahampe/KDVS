package fho.kdvs.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.ExoPlayer
import dagger.android.support.DaggerFragment
import fho.kdvs.R
import fho.kdvs.databinding.FragmentPlayerBinding
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.MainActivity
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.extensions.isPlaying
import fho.kdvs.global.ui.PlayerPaletteRequestListener
import fho.kdvs.global.util.TimeHelper
import kotlinx.android.synthetic.main.exo_playback_control_view.view.*
import kotlinx.android.synthetic.main.exo_player_archive_controls.view.*
import kotlinx.android.synthetic.main.exo_player_live_controls.view.*
import kotlinx.android.synthetic.main.fragment_player.*
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

class PlayerFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    @Inject
    lateinit var exoPlayer: ExoPlayer

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

        subscribeToSharedViewModel()

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

    override fun onResume() {
        super.onResume()
        subscribeToViewModel()
    }

    private fun subscribeToViewModel() {
        viewModel.nowPlayingLiveData.observe(this, Observer { (show, broadcast) ->
            Timber.d("got currently playing show: $show and broadcast: $broadcast")

            playerShowName.text = show.name
            showHost.text = show.host

            liveOrBroadcastDate.setOnClickListener { viewModel.onClickShowInfo(findNavController(), show) }
            playerShowName.setOnClickListener { viewModel.onClickShowInfo(findNavController(), show) }
            showHost.setOnClickListener { viewModel.onClickShowInfo(findNavController(), show) }
            viewPlaylist.setOnClickListener { viewModel.onClickPlaylist(findNavController(), broadcast) }
            star.setOnClickListener { viewModel.onClickStar() }
            arrow.setOnClickListener { fragmentManager?.popBackStack() }

            val imageHref = broadcast?.imageHref ?: show.defaultImageHref
            imageHref?.let {
                val parent = playing_image.parent as ConstraintLayout
                Glide.with(playing_image)
                    .asBitmap()
                    .load(imageHref)
                    .apply(
                        RequestOptions()
                            .error(R.drawable.show_placeholder)
                            .apply(RequestOptions.centerCropTransform())
                    )
                    .transition(BitmapTransitionOptions.withCrossFade())
                    .listener(
                        PlayerPaletteRequestListener(parent)
                    )
                    .into(playing_image)
            }

            if (broadcast == null ||
                TimeHelper.isShowBroadcastLive(show, broadcast) ||
                sharedViewModel.isLiveNow.value == null)
                configureLiveExoPlayer(show)
            else
                configureArchiveExoPlayer(broadcast)
        })
    }

    private fun subscribeToSharedViewModel() {
        sharedViewModel.isPlayingAudioNow.observe(this, Observer {
            if (it.isPlaying) {
                archiveControls
                    ?.exo_play_pause_archive
                    ?.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp)
                liveControls
                    ?.exo_play_pause_live
                    ?.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp)
            } else {
                archiveControls
                    ?.exo_play_pause_archive
                    ?.setImageResource(R.drawable.ic_play_circle_outline_white_48dp)
                liveControls
                    ?.exo_play_pause_live
                    ?.setImageResource(R.drawable.ic_play_circle_outline_white_48dp)
            }
        })
    }

    private fun configureArchiveExoPlayer(broadcast: BroadcastEntity) {
        val formatter = TimeHelper.uiDateFormatter
        liveOrBroadcastDate.text = formatter.format(broadcast.date)

        // hide progress bar, show time bar
        customExoPlayer.progress.progressBar.visibility = View.GONE
        customExoPlayer.progress.exo_progress.visibility = View.VISIBLE

        customExoPlayer.player = exoPlayer
        customExoPlayer.showTimeoutMs = 0

        customExoPlayer.timeStartLabel.visibility = View.GONE
        customExoPlayer.timeEndLabel.visibility = View.GONE
        customExoPlayer.exo_position.visibility = View.VISIBLE
        customExoPlayer.exo_duration.visibility = View.VISIBLE

        archiveControls.exo_back30.setOnClickListener {
            sharedViewModel.jumpBack30Seconds(exoPlayer)
        }
        archiveControls.exo_play_pause_archive.setOnClickListener {
            sharedViewModel.playOrPausePlayback()
        }
        archiveControls.exo_forward30.setOnClickListener {
            sharedViewModel.jumpForward30Seconds(exoPlayer)
        }

        archiveControls.visibility = View.VISIBLE
        liveControls.visibility = View.GONE
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

        customExoPlayer.player = exoPlayer
        customExoPlayer.showTimeoutMs = 0

        // hide time bar, show progress bar
        customExoPlayer.progress.progressBar.visibility = View.VISIBLE
        customExoPlayer.progress.exo_progress.visibility = View.GONE

        // set time labels
        customExoPlayer.timeStartLabel.text = TimeHelper.showTimeFormatter.format(show.timeStart)
        customExoPlayer.timeEndLabel.text = TimeHelper.showTimeFormatter.format(show.timeEnd)
        customExoPlayer.timeStartLabel.visibility = View.VISIBLE
        customExoPlayer.timeEndLabel.visibility = View.VISIBLE
        customExoPlayer.exo_position.visibility = View.GONE
        customExoPlayer.exo_duration.visibility = View.GONE

        liveControls.exo_stop.setOnClickListener {
            sharedViewModel.stopPlayback()
        }
        liveControls.exo_play_pause_live.setOnClickListener {
            sharedViewModel.playOrPausePlayback()
        }
        liveControls.exo_live.setOnClickListener {
            sharedViewModel.jumpToLivePlayback()
        }

        archiveControls.visibility = View.GONE
        liveControls.visibility = View.VISIBLE
    }
}