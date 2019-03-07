package fho.kdvs.schedule

import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import fho.kdvs.R
import fho.kdvs.global.util.TimeHelper
import org.threeten.bp.OffsetDateTime

@BindingAdapter("timeStart", "timeEnd")
fun showTimeRange(view: TextView, timeStart: OffsetDateTime, timeEnd: OffsetDateTime) {
    view.text = view.context.resources.getString(
        R.string.show_time_range,
        TimeHelper.showTimeFormatter.format(timeStart),
        TimeHelper.showTimeFormatter.format(timeEnd)
    )
}

@BindingAdapter("showNames")
fun makeShowNames(view: TextView, showNames: List<String>) {
    if (showNames.isEmpty()) return

    if (showNames.size == 1) {
        view.text = showNames.first()
    } else {
        view.text = showNames.joinToString("\n&\n")
    }
}

@BindingAdapter("android:layout_height")
fun setTimeslotHeight(view: CardView, height: Int){
    view.layoutParams.height = (
        height * view.context.resources.getDimension(R.dimen.timeslot_halfhour_height)
    ).toInt()
}
