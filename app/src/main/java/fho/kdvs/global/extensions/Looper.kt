package fho.kdvs.global.extensions

import android.os.Build
import android.os.Looper

/** Utility method which returns true if called from the main thread */
fun isMainThread(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Looper.getMainLooper().isCurrentThread
    } else {
        Thread.currentThread() === Looper.getMainLooper().thread
    }
}