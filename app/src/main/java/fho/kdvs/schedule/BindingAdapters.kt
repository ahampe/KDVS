package fho.kdvs.schedule

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestOptions
import fho.kdvs.R
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.extensions.TimeSlotRequestListener
import fho.kdvs.global.util.TimeHelper
import kotlinx.android.synthetic.main.cell_timeslot.view.*
import org.threeten.bp.OffsetDateTime
import timber.log.Timber
import kotlin.math.floor
import kotlin.math.max

@BindingAdapter("timeStart", "timeEnd")
fun showTimeRange(view: TextView, timeStart: OffsetDateTime, timeEnd: OffsetDateTime) {
    view.text = view.context.resources.getString(
        R.string.show_time_range,
        TimeHelper.showTimeFormatter.format(timeStart),
        TimeHelper.showTimeFormatter.format(timeEnd)
    )
}

@BindingAdapter("index")
fun setShowSelectionHeader(view: TextView, index: Int) {
    view.text = when(index){
        0 -> view.resources.getString(R.string.thisWeek)
        1 -> view.resources.getString(R.string.nextWeek)
        2 -> view.resources.getString(R.string.thenWeek)
        else -> view.resources.getString(R.string.thenWeek)
    }
}

@BindingAdapter("timeSlotSize")
fun setShowTimeAlternatingText(view: TextView, size: Int) {
    if (size > 1) {
        if (size == 2) view.text = view.resources.getString(R.string.alternating_every_other)
        else if (size > 2) view.text = view.resources.getString(
            R.string.alternating_num,
            size)
        view.visibility = View.VISIBLE
    }
}

@BindingAdapter("query", "showName")
fun setShowSearchNameHighlight(view: TextView, query: String, showName: String) {
    if (query.isNotEmpty() && showName.isNotEmpty()) {
        val startIndex = showName.indexOf(query, 0, true)
        val stopIndex = startIndex + query.length

        if (startIndex != -1) {
            val spannable = SpannableString(showName)
            spannable.setSpan(ForegroundColorSpan(Color.BLUE), startIndex, stopIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            view.text = spannable
            // TODO: adapt this logic to set a white background with dark text color
            // TODO: call this method when query changes, even if results are same
        }
    }
}

@BindingAdapter("searchTimeStart", "searchTimeEnd")
fun setShowSearchTimes(view: TextView, timeStart: OffsetDateTime, timeEnd: OffsetDateTime){
    val dayAbbrs = listOf(
        view.resources.getString(R.string.sun),
        view.resources.getString(R.string.mon),
        view.resources.getString(R.string.tues),
        view.resources.getString(R.string.wed),
        view.resources.getString(R.string.thurs),
        view.resources.getString(R.string.fri),
        view.resources.getString(R.string.sat)
    )

    var dayText = dayAbbrs.getOrNull(timeStart.dayOfWeek.value % 6)

    if (timeEnd.dayOfWeek != timeStart.dayOfWeek)
        dayText += "/" + dayAbbrs.getOrNull(timeEnd.dayOfWeek.value % 6)

    view.text = view.context.resources.getString(
        R.string.searchTimeLabel,
        dayText,
        TimeHelper.showTimeFormatter.format(timeStart),
        TimeHelper.showTimeFormatter.format(timeEnd)
    )
}

@BindingAdapter("showNames", "layoutHeight")
fun makeShowNames(view: TextView, showNames: List<String>, numHalfHours: Int) {
    if (showNames.isEmpty()) return

    val cardHeight = numHalfHours * view.context.resources.getDimension(R.dimen.timeslot_halfhour_height)

    // find max number of showName lines to fit on card without breaking margins
    view.maxLines = max(1,
        floor((cardHeight - (2 * view.resources.getDimension(R.dimen.timeslot_margin)))
                / view.showName.height).toInt())
    view.text = if (showNames.size == 1) showNames.first() else showNames.joinToString(" &\n")
}

@BindingAdapter("timeslot", "timeslotHeight")
fun makeTimeslotHeight(view: CardView, timeslot: TimeSlot, numHalfHours: Int){
    view.layoutParams.height = (
        numHalfHours * view.context.resources.getDimension(R.dimen.timeslot_halfhour_height)
    ).toInt()

    val hourCardHeight = view.resources.getDimension(R.dimen.timeslot_image)
        + (2 * view.resources.getDimension(R.dimen.timeslot_margin))

    // Hide image if it cannot fit on card (half-hour shows)
    if (view.layoutParams.height < hourCardHeight) {
        Timber.d("half-hour show ${timeslot.names.firstOrNull()} detected")
        val image = view.findViewById(R.id.timeSlotImage) as ImageView
        image.visibility = View.GONE
    }
}

@BindingAdapter("timeslotGlideHref")
fun loadImageWithGlideAndSetVisualizations(view: ImageView, timeslot: TimeSlot?) {
    val parent = view.parent as ConstraintLayout

    Glide.with(view)
        .asBitmap()
        .load(timeslot?.imageHref)
        .apply(
            RequestOptions()
                .error(R.drawable.show_placeholder)
                .apply(RequestOptions.centerCropTransform())
        )
        .transition(BitmapTransitionOptions.withCrossFade())
        .listener(
            TimeSlotRequestListener(view, parent, timeslot)
        )
        .into(view)
}

