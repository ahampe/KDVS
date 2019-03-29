package fho.kdvs.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import dagger.android.support.DaggerFragment
import fho.kdvs.databinding.FragmentHomeBinding
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import fho.kdvs.R
import kotlinx.android.synthetic.main.exo_playback_control_view.*
import kotlinx.android.synthetic.main.fragment_home.*
import javax.inject.Inject

class HomeFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    @Inject
    lateinit var exoPlayer: ExoPlayer

    private lateinit var viewModel: SharedViewModel
    private lateinit var binding: FragmentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(requireActivity(), vmFactory)
            .get(SharedViewModel::class.java)

        subscribeToViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@HomeFragment
            vm = viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exoPlayerView.player = exoPlayer

        initExoPlayer()
    }

    private fun subscribeToViewModel() {
        viewModel.currentShow.observe(this, Observer { show ->
            binding.currentShow = show
            binding.executePendingBindings()
            showName.isSelected = true
        })

        viewModel.nextShow.observe(this, Observer { show ->
            binding.nextShow = show
            binding.executePendingBindings()
        })

        viewModel.currentBroadcast.observe(this, Observer { broadcast ->

        })
    }

    // TODO: refactor exoPlayer functions to be activity-wide
    // TODO: fix glitch with exoPlayer disappearing
    private fun initExoPlayer() {
        exoTimeBar.visibility = View.GONE

        exoPlayerView.player.addListener(object: Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playWhenReady && playbackState == Player.STATE_READY) { // stream is playing
                    exo_play_pause.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp)
                } else if (playWhenReady) { // idle, buffering, or ended
                    exo_play_pause.setImageResource(R.drawable.ic_play_circle_outline_white_48dp)
                } else { // paused
                    exo_play_pause.setImageResource(R.drawable.ic_play_circle_outline_white_48dp)
                }
            }
        })

        initExoPlayerButtons()
    }

    private fun initExoPlayerButtons() {
        exo_stop.setOnClickListener { viewModel.stopPlayback() }
        exo_play_pause.setOnClickListener { viewModel.playOrPausePlayback() }
        exo_live.setOnClickListener { viewModel.changeToKdvsOgg() }
    }
}

