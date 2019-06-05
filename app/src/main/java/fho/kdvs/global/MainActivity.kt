package fho.kdvs.global

import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.android.support.DaggerAppCompatActivity
import fho.kdvs.R
import fho.kdvs.global.extensions.isPlaying
import fho.kdvs.global.util.TimeHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_player_bar.*
import kotlinx.android.synthetic.main.view_player_bar.view.*
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: KdvsViewModelFactory

    @Inject
    lateinit var exoPlayer: ExoPlayer

    private lateinit var viewModel: SharedViewModel

    private val handler = Handler()

    private val navController: NavController by lazy {
        findNavController(R.id.nav_host_fragment)
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                navController.navigate(R.id.homeFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_schedule_grid -> {
                navController.navigate(R.id.scheduleFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings -> {
                // Settings / other stuff
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(SharedViewModel::class.java)
            .also { vm ->
                vm.updateLiveShows()
            }

        // Direct system volume controls to affect in-app volume
        volumeControlStream = AudioManager.STREAM_MUSIC

        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        subscribeToViewModel()
    }

    override fun onSupportNavigateUp() = navController.navigateUp()

    private fun subscribeToViewModel() {
        initPlayerBarIcon(this)
        initPlayerBarData(this)
    }

    private fun initPlayerBarIcon(activity: MainActivity) {
        viewModel.isPlayingAudioNow.observe(activity, Observer {
            if (it.isPlaying) {
                playerBarView
                    .playerBayPlayPause
                    .setImageResource(R.drawable.ic_pause_circle_outline_white_48dp)
            } else {
                playerBarView
                    .playerBayPlayPause
                    .setImageResource(R.drawable.ic_play_circle_outline_white_48dp)
            }
        })
    }

    private fun initPlayerBarData(activity: MainActivity) {
        viewModel.nowPlayingStreamLiveData.observe(activity, Observer { (nowPlayingShow, nowPlayingBroadcast) ->
            val timeStart = nowPlayingShow.timeStart ?: return@Observer
            val timeEnd = nowPlayingShow.timeEnd ?: return@Observer

            playerBarView.apply {
                mNavController = navController
                sharedViewModel = viewModel
                mExoPlayer = exoPlayer

                setCurrentShowName(nowPlayingShow.name)
                initButtonClickListener()

                if (sharedViewModel.isShowBroadcastLiveNow(nowPlayingShow, nowPlayingBroadcast)) {
                    val formatter = TimeHelper.showTimeFormatter
                    val timeStr = formatter.format(nowPlayingShow.timeStart) +
                        " - " +
                        formatter.format(nowPlayingShow.timeEnd)
                    setShowTimeOrBroadcastDate(timeStr)

                    initLiveProgressBar(barProgressBar, timeStart, timeEnd)
                    initLiveShow()
                } else {
                    nowPlayingBroadcast?.let {
                        setShowTimeOrBroadcastDate(TimeHelper.uiDateFormatter
                            .format(nowPlayingBroadcast.date))

                        initArchiveProgressBar()
                        initArchiveShow()
                    }
                }
            }
        })
    }

    fun initLiveProgressBar(pb: ProgressBar, timeStart: OffsetDateTime, timeEnd: OffsetDateTime) {
        val interval = TimeHelper.getDurationInSecondsBetween(timeStart, timeEnd) / 100
        val currentProgress = TimeHelper.getPercentageInDurationRelativeToNow(timeStart, timeEnd)

        handler.removeCallbacksAndMessages(null)

        pb.progress = currentProgress

        val runnable = object: Runnable {
            override fun run() {
                handler.postDelayed(this, interval.toLong() * 1000)
                pb.progress++ // TODO: for some reason this resets to 0 when switching from archive to live
            }
        }

        handler.postDelayed(runnable, 0)
    }

    private fun initArchiveProgressBar() {
        handler.removeCallbacksAndMessages(null)

        val runnable = object: Runnable {
            override fun run() {
                barProgressBar.progress = ((exoPlayer.currentPosition * 100) / exoPlayer.duration).toInt()
                handler.postDelayed(this, 1000)
            }
        }

        handler.postDelayed(runnable, 0)
    }

    fun toggleBottomNavAndPlayerBar(visible: Boolean) {
        bottomNavigation.visibility = if (visible) View.VISIBLE else View.GONE
        playerBarView.visibility    = if (visible) View.VISIBLE else View.GONE
    }
}
