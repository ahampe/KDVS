package fho.kdvs.global.extensions

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.palette.graphics.Palette
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.narayanacharya.waveview.WaveView
import fho.kdvs.R
import fho.kdvs.global.util.ColorHelper
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.schedule.TimeSlot
import timber.log.Timber


/**
 * Apply dynamic [Palette] coloration and alpha gradients. Placeholder [TimeSlot]s are assigned random color
 * deterministically by their corresponding show names. [WaveView] animation applied to current show [TimeSlot].
 * */
class TimeSlotRequestListener (
    private val view: View,
    private val viewToColor: View,
    private val timeslot: TimeSlot?
) : RequestListener<Bitmap> {
    private var isPlaceholder = false
    private var color = 0
    private var seed = Long.MIN_VALUE

    override fun onLoadFailed( // TODO: fails for fbcdn images
        e: GlideException?,
        model: Any,
        target: Target<Bitmap>,
        isFirstResource: Boolean
    ) : Boolean {

        isPlaceholder = true

        setSeed()
        setRandomColor()
        setView()

        return false
    }

    override fun onResourceReady(
        resource: Bitmap?,
        model: Any,
        target: Target<Bitmap>,
        dataSource: DataSource,
        isFirstResource: Boolean
    ): Boolean {
        if (resource == null || timeslot == null) return false

        setSeed()
        setRandomColor()
        if (!isPlaceholder()) setPaletteColor(resource)
        setView()

        return false
    }

    private fun isPlaceholder(): Boolean{
        return ((timeslot?.imageHref ?: "").contains(".*kdvs.org.*placeholder.*".toRegex())) // TODO: don't rely on static kdvs placeholder?
        // || resource == (view.context.getDrawable(R.drawable.show_placeholder) as BitmapDrawable).bitmap // this produces glitches when shows aren't loaded in yet
    }

    /** Use showName to generate random color */
    private fun setSeed() {
        seed = (timeslot?.names?.first() ?: "").hashCode().toLong()
    }

    private fun setPaletteColor(bitmap: Bitmap) {
        color = Palette.from(bitmap).generate().getDarkVibrantColor(color)
    }

    private fun setRandomColor() {
        // TODO: make random color fit with theme
        // TODO: make colortype dynamic?
        color = ColorHelper.getRandomMatColor(500, view.context, seed)
    }

    private fun setView() {
        setViewColor()
        setViewTransparency()
        setViewSineWave()
    }

    private fun setViewColor() {
        viewToColor.setBackgroundColor(color)
    }

    /** Set transparent to opaque Left->Right gradient */
    private fun setViewTransparency() {
        val backgroundColors = intArrayOf(color, Color.TRANSPARENT)
        view.foreground = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, backgroundColors)
    }

    /** Sine wave animation overlay on current timeslot */
    private fun setViewSineWave() {
        if (timeslot == null) return

        val waveView = viewToColor.findViewById<WaveView>(R.id.waveView)
        if (TimeHelper.isTimeSlotForCurrentShow(timeslot)) {
            Timber.d("currentShow $timeslot.names.firstOrNull()")
            if (waveView != null && waveView.visibility != View.VISIBLE){
                waveView.backgroundColor = color
                waveView.waveColor = ColorHelper.getComplementaryColor(color, view.context)
                waveView.visibility = View.VISIBLE
            }
        } else {
            waveView.visibility = View.GONE
        }
    }
}