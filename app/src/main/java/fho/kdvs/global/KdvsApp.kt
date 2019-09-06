package fho.kdvs.global

import android.util.Log
import android.util.Log.INFO
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import fho.kdvs.BuildConfig
import fho.kdvs.api.endpoint.SpotifyEndpoint
import fho.kdvs.api.endpoint.YouTubeEndpoint
import fho.kdvs.global.util.Keys
import fho.kdvs.injection.DaggerAppComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class KdvsApp : DaggerApplication(), CoroutineScope {

    @Inject
    internal lateinit var spotifyEndpoint: SpotifyEndpoint

    @Inject
    internal lateinit var youTubeEndpoint: YouTubeEndpoint

    internal val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().create(this)
    }

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)

        // Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(KdvsDebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }

        launch {
            val authResponse = spotifyEndpoint.authorizeApp()
            authResponse.body()?.token?.let {
                Keys.spotfiyAuthToken = it
            }
        }
    }

    private class KdvsDebugTree : Timber.DebugTree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            return super.log(priority, tag, "$PREFIX$message", t)
        }
    }

    /** A tree which logs important information for crash reporting.  */
    private class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }

            // TODO: log with crash library
            // FakeCrashLibrary.log(priority, tag, message)

            if (t != null) {
                if (priority == Log.ERROR) {
                    // FakeCrashLibrary.logError(t)
                } else if (priority == Log.WARN) {
                    // FakeCrashLibrary.logWarning(t)
                }
            }
        }

        override fun isLoggable(tag: String?, priority: Int): Boolean {
            return priority >= INFO
        }
    }

    companion object {
        const val PREFIX = "[WDHMBT]"
    }
}
