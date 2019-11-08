package fho.kdvs.global

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.DaggerAppCompatActivity
import fho.kdvs.R
import fho.kdvs.global.extensions.callFunctionOnTimeout
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.home.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


class SplashActivity : DaggerAppCompatActivity(), CoroutineScope {
    @Inject
    lateinit var viewModelFactory: KdvsViewModelFactory

    @Inject
    lateinit var kdvsPreferences: KdvsPreferences

    internal val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    private lateinit var homeViewModel: HomeViewModel

    /** Fetch essential data with timeout during splash to minimize UI pop-in. */
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_launcher)
        super.onCreate(savedInstanceState)

        homeViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(HomeViewModel::class.java)

        var finished = false

        val callOnTimeout = {
            Toast.makeText(
                applicationContext,
                "Error retrieving station info. Please check your connection.",
                Toast.LENGTH_LONG
            ).show()

            startMainActivity()
        }

        homeViewModel.fetchHomeData()
            .callFunctionOnTimeout(10000, callOnTimeout)
            .observe(this, Observer {
                if (!finished) {
                    startMainActivity()

                    finished = true
                }
            })
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

        finish()

        Timber.d("Starting main activity")
    }
}