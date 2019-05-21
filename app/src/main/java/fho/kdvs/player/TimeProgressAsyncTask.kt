package fho.kdvs.player

import android.os.AsyncTask
import android.os.SystemClock
import android.widget.ProgressBar
import org.threeten.bp.OffsetDateTime
import java.lang.ref.WeakReference
import kotlin.math.roundToInt


/** Custom [ProgressBar] logic for live [PlayerFragment] / [PlayerBarView], in order to represent real-time duration. */
open class TimeProgressAsyncTask(
    private val pb: WeakReference<ProgressBar>,
    val timeStart: OffsetDateTime,
    val timeEnd: OffsetDateTime
): AsyncTask<Void, Int, String>() {
    private val initialProgress = getProgressForCurrentTime()

    override fun onPreExecute() {
        pb.get()?.progress = initialProgress
        pb.get()?.visibility = ProgressBar.VISIBLE
    }

    override fun doInBackground(vararg params: Void): String {
        val interval = getDurationInSecondsBetween(timeStart, timeEnd) / 100

        var count = 0
        while (count < (100 - initialProgress)) {
            SystemClock.sleep(interval.toLong() * 1000)
            count++
            publishProgress(count)
        }
        return "Complete"
    }

    override fun onProgressUpdate(vararg values: Int?) {
        pb.get()?.progress = values[0] ?: 0
    }

    private fun getDurationInSecondsBetween(start: OffsetDateTime, end: OffsetDateTime): Double {
        return if (start.hour > end.hour) {
            ((((24 - start.hour) + end.hour) * 3600 + end.minute * 60 + end.second)
                    - (start.minute * 60 + start.second)).toDouble()
        } else {
            ((end.hour * 3600 + end.minute * 60 + end.second)
            - (start.hour * 3600 + start.minute * 60 + start.second)).toDouble()
        }
    }

    /** Get progress in live broadcast relative to current time. **/
    private fun getProgressForCurrentTime(): Int {
        val now = getDurationInSecondsBetween(timeStart, OffsetDateTime.now())
        val duration = getDurationInSecondsBetween(timeStart, timeEnd)
        return if (duration < now) 0 else
            (100 * (now / duration)).roundToInt()
    }
}