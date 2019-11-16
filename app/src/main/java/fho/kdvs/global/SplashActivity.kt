package fho.kdvs.global

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.DaggerAppCompatActivity
import fho.kdvs.R
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.URLs
import fho.kdvs.global.web.ConnectionManager
import fho.kdvs.home.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


const val SPLASH_TIMEOUT = 5000L

class SplashActivity : DaggerAppCompatActivity(), CoroutineScope {
    @Inject
    lateinit var viewModelFactory: KdvsViewModelFactory

    @Inject
    lateinit var kdvsPreferences: KdvsPreferences

    @Inject
    lateinit var connectionManager: ConnectionManager

    internal val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    private lateinit var homeViewModel: HomeViewModel

    /**
     * Fetch essential data with timeout during splash to minimize UI pop-in.
     * Timeout with connection error toast.
     * */
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_launcher)
        super.onCreate(savedInstanceState)

        homeViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(HomeViewModel::class.java)

        homeViewModel.fetchHomeData()
            .observe(this, Observer { isDataScraped ->
                if (isDataScraped) {
                    startMainActivity()
                }
            })

        Handler().postDelayed({
            Timber.d("Splash timed out")
            startMainActivity()
        }, SPLASH_TIMEOUT)
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

        finish()

        Timber.d("Starting main activity")
    }


}