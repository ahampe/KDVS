package fho.kdvs.global

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.DaggerAppCompatActivity
import fho.kdvs.R
import fho.kdvs.home.HomeViewModel
import fho.kdvs.schedule.ScheduleViewModel
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class SplashActivity : DaggerAppCompatActivity(), CoroutineScope {
    @Inject
    lateinit var viewModelFactory: KdvsViewModelFactory

    internal val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var scheduleViewModel: ScheduleViewModel
    private lateinit var sharedViewModel: SharedViewModel

    /** Fetch essential data with timeout during splash to minimize UI pop-in. */
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        val context = this

        homeViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(HomeViewModel::class.java)

        scheduleViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(ScheduleViewModel::class.java)

        sharedViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(SharedViewModel::class.java)
            .also { vm ->
                vm.updateLiveShows()
            }

        runBlocking {
            withTimeoutOrNull(5000L) {
                val deferredHomeData = async{ homeViewModel.fetchHomeData() }
                val deferredScheduleData = async{ scheduleViewModel.fetchShows() }

                deferredHomeData.await()
                deferredScheduleData.await()
            }
        }

        val intent = Intent(context, MainActivity::class.java)
        startActivity(intent)

        finish()
    }
}