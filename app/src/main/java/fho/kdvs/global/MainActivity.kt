package fho.kdvs.global

import android.media.AudioManager
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.android.support.DaggerAppCompatActivity
import fho.kdvs.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_now_playing.*
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
        configureBottomSheet()
        subscribeToViewModel()
    }

    override fun onSupportNavigateUp() = navController.navigateUp()

    private fun configureBottomSheet() {
        val bottomSheetBehavior = BottomSheetBehavior.from(nowPlayingView)

        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {
                // TODO
            }

            override fun onStateChanged(p0: View, p1: Int) {
                // TODO
            }
        })
    }

    private fun subscribeToViewModel() {
        viewModel.nowPlaying.observe(this, Observer { show ->
            if (viewModel.isLiveNow.value == true) {
                nowPlayingView.apply {
                    setCurrentShowTitle(show.name)
                    setCurrentShowImage(show.defaultImageHref)
                }
            }
        })
    }
}
