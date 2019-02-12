package fho.kdvs.global

import android.util.Log
import android.util.Log.INFO
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import fho.kdvs.BuildConfig
import fho.kdvs.injection.DaggerAppComponent
import timber.log.Timber

class KdvsApp : DaggerApplication() {

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