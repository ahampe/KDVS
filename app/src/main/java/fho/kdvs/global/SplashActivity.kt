package fho.kdvs.global

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.DaggerAppCompatActivity
import fho.kdvs.R
import fho.kdvs.home.HomeViewModel
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


class SplashActivity : DaggerAppCompatActivity(), CoroutineScope {
    @Inject
    lateinit var viewModelFactory: KdvsViewModelFactory

    internal val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    private lateinit var homeViewModel: HomeViewModel

    /** Fetch essential data with timeout during splash to minimize UI pop-in. */
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_launcher)
        super.onCreate(savedInstanceState)

        val activity = this

        homeViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(HomeViewModel::class.java)

        runBlocking {
            withTimeoutOrNull(8000L) {
                withContext(activity.coroutineContext) { homeViewModel.fetchHomeData() }.observe(activity, Observer {
                    Timber.d("Splash pre-load complete")
                    return@Observer
                })
            }
        }

        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)

        finish()
    }
}