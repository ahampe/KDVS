package fho.kdvs.player

import android.os.AsyncTask
import android.os.SystemClock
import android.widget.ProgressBar
import org.threeten.bp.OffsetDateTime
import java.lang.ref.WeakReference


/** Custom [ProgressBar] logic for live [PlayerFragment] / [PlayerBarView], in order to represent real-time duration. */
open class TimeProgressAsyncTask(
    private val pb: WeakReference<ProgressBar>,
    val timeStart: OffsetDateTime,
    val timeEnd: OffsetDateTime
): AsyncTask<Void, Int, String>() {
    var count = 0

    override fun onPreExecute() {
        pb.get()?.visibility = ProgressBar.VISIBLE
    }

    override fun doInBackground(vararg params: Void): String {
        while (count < 100) {
            val interval = getSecondsIntervalForDuration(timeStart, timeEnd)
            SystemClock.sleep(interval.toLong())
            count++
            publishProgress(count)
        }
        return "Complete"
    }

    override fun onProgressUpdate(vararg values: Int?) {
        pb.get()?.progress = values[0] ?: 0
    }

    /** Calculate a hundredth of timeStart - timeEnd duration. **/
    private fun getSecondsIntervalForDuration(timeStart: OffsetDateTime, timeEnd: OffsetDateTime): Int {
        return (timeEnd.second - timeStart.second) / 100
    }
}