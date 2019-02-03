package fho.kdvs.util

import android.widget.TextView
import androidx.databinding.BindingAdapter
import fho.kdvs.R
import org.threeten.bp.OffsetDateTime

@BindingAdapter("bind:timeStart", "bind:timeEnd")
fun showTimeRange(view: TextView, timeStart: OffsetDateTime, timeEnd: OffsetDateTime) {
    view.text = view.context.resources.getString(
        R.string.show_time_range,
        TimeHelper.showTimeFormatter.format(timeStart),
        TimeHelper.showTimeFormatter.format(timeEnd)
    )
}