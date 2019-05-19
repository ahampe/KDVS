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
import fho.kdvs.global.util.TimeHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.player_bar_view.*
import kotlinx.android.synthetic.main.player_bar_view.view.*
import org.threeten.bp.OffsetDateTime
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

        viewModel.nowPlayingStreamLiveData.observe(fragment, Observer { (nowPlayingShow, nowPlayingBroadcast) ->
            if (nowPlayingBroadcast?.date != null &&
                nowPlayingShow.timeStart != null &&
                nowPlayingShow.timeEnd != null) {

                playerBarView.apply {
                    setCurrentShowName(nowPlayingShow.name)
                    initButtonClickListener(viewModel)
                    mNavController = navController

                    if (previewPlayPauseIcon != null)
                        viewModel.nowPlayingPreviewPlayButton = previewPlayPauseIcon

                    if (TimeHelper.isShowBroadcastLive(nowPlayingShow, nowPlayingBroadcast)) {
                        val formatter = TimeHelper.showTimeFormatter
                        val timeStr = formatter.format(nowPlayingShow.timeStart) +
                            " - " +
                            formatter.format(nowPlayingShow.timeEnd)

                        setShowTimeOrBroadcastDate(timeStr)
                        initLiveShow()
                    } else {
                        val formatter = TimeHelper.uiDateFormatter
                        setShowTimeOrBroadcastDate(formatter.format(nowPlayingBroadcast.date))

                        initArchiveShow()
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
