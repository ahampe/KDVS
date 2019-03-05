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
import fho.kdvs.global.extensions.BitmapColorRequestListener
import fho.kdvs.global.util.TimeHelper
import kotlinx.android.synthetic.main.cell_timeslot.view.*
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

@BindingAdapter("timeslotHeight")
fun makeTimeslotHeight(view: CardView, height: Int){
    view.layoutParams.height = (
        height * view.context.resources.getDimension(R.dimen.timeslot_halfhour_height)
    ).toInt()

    // Hide image if it cannot fit on card
    val image = view.findViewById(R.id.showImage) as ImageView
    if (image.height > view.layoutParams.height)
        image.visibility = View.INVISIBLE
}

@BindingAdapter("timeslotGlideHref")
fun loadImageWithGlideAndSetVisualizations(view: ImageView, timeslot: TimeSlot?) {
    val parent = view.parent as ConstraintLayout
    Glide.with(view)
        .asBitmap()
        .load(timeslot?.imageHref)
        .transition(BitmapTransitionOptions.withCrossFade())
        .apply(
            RequestOptions()
                .apply(RequestOptions.centerCropTransform())
                .error(R.drawable.show_placeholder)
        )
        .listener(
            BitmapColorRequestListener(view, parent, timeslot)
        )
        .into(view)
}

