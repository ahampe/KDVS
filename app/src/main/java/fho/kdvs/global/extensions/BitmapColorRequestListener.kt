package fho.kdvs.global.extensions

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import androidx.palette.graphics.Palette
import com.bumptech.glide.load.DataSource
import fho.kdvs.R
import fho.kdvs.global.util.ColorHelper
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.schedule.TimeSlot
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import timber.log.Timber


class BitmapColorRequestListener(
    private val view: View,
    private val viewToColor: View,
    private val timeslot: TimeSlot?
) : RequestListener<Bitmap> {
    override fun onLoadFailed(
        e: GlideException?,
        model: Any,
        target: Target<Bitmap>,
        isFirstResource: Boolean
    ) : Boolean {
        return false
    }

    override fun onResourceReady(
        resource: Bitmap?,
        model: Any,
        target: Target<Bitmap>,
        dataSource: DataSource,
        isFirstResource: Boolean
    ): Boolean {
        if (resource != null) {
            // set placeholder timeslots to independent random colors
            // TODO: don't rely on static kdvs placeholder?
            val imageHref = timeslot?.imageHref ?: ""
            val showName = timeslot?.names?.first() ?: ""
            val isPlaceholder = (imageHref.contains(".*kdvs.org.*placeholder.*".toRegex()))
                   // || resource == (view.context.getDrawable(R.drawable.show_placeholder) as BitmapDrawable).bitmap
            val scheduleTime = TimeHelper.makeEpochRelativeTime(OffsetDateTime.now())
            val isCurrentShow = (scheduleTime >= timeslot?.timeStart)
                    && (scheduleTime < timeslot?.timeEnd)

            val seed = showName.hashCode().toLong() // use showName to generate random color
            var colorVal = ColorHelper.getRandomMatColor(500, view.context, seed)
            // TODO: make colortype dynamic?

            if (!isPlaceholder) { // TODO: dynamic color for grayscale images?
                val p = Palette.from(resource).generate()
                colorVal = p.getDarkVibrantColor(colorVal)
            }

            viewToColor.setBackgroundColor(colorVal) // TODO: make random color fit with palette

            val backgroundColors = intArrayOf(colorVal,  0xaa000000.toChar().toInt())
            val imageGradientDrawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, backgroundColors)
            view.foreground = imageGradientDrawable

            if (isCurrentShow){
                Timber.d("currentShow $showName")
            }
        }

        return false
    }
}