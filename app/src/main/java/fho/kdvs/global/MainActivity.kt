package fho.kdvs.global

import android.media.AudioManager
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.android.support.DaggerAppCompatActivity
import fho.kdvs.R
import fho.kdvs.global.extensions.isPlaying
import fho.kdvs.global.util.TimeHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.player_bar_view.*
import kotlinx.android.synthetic.main.player_bar_view.view.*
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: KdvsViewModelFactory

    private lateinit var viewModel: SharedViewModel

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
        //configureBottomSheet()
        subscribeToViewModel()
    }

    override fun onSupportNavigateUp() = navController.navigateUp()

    private fun subscribeToViewModel() {
        val fragment = this

        viewModel.isPlayingAudioNow.observe(fragment, Observer {
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

        viewModel.nowPlayingStreamLiveData.observe(fragment, Observer { (nowPlayingShow, nowPlayingBroadcast) ->
            val timeStart = nowPlayingShow.timeStart
            val timeEnd = nowPlayingShow.timeEnd

            if (timeStart != null && timeEnd != null) {
                playerBarView.apply {
                    mNavController = navController
                    sharedViewModel = viewModel

                    setCurrentShowName(nowPlayingShow.name)
                    initButtonClickListener()
                    initProgressBar(timeStart, timeEnd)

                    if (viewModel.isLiveNow.value == null ||
                        (nowPlayingBroadcast != null &&
                                TimeHelper.isShowBroadcastLive(nowPlayingShow, nowPlayingBroadcast))) {
                        val formatter = TimeHelper.showTimeFormatter
                        val timeStr = formatter.format(nowPlayingShow.timeStart) +
                            " - " +
                            formatter.format(nowPlayingShow.timeEnd)

                        setShowTimeOrBroadcastDate(timeStr)
                        initLiveShow()
                    } else {
                        nowPlayingBroadcast?.let {
                            val formatter = TimeHelper.uiDateFormatter
                            setShowTimeOrBroadcastDate(formatter.format(nowPlayingBroadcast.date))

                            initArchiveShow()
                        }
                    }
                }
            }
        })
    }

    fun toggleBottomNavAndPlayerBar(visible: Boolean) {
        bottomNavigation.visibility = if (visible) {
            View.VISIBLE
        } else {
            View.GONE
        }

        playerBarView.visibility = if (visible) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}
