package fho.kdvs.schedule

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

    // Hide image if it cannot fit on card
    if (view.layoutParams.height < view.resources.getDimension(R.dimen.timeslot_image)
                                 + (2 * view.resources.getDimension(R.dimen.timeslot_margin))) {
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

