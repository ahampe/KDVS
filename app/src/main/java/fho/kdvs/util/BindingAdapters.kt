package fho.kdvs.util

import android.widget.TextView
import androidx.databinding.BindingAdapter
import fho.kdvs.R
import java.text.DateFormat
import java.util.*

@BindingAdapter("bind:timeStart", "bind:timeEnd")
fun showTimeRange(view: TextView, timeStart: Date, timeEnd: Date) {
    view.text = view.context.resources.getString(
        R.string.show_time_range,
        TimeUtil.formatter.format(timeStart),
        TimeUtil.formatter.format(timeEnd)
    )
}

private object TimeUtil {
    val formatter: DateFormat = DateFormat.getTimeInstance(DateFormat.SHORT)
}